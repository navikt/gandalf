package no.nav.gandalf.api

import mu.KotlinLogging
import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.model.Validation
import org.apache.commons.codec.binary.Base64
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets

private val log = KotlinLogging.logger { }

@RestController
@RequestMapping("rest/v1/sts", produces = ["application/json"])
class ValidateController {

    @Autowired
    private lateinit var issuer: AccessTokenIssuer

    @PostMapping("/samltoken/validate")
    fun validateSAMLToken(
        @RequestParam("token") samlToken: String
    ): ResponseEntity<Any> {
        userDetails() ?: return unauthorizedResponse(Throwable(), "Unauthorized")
        log.debug("Validate SAML token")
        return try {
            val samlObject =
                issuer.validateSamlToken(String(Base64.decodeBase64(samlToken.toByteArray()), StandardCharsets.UTF_8))
            ResponseEntity
                .status(HttpStatus.OK)
                .headers(tokenHeaders)
                .body(Validation(true, samlObject.toString()))
        } catch (e: Throwable) {
            val errorMessage = "Validation failed: " + e.message
            log.error(e) { errorMessage }
            ResponseEntity
                .status(HttpStatus.OK)
                .headers(tokenHeaders)
                .body(Validation(false, errorMessage))
        }
    }

    @PostMapping("/token/validate")
    fun validateOIDCToken(
        @RequestParam("token") oidcToken: String?
    ): ResponseEntity<Any> {
        userDetails() ?: return unauthorizedResponse(Throwable(), "Unauthorized")
        log.debug("Validate oidc token")
        return try {
            val oidcObject = issuer.validateOidcToken(oidcToken)
            ResponseEntity
                .status(HttpStatus.OK)
                .headers(tokenHeaders)
                .body(Validation(true, oidcObject.issuer!!))
        } catch (e: Throwable) {
            val errorMessage = "Validation failed: " + e.message
            log.error(e) { errorMessage }
            ResponseEntity
                .status(HttpStatus.OK)
                .headers(tokenHeaders)
                .body(Validation(false, errorMessage))
        }
    }
}
