package no.nav.gandalf.api

import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.model.ConfigurationResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private const val TOKEN_PATH = "/rest/v1/sts/token"
private const val EXCHANGE_PATH = "/rest/v1/sts/token/exchange"
private const val JWKS_PATH = "/rest/v1/sts/jwks"

@RestController
@RequestMapping("/", produces = ["application/json"])
class IdentityProviderController {

    @Autowired
    private lateinit var accessTokenIssuer: AccessTokenIssuer

    @GetMapping("/jwks")
    fun getKeys(): ResponseEntity<Any> {
        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(tokenHeaders)
                .body(accessTokenIssuer.getPublicJWKSet()!!.toJSONObject())
    }

    @GetMapping("/.well-known/openid-configuration")
    fun getConfiguration(): ResponseEntity<Any> {
        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(tokenHeaders)
                .body(ConfigurationResponse(accessTokenIssuer.issuer, TOKEN_PATH, EXCHANGE_PATH, JWKS_PATH))
    }
}
