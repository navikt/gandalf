package no.nav.gandalf.controller

import mu.KotlinLogging
import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.model.ErrorResponse
import no.nav.gandalf.service.AccessTokenResponseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger { }

@RestController
@RequestMapping("v1/sts")
class AccessTokenController {

    @Autowired
    lateinit var issuer: AccessTokenIssuer

    @GetMapping("/token", produces = ["application/json"])
    fun getOIDCToken(
        @RequestParam("grant_type", required = true) grantType: String,
        @RequestParam("scope", required = true) scope: String
    ): ResponseEntity<Any> {
        when {
            !grantType.equals("client_credentials", ignoreCase = true) || !scope.equals("openid", ignoreCase = true) -> {
                log.warn("Invalid request, grant_type= $grantType scope $scope")
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse("invalid_request"))
            }
            else -> {
                val user = try {
                    authDetails().name
                } catch (e: Exception) {
                    log.error("Error, invalid_client: " + e.message)
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse("invalid_client"))
                }

                val oidcToken = try {
                    issuer.issueToken(user)
                } catch (e: Exception) {
                    log.error("Error: " + e.message)
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse("Internal server error, teknisk feil"))
                }

                val headers = HttpHeaders().apply {
                    add("Cache-Control", "no-store")
                    add("Pragma", "no-cache")
                }
                return ResponseEntity.status(HttpStatus.OK).headers(headers).body(AccessTokenResponseService(oidcToken!!).tokenResponse)
            }
        }
    }

    fun authDetails() = SecurityContextHolder.getContext().authentication
}
