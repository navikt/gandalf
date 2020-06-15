package no.nav.gandalf.api

import io.prometheus.client.Histogram
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.api.Util.Companion.tokenHeaders
import no.nav.gandalf.metric.ApplicationMetric
import no.nav.gandalf.model.ConfigurationResponse
import no.nav.gandalf.model.Keys
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
@RequestMapping(produces = ["application/json"])
@Tag(name = "Identity Provider Metadata", description = "Retrieve metadata")
class IdentityProviderController {

    @Autowired
    private lateinit var accessTokenIssuer: AccessTokenIssuer

    @Operation(
        summary = "The JSON Web Key Set (JWKS)",
        description = "The JSON Web Key Set (JWKS) is a set of keys which contains the public keys used to verify any JSON Web Token (JWT) issued by the authorization server and signed using the RS256 signing algorithm."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "JWKS Keys",
                content = [
                    (
                        Content(
                            mediaType = "application/json",
                            schema = Schema(implementation = Keys::class)
                        )
                        )
                ]
            ),
            ApiResponse(
                responseCode = "500",
                description = INTERNAL_SERVER_ERROR,
                content = [Content()]
            )
        ]
    )
    @GetMapping("/jwks")
    fun getKeys(): ResponseEntity<Any> {
        val requestTimer: Histogram.Timer = ApplicationMetric.requestLatencyJwks.startTimer()
        try {
            return ResponseEntity
                .status(HttpStatus.OK)
                .headers(tokenHeaders)
                .body(accessTokenIssuer.getPublicJWKSet()!!.toJSONObject())
        } finally {
            requestTimer.observeDuration()
        }
    }

    @Operation(summary = "The JSON Web Key Set (JWKS)", deprecated = true)
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "JWKS Keys",
                content = [
                    (
                        Content(
                            mediaType = "application/json",
                            schema = Schema(implementation = Keys::class)
                        )
                        )
                ]
            ),
            ApiResponse(
                responseCode = "500",
                description = INTERNAL_SERVER_ERROR,
                content = [Content()]
            )
        ]
    )
    @GetMapping("rest/v1/sts/jwks")
    fun getDeprecatedKeys(): ResponseEntity<Any> {
        return getKeys()
    }

    @Operation(summary = "Discovery endpoint can be used to retrieve metadata.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Metadata",
                content = [
                    (
                        Content(
                            mediaType = "application/json",
                            schema = Schema(implementation = ConfigurationResponse::class)
                        )
                        )
                ]
            ),
            ApiResponse(
                responseCode = "500",
                description = INTERNAL_SERVER_ERROR,
                content = [Content()]
            )
        ]
    )
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

    @Operation(summary = "Discovery endpoint can be used to retrieve metadata.", deprecated = true)
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Meatadata",
                content = [
                    (
                        Content(
                            mediaType = "application/json",
                            schema = Schema(implementation = ConfigurationResponse::class)
                        )
                        )
                ]
            ),
            ApiResponse(
                responseCode = "500",
                description = INTERNAL_SERVER_ERROR,
                content = [Content()]
            )
        ]
    )
    @GetMapping("rest/v1/sts/.well-known/openid-configuration")
    fun getDeprecatedConfiguration(): ResponseEntity<Any> {
        return getConfiguration()
    }
}
