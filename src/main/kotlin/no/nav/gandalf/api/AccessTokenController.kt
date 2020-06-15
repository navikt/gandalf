package no.nav.gandalf.api

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
import no.nav.gandalf.accesstoken.SamlObject
import no.nav.gandalf.api.Util.Companion.badRequestResponse
import no.nav.gandalf.api.Util.Companion.serverErrorResponse
import no.nav.gandalf.api.Util.Companion.tokenHeaders
import no.nav.gandalf.api.Util.Companion.unauthorizedResponse
import no.nav.gandalf.api.Util.Companion.userDetails
import no.nav.gandalf.config.LdapConfig
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
import no.nav.gandalf.model.User
import no.nav.gandalf.service.ExchangeTokenService
import no.nav.gandalf.util.authenticate
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
    @Autowired val ldapConfig: LdapConfig
) {

    @Autowired
    private lateinit var issuer: AccessTokenIssuer

    @Operation(
        summary = "System User -> OIDC Token",
        deprecated = true,
        security = [SecurityRequirement(name = "BasicAuth")]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Issued OIDC Token",
                content = [
                    (
                        Content(
                            mediaType = "application/json",
                            schema = Schema(implementation = AccessTokenResponse::class)
                        )
                        )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = INVALID_CLIENT,
                content = [Content(schema = Schema(implementation = ErrorDescriptiveResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = INVALID_REQUEST,
                content = [Content(schema = Schema(implementation = ErrorDescriptiveResponse::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = INTERNAL_SERVER_ERROR,
                content = [Content()]
            )
        ]
    )
    @GetMapping("/token")
    fun getOIDCToken(
        @Parameter(description = "(Defined in RFC 6749, section 4.4) allows an application to request an Access Token using its Client Id and Client Secret")
        @RequestParam(
            "grant_type",
            required = true
        ) grantType: String,
        @Parameter(description = "Indicate that the application intends to use OIDC to verify the user's identity")
        @RequestParam("scope", required = true) scope: String
    ): ResponseEntity<Any> {
        val requestTimer: Histogram.Timer = requestLatencyToken.startTimer()
        try {
            when {
                grantType != "client_credentials" || scope != "openid" -> {
                    tokenNotOk.inc()
                    return badRequestResponse("grant_type = $grantType, scope = $scope")
                }
                else -> {
                    val user = requireNotNull(userDetails()) {
                        tokenNotOk.inc()
                        return unauthorizedResponse(Throwable(), "Unauthorized")
                    }
                    val oidcToken = try {
                        issuer.issueToken(user)
                    } catch (e: Throwable) {
                        tokenError.inc()
                        return serverErrorResponse(e)
                    }
                    tokenOK.inc()
                    return ResponseEntity.status(HttpStatus.OK).headers(tokenHeaders)
                        .body(AccessTokenResponse(oidcToken!!))
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
                responseCode = "200", description = "Issued OIDC Token",
                content = [
                    (
                        Content(
                            mediaType = "application/json",
                            schema = Schema(implementation = AccessTokenResponse::class)
                        )
                        )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = INVALID_CLIENT,
                content = [Content(schema = Schema(implementation = ErrorDescriptiveResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = INVALID_REQUEST,
                content = [Content(schema = Schema(implementation = ErrorDescriptiveResponse::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = INTERNAL_SERVER_ERROR,
                content = [Content()]
            )
        ]
    )
    @PostMapping("/token")
    fun postOIDCToken(
        @Parameter(description = "(Defined in RFC 6749, section 4.4) allows an application to request an Access Token using its Client Id and Client Secret")
        @RequestParam("grant_type", required = true, defaultValue = "client_credentials") grantType: String,
        @Parameter(description = "Indicate that the application intends to use OIDC to verify the user's identity")
        @RequestParam("scope", required = true, defaultValue = "openid") scope: String
    ): ResponseEntity<Any> {
        return getOIDCToken(grantType, scope)
    }

    @Operation(summary = "Stormaskin: System User -> OIDC Token")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Issued OIDC Token",
                headers = [
                    Header(name = "username", description = "Username for Authentication"),
                    Header(name = "password", description = "Password For Authentication")
                ],
                content = [
                    (
                        Content(
                            mediaType = "application/json",
                            schema = Schema(implementation = AccessToken2Response::class)
                        )
                        )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = INVALID_CLIENT,
                content = [Content(schema = Schema(implementation = ErrorDescriptiveResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = INVALID_REQUEST,
                content = [Content(schema = Schema(implementation = ErrorDescriptiveResponse::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = INTERNAL_SERVER_ERROR,
                content = [Content()]
            )
        ]
    )
    @GetMapping("/token2")
    fun getOIDCToken2(
        @RequestHeader("username") username: String,
        @RequestHeader("password") password: String
    ): ResponseEntity<Any> {
        val requestTimer: Histogram.Timer = requestLatencyToken2.startTimer()
        try {
            try {
                authenticate(ldapConfig, User(username, password))
            } catch (e: Throwable) {
                token2NotOk.inc()
                return unauthorizedResponse(e, "Could Not Authenticate username: $username")
            }
            log.info("Issue OIDC token2 for user: $username")
            val oidcToken = try {
                issuer.issueToken(username)
            } catch (e: Throwable) {
                token2Error.inc()
                return serverErrorResponse(e)
            }
            token2Ok.inc()
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(AccessToken2Response(oidcToken!!))
        } finally {
            requestTimer.observeDuration()
        }
    }

    @Operation(summary = "System User -> SAML Token", security = [SecurityRequirement(name = "BasicAuth")])
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Issued OIDC Token",
                headers = [
                    Header(name = "username", description = "Username for Authentication"),
                    Header(name = "password", description = "Password For Authentication")
                ],
                content = [
                    (
                        Content(
                            mediaType = "application/json",
                            schema = Schema(implementation = ExchangeTokenResponse::class)
                        )
                        )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = INVALID_CLIENT,
                content = [Content(schema = Schema(implementation = ErrorDescriptiveResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = INVALID_REQUEST,
                content = [Content(schema = Schema(implementation = ErrorDescriptiveResponse::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = INTERNAL_SERVER_ERROR,
                content = [Content()]
            )
        ]
    )
    @GetMapping("/samltoken")
    fun getSAMLToken(): ResponseEntity<Any> {
        val requestTimer: Histogram.Timer = requestLatencySAMLToken.startTimer()
        try {
            val user = requireNotNull(userDetails()) {
                samlTokenNotOk.inc()
                return unauthorizedResponse(Throwable(), "Unauthorized")
            }
            log.debug("Issue SAML token for: $user")
            val samlToken = try {
                issuer.issueSamlToken(user, user, AccessTokenIssuer.DEFAULT_SAML_AUTHLEVEL)
            } catch (e: Throwable) {
                samlTokenError.inc()
                return serverErrorResponse(e)
            }
            val samlObj = SamlObject().apply {
                this.read(samlToken)
            }
            samlTokenOk.inc()
            return ResponseEntity
                .status(HttpStatus.OK)
                .headers(tokenHeaders)
                .body(
                    ExchangeTokenService().constructResponse(
                        samlToken,
                        "Bearer",
                        "urn:ietf:params:oauth:token-type:saml2",
                        samlObj.expiresIn,
                        false
                    )
                )
        } finally {
            requestTimer.observeDuration()
        }
    }
}
