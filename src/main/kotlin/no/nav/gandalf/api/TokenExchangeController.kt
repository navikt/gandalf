package no.nav.gandalf.api

import com.nimbusds.jwt.SignedJWT
import java.nio.charset.StandardCharsets
import mu.KotlinLogging
import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.accesstoken.SamlObject
import no.nav.gandalf.model.ErrorResponse
import no.nav.gandalf.service.AccessTokenResponseService
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

private val log = KotlinLogging.logger { }

@RestController
@RequestMapping("v1/sts", produces = ["application/json"])
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
        var copyReqTokenType = reqTokenType
        log.debug("Exchange $subTokenType to $copyReqTokenType")

        // sjekk at bruker er gyldig, dvs at ikke basic auth har feilet
        // TODO Flytt denne ut for validering
        val username = try {
            authDetails()
        } catch (e: Exception) {
            return unauthorizedResponse(e, e.message!!)
        }
        if (grantType.isNullOrEmpty() || !grantType.equals("urn:ietf:params:oauth:grant-type:token-exchange", ignoreCase = true)) {
            return badRequestResponse("Unknown grant_type")
        }
        if (subjectToken.isNullOrEmpty()) {
            return badRequestResponse("Missing subject_token in request")
        }
        when {
            subTokenType.equals("urn:ietf:params:oauth:token-type:saml2", ignoreCase = true) -> {
                // exchange SAML token to OIDC token
                log.debug("Exchange SAML token to OIDC")
                val oidcToken: SignedJWT?
                oidcToken = try {
                    // byte[] decodedSaml = Base64.getUrlDecoder().decode(subjectToken);
                    val decodedSaml = Base64.decodeBase64(subjectToken.toByteArray())
                    issuer.exchangeSamlToOidcToken(String(decodedSaml, StandardCharsets.UTF_8))
                } catch (e: Exception) {
                    return badRequestResponse(e.message!!)
                }
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .headers(tokenHeaders)
                        .body(ExchangeTokenService().getResponseFrom(oidcToken!!))
            }
            subTokenType.equals("urn:ietf:params:oauth:token-type:access_token", ignoreCase = true)
                    && (copyReqTokenType == null || copyReqTokenType.equals("urn:ietf:params:oauth:token-type:saml2", ignoreCase = true)) -> {
                // exchange OIDC token to SAML token
                log.debug("Exchange OIDC to SAML token")
                if (copyReqTokenType == null) {
                    copyReqTokenType = "urn:ietf:params:oauth:token-type:saml2"
                }

                val saml = try {
                    val samlToken = issuer.exchangeOidcToSamlToken(subjectToken, username)
                    val samlObj = SamlObject()
                    samlObj.read(samlToken)
                    Pair(samlToken, samlObj)
                } catch (e: Exception) {
                    return badRequestResponse(e.message!!)
                }
                return ResponseEntity.status(HttpStatus.OK)
                        .headers(tokenHeaders)
                        .body(ExchangeTokenService().constructResponse(
                                saml.first,
                                "Bearer",
                                copyReqTokenType,
                                saml.second.expiresIn,
                                true)
                        )
            }
            else -> {
                return badRequestResponse("Unsupported token exchange for subject/requested token type")
            }
        }
    }

    @PostMapping("/token/exchangedifi")
    fun exchangeDIFIOIDCToken(
        @RequestHeader("token") difiToken: String?
    ): ResponseEntity<Any> {
        log.debug("Exchange difi token to oidc token")
        try {
            require(authDetails() == "srvDatapower") { "Client is unauthorized for this endpoint" }
        } catch (e: Exception) {
            return unauthorizedResponse(e, e.message!!)
        }
        if (difiToken.isNullOrEmpty()) {
            val errorMessage = "Exchange difi token called with null or empty difi token"
            log.error(errorMessage)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(errorMessage))
        }
        val oidcToken = try {
            issuer.exchangeDifiTokenToOidc(difiToken)
        } catch (e: Exception) {
            return badRequestResponse("Failed to exchange difi oidc token to oidc token: " + e.message)
        }
        return ResponseEntity.status(HttpStatus.OK)
                .headers(tokenHeaders)
                .body(AccessTokenResponseService(oidcToken))
    }
}
