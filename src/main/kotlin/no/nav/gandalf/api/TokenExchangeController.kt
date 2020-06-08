package no.nav.gandalf.api

import com.nimbusds.jwt.SignedJWT
import io.prometheus.client.Histogram
import mu.KotlinLogging
import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.accesstoken.SamlObject
import no.nav.gandalf.api.metric.ApplicationMetric
import no.nav.gandalf.api.metric.ApplicationMetric.exchangeDIFINotOk
import no.nav.gandalf.api.metric.ApplicationMetric.exchangeDIFIOk
import no.nav.gandalf.api.metric.ApplicationMetric.exchangeOIDCTokenOk
import no.nav.gandalf.api.metric.ApplicationMetric.exchangeSAMLTokenOk
import no.nav.gandalf.api.metric.ApplicationMetric.exchangeTokenNotOk
import no.nav.gandalf.model.AccessTokenResponse
import no.nav.gandalf.model.ErrorResponse
import no.nav.gandalf.service.ExchangeTokenService
import org.apache.commons.codec.binary.Base64
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets

private val log = KotlinLogging.logger { }

@RestController
@RequestMapping("rest/v1/sts", produces = ["application/json"])
class TokenExchangeController {

    @Autowired
    private lateinit var issuer: AccessTokenIssuer

    @PostMapping("/token/exchange")
    fun exchangeSAMLToOIDCToSAMLToken(
        @RequestParam("grant_type") grantType: String?,
        @RequestParam("requested_token_type") reqTokenType: String?,
        @RequestParam("subject_token") subjectToken: String?,
        @RequestParam("subject_token_type") subTokenType: String?
    ): ResponseEntity<Any> {
        val requestTimer: Histogram.Timer = ApplicationMetric.requestLatencyTokenExchange.startTimer()
        try {
            var copyReqTokenType = reqTokenType
            log.debug("Exchange $subTokenType to $copyReqTokenType")
            val user = userDetails() ?: return unauthorizedResponse(Throwable(), "Unauthorized").also {
                exchangeTokenNotOk.inc()
            }
            if (grantType.isNullOrEmpty() || !grantType.equals(
                    "urn:ietf:params:oauth:grant-type:token-exchange",
                    ignoreCase = true
                )
            ) {
                exchangeTokenNotOk.inc()
                return badRequestResponse("Unknown grant_type")
            }
            if (subjectToken.isNullOrEmpty()) {
                exchangeTokenNotOk.inc()
                return badRequestResponse("Missing subject_token in request")
            }
            when {
                subTokenType.equals("urn:ietf:params:oauth:token-type:saml2", ignoreCase = true) -> {
                    // exchange SAML token to OIDC token
                    log.debug("Exchange SAML token to OIDC")
                    val oidcToken: SignedJWT?
                    oidcToken = try {
                        val decodedSaml = Base64.decodeBase64(subjectToken.toByteArray())
                        issuer.exchangeSamlToOidcToken(String(decodedSaml, StandardCharsets.UTF_8))
                    } catch (e: Throwable) {
                        exchangeTokenNotOk.inc()
                        return badRequestResponse(e.message!!)
                    }
                    exchangeSAMLTokenOk.inc()
                    return ResponseEntity
                        .status(HttpStatus.OK)
                        .headers(tokenHeaders)
                        .body(ExchangeTokenService().getResponseFrom(oidcToken!!))
                }
                subTokenType.equals("urn:ietf:params:oauth:token-type:access_token", ignoreCase = true)
                    && (copyReqTokenType == null || copyReqTokenType.equals(
                    "urn:ietf:params:oauth:token-type:saml2",
                    ignoreCase = true
                )) -> {
                    // exchange OIDC token to SAML token
                    log.debug("Exchange OIDC to SAML token")
                    if (copyReqTokenType == null) {
                        copyReqTokenType = "urn:ietf:params:oauth:token-type:saml2"
                    }

                    val saml = try {
                        val samlToken = issuer.exchangeOidcToSamlToken(subjectToken, user)
                        val samlObj = SamlObject()
                        samlObj.read(samlToken)
                        Pair(samlToken, samlObj)
                    } catch (e: Throwable) {
                        exchangeTokenNotOk.inc()
                        return badRequestResponse(e.message!!)
                    }
                    exchangeOIDCTokenOk.inc()
                    return ResponseEntity.status(HttpStatus.OK)
                        .headers(tokenHeaders)
                        .body(
                            ExchangeTokenService().constructResponse(
                                saml.first,
                                "Bearer",
                                copyReqTokenType,
                                saml.second.expiresIn,
                                true
                            )
                        )
                }
                else -> {
                    exchangeTokenNotOk.inc()
                    return badRequestResponse("Unsupported token exchange for subject/requested token type")
                }
            }
        } finally {
            requestTimer.observeDuration()
        }
    }

    @PostMapping("/token/exchangedifi")
    fun exchangeDIFIOIDCToken(
        @RequestHeader("token") difiToken: String?
    ): ResponseEntity<Any> {
        val requestTimer: Histogram.Timer = ApplicationMetric.requestLatencyTokenExchangeDIFI.startTimer()
        try {
            log.debug("Exchange difi token to oidc token")
            try {
                require(userDetails() == "srvDatapower") { "Client is unauthorized for this endpoint" }
            } catch (e: Throwable) {
                exchangeDIFINotOk.inc()
                return unauthorizedResponse(e, e.message!!)
            }
            if (difiToken.isNullOrEmpty()) {
                val errorMessage = "Exchange difi token called with null or empty difi token"
                log.error(errorMessage)
                exchangeDIFINotOk.inc()
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(errorMessage))
            }
            val oidcToken = try {
                issuer.exchangeDifiTokenToOidc(difiToken)
            } catch (e: Throwable) {
                exchangeDIFINotOk.inc()
                return badRequestResponse("Failed to exchange difi oidc token to oidc token: " + e.message)
            }
            exchangeDIFIOk.inc()
            return ResponseEntity.status(HttpStatus.OK)
                .headers(tokenHeaders)
                .body(AccessTokenResponse(oidcToken))
        } finally {
            requestTimer.observeDuration()
        }
    }
}
