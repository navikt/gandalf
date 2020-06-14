package no.nav.gandalf.api

import io.prometheus.client.Histogram
import io.swagger.v3.oas.annotations.Operation
import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.metric.ApplicationMetric
import no.nav.gandalf.config.LdapConfig
import no.nav.gandalf.model.User
import no.nav.gandalf.util.authenticate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("rest/v1/sts", consumes = ["text/xml"], produces = ["text/xml"])
class WSSAMLTokenController(
    @Autowired val ldapConfig: LdapConfig,
    @Autowired val issuer: AccessTokenIssuer
) {

    @Operation(hidden = true)
    @PostMapping("/ws/samltoken")
    fun getSAMLTokenWS(
        @RequestBody xmlRequest: String?
    ): ResponseEntity<Any> {
        val requestTimer: Histogram.Timer = ApplicationMetric.requestLatencyWSSAMLToken.startTimer()
        try {
            if (xmlRequest == null) {
                ApplicationMetric.wsSAMLTokenNotOk.inc()
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Body is empty")
            }
            // parse xml
            val wsReq = WSTrustRequest()
            try {
                wsReq.read(xmlRequest!!)
            } catch (e: Throwable) {
                ApplicationMetric.wsSAMLTokenNotOk.inc()
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: ${e.message}")
            }
            // check authorization
            try {
                // Bind to ldap
                if (wsReq.username == null || wsReq.password == null) {
                    throw RuntimeException("Missing username and/or password")
                } else {
                    authenticate(ldapConfig, User(wsReq.username!!, wsReq.password!!))
                }
            } catch (e: Throwable) {
                ApplicationMetric.wsSAMLTokenNotOk.inc()
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized user: ${e.message}")
            }
            when {
                wsReq.isValidateSaml -> {
                    println("isValidateSaml")
                    return try {
                        val samlToken: String? = wsReq.validateTarget
                        issuer.validateSamlToken(samlToken)
                        ApplicationMetric.wsSAMLTokenOk.inc()
                        ResponseEntity.status(HttpStatus.OK).body(wsReq.getResponse(samlToken!!))
                    } catch (e: Throwable) {
                        ApplicationMetric.wsSAMLTokenNotOk.inc()
                        ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation failed" + e.message)
                    }
                }
                wsReq.isIssueSamlFromUNT -> {
                    println("isIssueSamlFromUNT")
                    return try {
                        val samlToken: String = issuer.issueSamlToken(
                            wsReq.username!!,
                            wsReq.username,
                            AccessTokenIssuer.DEFAULT_SAML_AUTHLEVEL
                        )
                        ApplicationMetric.wsSAMLTokenOk.inc()
                        ResponseEntity.status(HttpStatus.OK).body(wsReq.getResponse(samlToken))
                    } catch (e: Throwable) {
                        ApplicationMetric.wsSAMLTokenNotOk.inc()
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: ${e.message}")
                    }
                }
                wsReq.isExchangeOidcToSaml -> {
                    println("isExchangeOidcToSaml")
                    return try {
                        val samlToken: String = issuer.exchangeOidcToSamlToken(wsReq.decodedOidcToken!!, wsReq.username)
                        ApplicationMetric.wsSAMLTokenOk.inc()
                        ResponseEntity.status(HttpStatus.OK).body(wsReq.getResponse(samlToken))
                    } catch (e: Throwable) {
                        ApplicationMetric.wsExchangeOIDCTokenNotOk.inc()
                        ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.message)
                    }
                }
                else -> {
                    ApplicationMetric.wsSAMLTokenNotOk.inc()
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request is unsupported")
                }
            }
        } finally {
            requestTimer.observeDuration()
        }
    }
}
