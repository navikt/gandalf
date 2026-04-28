package no.nav.gandalf.api.controllers

import io.micrometer.core.instrument.Timer
import io.swagger.v3.oas.annotations.Operation
import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.api.WSTrustRequest
import no.nav.gandalf.ldap.CustomAuthenticationProvider
import no.nav.gandalf.ldap.authenticate
import no.nav.gandalf.metric.ApplicationMetric
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("rest/v1/sts", consumes = ["text/xml"], produces = ["text/xml"])
class WSSAMLTokenController(
    val provider: CustomAuthenticationProvider,
    val issuer: AccessTokenIssuer,
) {
    @Operation(hidden = true)
    @PostMapping("/ws/samltoken", "/ws/samltoken/")
    fun getSAMLTokenWS(
        @RequestBody xmlRequest: String?,
    ): ResponseEntity<Any> {
        val sample = Timer.start(ApplicationMetric.meterRegistry())
        try {
            if (xmlRequest == null) {
                ApplicationMetric.wsSAMLTokenNotOk.increment()
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Body is empty")
            }
            // parse xml
            val wsReq = WSTrustRequest()
            try {
                wsReq.read(xmlRequest!!)
            } catch (e: Throwable) {
                ApplicationMetric.wsSAMLTokenNotOk.increment()
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: ${e.message}")
            }
            // check authorization
            try {
                // Bind to ldap
                provider.authenticate(wsReq.username, wsReq.password)
            } catch (e: Throwable) {
                ApplicationMetric.wsSAMLTokenNotOk.increment()
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized user: ${e.message}")
            }
            when {
                wsReq.isValidateSaml -> {
                    println("isValidateSaml")
                    return try {
                        val samlToken: String? = wsReq.validateTarget
                        issuer.validateSamlToken(samlToken)
                        ApplicationMetric.wsSAMLTokenOk.increment()
                        ResponseEntity.status(HttpStatus.OK).body(wsReq.getResponse(samlToken!!))
                    } catch (e: Throwable) {
                        ApplicationMetric.wsSAMLTokenNotOk.increment()
                        ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation failed" + e.message)
                    }
                }
                wsReq.isIssueSamlFromUNT -> {
                    println("isIssueSamlFromUNT")
                    return try {
                        val samlToken: String =
                            issuer.issueSamlToken(
                                wsReq.username!!,
                                wsReq.username,
                                AccessTokenIssuer.DEFAULT_SAML_AUTHLEVEL,
                            )
                        ApplicationMetric.wsSAMLTokenOk.increment()
                        ResponseEntity.status(HttpStatus.OK).body(wsReq.getResponse(samlToken))
                    } catch (e: Throwable) {
                        ApplicationMetric.wsSAMLTokenNotOk.increment()
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: ${e.message}")
                    }
                }
                wsReq.isExchangeOidcToSaml -> {
                    println("isExchangeOidcToSaml")
                    return try {
                        val samlToken: String = issuer.exchangeOidcToSamlToken(wsReq.decodedOidcToken!!, wsReq.username)
                        ApplicationMetric.wsSAMLTokenOk.increment()
                        ResponseEntity.status(HttpStatus.OK).body(wsReq.getResponse(samlToken))
                    } catch (e: Throwable) {
                        ApplicationMetric.wsExchangeOIDCTokenNotOk.increment()
                        ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.message)
                    }
                }
                else -> {
                    ApplicationMetric.wsSAMLTokenNotOk.increment()
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request is unsupported")
                }
            }
        } finally {
            sample.stop(ApplicationMetric.requestLatencyWSSAMLToken)
        }
    }
}
