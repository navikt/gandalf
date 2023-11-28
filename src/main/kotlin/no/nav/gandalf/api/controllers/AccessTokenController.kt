package no.nav.gandalf.api.controllers

import io.prometheus.client.Histogram
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
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
import no.nav.gandalf.api.Util.serverErrorResponse
import no.nav.gandalf.api.Util.tokenHeaders
import no.nav.gandalf.api.Util.unauthorizedResponse
import no.nav.gandalf.api.Util.userDetails
import no.nav.gandalf.ldap.CustomAuthenticationProvider
import no.nav.gandalf.ldap.authenticate
import no.nav.gandalf.metric.ApplicationMetric.Companion.issuedTokenCounterUnique
import no.nav.gandalf.metric.ApplicationMetric.Companion.requestLatencySAMLToken
import no.nav.gandalf.metric.ApplicationMetric.Companion.requestLatencyToken
import no.nav.gandalf.metric.ApplicationMetric.Companion.requestLatencyToken2
import no.nav.gandalf.metric.ApplicationMetric.Companion.samlTokenError
import no.nav.gandalf.metric.ApplicationMetric.Companion.samlTokenNotOk
import no.nav.gandalf.metric.ApplicationMetric.Companion.samlTokenOk
import no.nav.gandalf.metric.ApplicationMetric.Companion.token2Error
import no.nav.gandalf.metric.ApplicationMetric.Companion.token2NotOk
import no.nav.gandalf.metric.ApplicationMetric.Companion.token2Ok
import no.nav.gandalf.metric.ApplicationMetric.Companion.tokenError
import no.nav.gandalf.metric.ApplicationMetric.Companion.tokenNotOk
import no.nav.gandalf.metric.ApplicationMetric.Companion.tokenOK
import no.nav.gandalf.model.AccessToken2Response
import no.nav.gandalf.model.AccessTokenResponse
import no.nav.gandalf.model.ErrorDescriptiveResponse
import no.nav.gandalf.model.ExchangeTokenResponse
import no.nav.gandalf.service.ExchangeTokenService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger { }

@RestController
@RequestMapping("rest/v1/sts", produces = ["application/json"])
@Tag(name = "System OIDC Token", description = "System User to OIDC & SAML Token")
class AccessTokenController(
    @Autowired val authenticationProvider: CustomAuthenticationProvider,
) {
    @Autowired
    private lateinit var issuer: AccessTokenIssuer

    @Operation(
        summary = "System User -> OIDC Token",
        deprecated = true,
        security = [SecurityRequirement(name = "BasicAuth")],
    )
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
    @GetMapping("/token", "/token/")
    fun getOIDCToken(
        @Parameter(
            description = "(Defined in RFC 6749, section 4.4) allows an application to request an Access Token using its Client Id and Client Secret",
        )
        @RequestParam(
            "grant_type",
            required = true,
        ) grantType: String,
        @Parameter(description = "Indicate that the application intends to use OIDC to verify the user's identity")
        @RequestParam("scope", required = true) scope: String,
    ): ResponseEntity<Any> {
        val requestTimer: Histogram.Timer = requestLatencyToken.startTimer()
        try {
            when {
                grantType != "client_credentials" || scope != "openid" -> {
                    tokenNotOk.inc()
                    return badRequestResponse("grant_type = $grantType, scope = $scope")
                }
                else -> {
                    val user =
                        requireNotNull(userDetails()) {
                            tokenNotOk.inc()
                            return unauthorizedResponse(Throwable(), "Unauthorized")
                        }
                    return try {
                        val oidcToken = issuer.issueToken(user)
                        tokenOK.inc()
                        issuedTokenCounterUnique.labels(user).inc()
                        ResponseEntity
                            .status(HttpStatus.OK)
                            .headers(tokenHeaders)
                            .body(AccessTokenResponse(oidcToken))
                    } catch (e: Throwable) {
                        tokenError.inc()
                        serverErrorResponse(e)
                    }
                }
            }
        } finally {
            requestTimer.observeDuration()
        }
    }

    // As specified in the Standard
    @Operation(summary = "System User -> OIDC Token", security = [SecurityRequirement(name = "BasicAuth")])
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
    @PostMapping("/token", "/token/")
    fun postOIDCToken(
        @Parameter(
            description = "(Defined in RFC 6749, section 4.4) allows an application to request an Access Token using its Client Id and Client Secret",
        )
        @RequestParam("grant_type", required = true, defaultValue = "client_credentials") grantType: String,
        @Parameter(description = "Indicate that the application intends to use OIDC to verify the user's identity")
        @RequestParam("scope", required = true, defaultValue = "openid") scope: String,
    ): ResponseEntity<Any> {
        return getOIDCToken(grantType, scope)
    }

    @Operation(summary = "Stormaskin: System User -> OIDC Token")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Issued OIDC Token",
                headers = [
                    Header(name = "username", description = "Username for Authentication"),
                    Header(name = "password", description = "Password For Authentication"),
                ],
                content = [
                    (
                        Content(
                            mediaType = "application/json",
                            schema = Schema(implementation = AccessToken2Response::class),
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
    @GetMapping("/token2", "/token2/")
    fun getOIDCToken2(
        @RequestHeader("username") username: String?,
        @RequestHeader("password") password: String?,
    ): ResponseEntity<Any> {
        val requestTimer: Histogram.Timer = requestLatencyToken2.startTimer()
        try {
            try {
                authenticationProvider.authenticate(username, password)
            } catch (e: Throwable) {
                token2NotOk.inc()
                return unauthorizedResponse(e, e.message ?: "")
            }
            log.info("Issue OIDC token2 for user: $username")
            return try {
                val oidcToken = issuer.issueToken(username)
                token2Ok.labels(username).inc()
                return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(AccessToken2Response(oidcToken))
            } catch (e: Throwable) {
                token2Error.inc()
                serverErrorResponse(e)
            }
        } finally {
            requestTimer.observeDuration()
        }
    }

    @Operation(summary = "System User -> SAML Token", security = [SecurityRequirement(name = "BasicAuth")])
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Issued SAML Token",
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
    @GetMapping("/samltoken", "/samltoken/")
    fun getSAMLToken(): ResponseEntity<Any> {
        val requestTimer: Histogram.Timer = requestLatencySAMLToken.startTimer()
        try {
            val user =
                requireNotNull(userDetails()) {
                    samlTokenNotOk.inc()
                    return unauthorizedResponse(Throwable(), "Unauthorized")
                }
            log.info("Issue SAML token for: $user")
            return try {
                val samlToken = issuer.issueSamlToken(user, user, AccessTokenIssuer.DEFAULT_SAML_AUTHLEVEL)
                val samlObj =
                    SamlObject().apply {
                        this.read(samlToken)
                    }
                samlTokenOk.labels(user).inc()
                return ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(tokenHeaders)
                    .body(
                        ExchangeTokenService().constructResponse(
                            samlToken,
                            "Bearer",
                            "urn:ietf:params:oauth:token-type:saml2",
                            samlObj.expiresIn(),
                            false,
                        ),
                    )
            } catch (e: Throwable) {
                samlTokenError.inc()
                serverErrorResponse(e)
            }
        } finally {
            requestTimer.observeDuration()
        }
    }
}
