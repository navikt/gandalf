package no.nav.gandalf.api

import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.model.ConfigurationResponse
import no.nav.gandalf.model.toExchangePath
import no.nav.gandalf.model.toJwksPath
import no.nav.gandalf.model.toTokenPath
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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

    @GetMapping("/v1/sts/jwks")
    fun getDeprecatedKeys(): ResponseEntity<Any> {
        return getKeys()
    }

    @GetMapping("/.well-known/openid-configuration")
    fun getConfiguration(): ResponseEntity<Any> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .headers(tokenHeaders)
            .body(
                ConfigurationResponse(
                    issuer = accessTokenIssuer.issuer,
                    token_endpoint = toTokenPath(accessTokenIssuer.issuer),
                    exchange_token_endpoint = toExchangePath(accessTokenIssuer.issuer),
                    jwks_uri = toJwksPath(accessTokenIssuer.issuer),
                    subject_types_supported = listOf("public")
                )
            )
    }

    @GetMapping("/v1/sts/.well-known/openid-configuration")
    fun getDeprecatedConfiguration(): ResponseEntity<Any> {
        return getConfiguration()
    }
}
