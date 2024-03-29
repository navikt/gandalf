package no.nav.gandalf.api.controllers

import com.nimbusds.jwt.SignedJWT
import io.prometheus.client.Histogram
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.accesstoken.saml.SamlObject
import no.nav.gandalf.api.INTERNAL_SERVER_ERROR
import no.nav.gandalf.api.INVALID_CLIENT
import no.nav.gandalf.api.INVALID_REQUEST
import no.nav.gandalf.api.Util.badRequestResponse
import no.nav.gandalf.api.Util.tokenHeaders
import no.nav.gandalf.api.Util.unauthorizedResponse
import no.nav.gandalf.api.Util.userDetails
import no.nav.gandalf.metric.ApplicationMetric
import no.nav.gandalf.model.AccessTokenResponse
import no.nav.gandalf.model.ErrorDescriptiveResponse
import no.nav.gandalf.model.ErrorResponse
import no.nav.gandalf.model.ExchangeTokenResponse
import no.nav.gandalf.service.ExchangeTokenService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets
import java.util.Base64

private val log = KotlinLogging.logger { }

@RestController
@RequestMapping("rest/v1/sts", produces = ["application/json"])
@Tag(
    name = "OIDC/SAML Token Exchange",
    description = "Exchange SAML (Datapower STS) -> OIDC & Exchange OIDC (OpenAm, Azure, IDP) -> SAML",
)
class TokenExchangeController {
    @Autowired
    private lateinit var issuer: AccessTokenIssuer

    @Operation(summary = "SAML <-> OIDC", security = [SecurityRequirement(name = "BasicAuth")])
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Issued OIDC/SAML Token",
                content = [
                    (
                        Content(
                            mediaType = "application/json",
                            schema = Schema(implementation = ExchangeTokenResponse::class),
                        )
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "401",
                description = INVALID_CLIENT,
                content = [Content(schema = Schema(implementation = ErrorDescriptiveResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = INVALID_REQUEST,
                content = [Content(schema = Schema(implementation = ErrorDescriptiveResponse::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = INTERNAL_SERVER_ERROR,
                content = [Content()],
            ),
        ],
    )
    @PostMapping("/token/exchange", "/token/exchange/", consumes = ["application/x-www-form-urlencoded"])
    fun exchangeSAMLToOIDCToSAMLToken(
        @Parameter(
            description = "'grant type' refers to the way an application gets an access token. OAuth 2.0 defines several grant types.",
            required = true,
        )
        @RequestParam("grant_type")
        grantType: String?,
        @Parameter(
            description = "An identifier, as described in Token Type Identifiers (OAuth 2.0 Token Exchange Section 3), for the type of the requested security token.",
            required = false,
        )
        @RequestParam("requested_token_type", required = false)
        reqTokenType: String?,
        @Parameter(
            description = "Represents the identity of the party on behalf of whom the token is being requested.",
            required = true,
        )
        @RequestParam("subject_token")
        subjectToken: String?,
        @Parameter(
            description =
                "An identifier, as described in Token Type Identifiers " +
                    "(OAuth 2.0 Token Exchange Section 3), that indicates the type of the security " +
                    "token in the 'subject_token' parameter.",
            required = true,
        )
        @RequestParam("subject_token_type")
        subTokenType: String?,
    ): ResponseEntity<Any> {
        val requestTimer: Histogram.Timer = ApplicationMetric.requestLatencyTokenExchange.startTimer()
        try {
            log.info("Exchange $subTokenType to $reqTokenType")
            val user =
                requireNotNull(userDetails()) {
                    ApplicationMetric.exchangeTokenNotOk.inc()
                    return unauthorizedResponse(Throwable(), "Unauthorized")
                }
            if (grantType.isNullOrEmpty() || grantType != "urn:ietf:params:oauth:grant-type:token-exchange") {
                ApplicationMetric.exchangeTokenNotOk.inc()
                return badRequestResponse("Unknown grant_type")
            }
            if (subjectToken.isNullOrEmpty()) {
                ApplicationMetric.exchangeTokenNotOk.inc()
                return badRequestResponse("Missing subject_token in request")
            }
            when {
                subTokenType.equals("urn:ietf:params:oauth:token-type:saml2") -> {
                    log.info("Exchange SAML token to OIDC")
                    return try {
                        val decodedSaml = Base64.getDecoder().decode(subjectToken.toByteArray())
                        val oidcToken: SignedJWT =
                            issuer.exchangeSamlToOidcToken(String(decodedSaml, StandardCharsets.UTF_8))
                        ApplicationMetric.exchangeSAMLTokenOk.labels(user).inc()
                        ResponseEntity
                            .status(HttpStatus.OK)
                            .headers(tokenHeaders)
                            .body(ExchangeTokenService().getResponseFrom(oidcToken))
                    } catch (e: Throwable) {
                        ApplicationMetric.exchangeTokenNotOk.inc()
                        badRequestResponse(e.message ?: "")
                    }
                }

                subTokenType.equals("urn:ietf:params:oauth:token-type:access_token") &&
                    (reqTokenType.isNullOrEmpty() || reqTokenType == "urn:ietf:params:oauth:token-type:saml2") -> {
                    log.info("Exchange OIDC to SAML token")
                    return try {
                        val samlToken = issuer.exchangeOidcToSamlToken(subjectToken, user)
                        val samlObj = SamlObject()
                        samlObj.read(samlToken)
                        ApplicationMetric.exchangeOIDCTokenOk.labels(user).inc()
                        ResponseEntity.status(HttpStatus.OK)
                            .headers(tokenHeaders)
                            .body(
                                ExchangeTokenService().constructResponse(
                                    samlToken,
                                    "Bearer",
                                    if (reqTokenType.isNullOrEmpty()) "urn:ietf:params:oauth:token-type:saml2" else reqTokenType,
                                    samlObj.expiresIn(),
                                    true,
                                ),
                            )
                    } catch (e: Throwable) {
                        ApplicationMetric.exchangeTokenNotOk.inc()
                        return badRequestResponse(e.message ?: "")
                    }
                }

                else -> {
                    ApplicationMetric.exchangeTokenNotOk.inc()
                    return badRequestResponse("Unsupported token exchange for subject/requested token type")
                }
            }
        } finally {
            requestTimer.observeDuration()
        }
    }

    @Operation(summary = "DIFI Mmaskinporten TOKEN -> OIDC", security = [SecurityRequirement(name = "BasicAuth")])
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Issued OIDC Token",
                content = [
                    (
                        Content(
                            mediaType = "application/json",
                            schema = Schema(implementation = AccessTokenResponse::class),
                        )
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "401",
                description = INVALID_CLIENT,
                content = [Content(schema = Schema(implementation = ErrorDescriptiveResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = INVALID_REQUEST,
                content = [Content(schema = Schema(implementation = ErrorDescriptiveResponse::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = INTERNAL_SERVER_ERROR,
                content = [Content()],
            ),
        ],
    )
    @PostMapping("/token/exchangedifi", "/token/exchangedifi/")
    fun exchangeDIFIOIDCToken(
        @Parameter(
            description = "Base64Encoded DIFI Access Token.",
            required = true,
        )
        @RequestHeader("token", required = true)
        difiToken: String?,
    ): ResponseEntity<Any> {
        val requestTimer: Histogram.Timer = ApplicationMetric.requestLatencyTokenExchangeDIFI.startTimer()
        try {
            log.info("Exchange difi token to oidc token")
            try {
                require(userDetails() == "srvDatapower") { "Client is Unauthorized for this endpoint" }
            } catch (e: Throwable) {
                ApplicationMetric.exchangeDIFINotOk.inc()
                return unauthorizedResponse(e, e.message!!)
            }
            if (difiToken.isNullOrEmpty()) {
                val errorMessage = "Exchange difi token called with null or empty difi token"
                log.error(errorMessage)
                ApplicationMetric.exchangeDIFINotOk.inc()
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(errorMessage))
            }
            return try {
                val oidcToken = issuer.exchangeDifiTokenToOidc(difiToken)
                ApplicationMetric.exchangeDIFIOk.inc()
                ResponseEntity.status(HttpStatus.OK)
                    .headers(tokenHeaders)
                    .body(AccessTokenResponse(oidcToken))
            } catch (e: Throwable) {
                ApplicationMetric.exchangeDIFINotOk.inc()
                badRequestResponse("Failed to exchange difi token to oidc token: " + e.message)
            }
        } finally {
            requestTimer.observeDuration()
        }
    }
}
