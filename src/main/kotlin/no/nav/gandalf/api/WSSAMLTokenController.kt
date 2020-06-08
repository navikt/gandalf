package no.nav.gandalf.api

import io.prometheus.client.Histogram
import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.api.metric.ApplicationMetric
import no.nav.gandalf.api.metric.ApplicationMetric.wsExchangeOIDCTokenNotOk
import no.nav.gandalf.api.metric.ApplicationMetric.wsSAMLTokenNotOk
import no.nav.gandalf.api.metric.ApplicationMetric.wsSAMLTokenOk
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

    @PostMapping("/ws/samltoken")
    fun getSAMLTokenWS(
        @RequestBody xmlRequest: String?
    ): ResponseEntity<Any> {
        val requestTimer: Histogram.Timer = ApplicationMetric.requestLatencyWSSAMLToken.startTimer()
        try {
            if (xmlRequest == null) {
                wsSAMLTokenNotOk.inc()
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Body is empty")
            }
            // parse xml
            val wsReq = WSTrustRequest()
            try {
                wsReq.read(xmlRequest!!)
            } catch (e: Throwable) {
                wsSAMLTokenNotOk.inc()
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
                wsSAMLTokenNotOk.inc()
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized user: ${e.message}")
            }
            when {
                wsReq.isValidateSaml -> {
                    println("isValidateSaml")
                    return try {
                        val samlToken: String? = wsReq.validateTarget
                        issuer.validateSamlToken(samlToken)
                        wsSAMLTokenOk.inc()
                        ResponseEntity.status(HttpStatus.OK).body(wsReq.getResponse(samlToken!!))
                    } catch (e: Throwable) {
                        wsSAMLTokenNotOk.inc()
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
                        wsSAMLTokenOk.inc()
                        ResponseEntity.status(HttpStatus.OK).body(wsReq.getResponse(samlToken))
                    } catch (e: Throwable) {
                        wsSAMLTokenNotOk.inc()
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: ${e.message}")
                    }
                }
                wsReq.isExchangeOidcToSaml -> {
                    println("isExchangeOidcToSaml")
                    return try {
                        val samlToken: String = issuer.exchangeOidcToSamlToken(wsReq.decodedOidcToken!!, wsReq.username)
                        wsSAMLTokenOk.inc()
                        ResponseEntity.status(HttpStatus.OK).body(wsReq.getResponse(samlToken))
                    } catch (e: Throwable) {
                        wsExchangeOIDCTokenNotOk.inc()
                        ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.message)
                    }
                }
                else -> {
                    wsSAMLTokenNotOk.inc()
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request is unsupported")
                }
            }
        } finally {
            requestTimer.observeDuration()
        }
    }
}
