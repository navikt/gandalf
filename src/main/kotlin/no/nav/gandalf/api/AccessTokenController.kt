package no.nav.gandalf.api

import io.prometheus.client.Histogram
import mu.KotlinLogging
import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.accesstoken.SamlObject
import no.nav.gandalf.api.metric.ApplicationMetric.requestLatencySAMLToken
import no.nav.gandalf.api.metric.ApplicationMetric.requestLatencyToken
import no.nav.gandalf.api.metric.ApplicationMetric.requestLatencyToken2
import no.nav.gandalf.api.metric.ApplicationMetric.samlTokenError
import no.nav.gandalf.api.metric.ApplicationMetric.samlTokenNotOk
import no.nav.gandalf.api.metric.ApplicationMetric.samlTokenOk
import no.nav.gandalf.api.metric.ApplicationMetric.token2Error
import no.nav.gandalf.api.metric.ApplicationMetric.token2NotOk
import no.nav.gandalf.api.metric.ApplicationMetric.token2Ok
import no.nav.gandalf.api.metric.ApplicationMetric.tokenError
import no.nav.gandalf.api.metric.ApplicationMetric.tokenNotOk
import no.nav.gandalf.api.metric.ApplicationMetric.tokenOK
import no.nav.gandalf.config.LdapConfig
import no.nav.gandalf.ldap.Ldap
import no.nav.gandalf.model.AccessToken2Response
import no.nav.gandalf.model.User
import no.nav.gandalf.service.AccessTokenResponseService
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
class AccessTokenController(
    @Autowired val ldapConfig: LdapConfig
) {

    @Autowired
    private lateinit var issuer: AccessTokenIssuer

    @GetMapping("/token")
    fun getOIDCToken(
        @RequestParam("grant_type", required = true) grantType: String,
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
                    val user = userDetails() ?: return unauthorizedResponse(Throwable(), "Unauthorized")
                        .also {
                            tokenNotOk.inc()
                        }
                    val oidcToken = try {
                        issuer.issueToken(user)
                    } catch (e: Throwable) {
                        tokenError.inc()
                        return serverErrorResponse(e)
                    }
                    tokenOK.inc()
                    return ResponseEntity.status(HttpStatus.OK).headers(tokenHeaders)
                        .body(AccessTokenResponseService(oidcToken!!).tokenResponse)
                }
            }
        } finally {
            requestTimer.observeDuration()
        }
    }

    // As specified in the Standard
    @PostMapping("/token")
    fun postOIDCToken(
        @RequestParam("grant_type", required = true) grantType: String,
        @RequestParam("scope", required = true) scope: String
    ): ResponseEntity<Any> {
        return getOIDCToken(grantType, scope)
    }

    @GetMapping("/token2")
    fun getOIDCToken2(
        @RequestHeader("username") username: String,
        @RequestHeader("password") password: String
    ): ResponseEntity<Any> {
        val requestTimer: Histogram.Timer = requestLatencyToken2.startTimer()
        try {
            try {
                Ldap(ldapConfig).result(User(username, password))
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

    @GetMapping("/samltoken")
    fun getSAMLToken(): ResponseEntity<Any> {
        val requestTimer: Histogram.Timer = requestLatencySAMLToken.startTimer()
        try {
            val user = userDetails() ?: return unauthorizedResponse(Throwable(), "Unauthorized").also {
                samlTokenNotOk.inc()
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
