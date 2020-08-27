package no.nav.gandalf.api

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
import no.nav.gandalf.api.Util.Companion.tokenHeaders
import no.nav.gandalf.api.Util.Companion.unauthorizedResponse
import no.nav.gandalf.api.Util.Companion.userDetails
import no.nav.gandalf.model.ErrorDescriptiveResponse
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
@Tag(
    name = "OIDC/SAML Token Validation",
    description = "Validate tokens, SAML & OIDC (Datapower, IDP & IDP, AZURE, OPENAM)"
)
class ValidateController {

    @Autowired
    private lateinit var issuer: AccessTokenIssuer

    @Operation(summary = "Validate SAML Token", security = [SecurityRequirement(name = "BasicAuth")], hidden = true)
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Validated Response",
                content = [
                    (
                        Content(
                            mediaType = "application/json",
                            schema = Schema(implementation = Validation::class)
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
                responseCode = "500",
                description = INTERNAL_SERVER_ERROR,
                content = [Content()]
            )
        ]
    )
    @PostMapping("/samltoken/validate")
    fun validateSAMLToken(
        @Parameter(description = "Base64Encoded SAML Token to Validate", required = true)
        @RequestParam("token", required = true) samlToken: String
    ): ResponseEntity<Any> {
        userDetails() ?: return unauthorizedResponse(Throwable(), "Unauthorized")
        log.info("Validate SAML token")
        return try {
            val samlObject =
                issuer.validateSamlToken(String(Base64.decodeBase64(samlToken.toByteArray()), StandardCharsets.UTF_8))
            ResponseEntity
                .status(HttpStatus.OK)
                .headers(tokenHeaders)
                .body(Validation(true, samlObject.toString()))
        } catch (e: Throwable) {
            val errorMessage = e.message ?: ""
            log.error(e) { errorMessage }
            ResponseEntity
                .status(HttpStatus.OK)
                .headers(tokenHeaders)
                .body(Validation(false, errorMessage))
        }
    }

    @Operation(summary = "Validate OIDC Token", security = [SecurityRequirement(name = "BasicAuth")], hidden = true)
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Validated Response",
                content = [
                    (
                        Content(
                            mediaType = "application/json",
                            schema = Schema(implementation = Validation::class)
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
                responseCode = "500",
                description = INTERNAL_SERVER_ERROR,
                content = [Content()]
            )
        ]
    )
    @PostMapping("/token/validate")
    fun validateOIDCToken(
        @Parameter(description = "Base64Encoded OIDC Token to Validate", required = true)
        @RequestParam("token", required = true) oidcToken: String?
    ): ResponseEntity<Any> {
        requireNotNull(userDetails()) { return unauthorizedResponse(Throwable(), "Unauthorized") }
        log.info("Validate oidc token")
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
