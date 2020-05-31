package no.nav.gandalf.api

import mu.KotlinLogging
import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.config.LdapConfig
import no.nav.gandalf.ldap.Ldap
import no.nav.gandalf.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger { }

@RestController
@RequestMapping("rest/v1/sts", consumes = ["text/xml"], produces = ["text/xml"])
class WSSAMLTokenController(
    @Autowired val ldapConfig: LdapConfig,
    @Autowired val issuer: AccessTokenIssuer
) {

    @PostMapping("/ws/samltoken")
    fun getSAMLTokenWS(
        xmlRequest: String?
    ): ResponseEntity<Any> {
        // parse xml
        val wsReq = WSTrustRequest()
        try {
            wsReq.read(xmlRequest!!)
        } catch (e: Throwable) {
            badRequestResponse("Error: ${e.message}")
        }
        // check authorization
        try {
            // Bind to ldap
            if (wsReq.username == null || wsReq.password == null) {
                throw RuntimeException("Missing username and/or password")
            } else {
                Ldap(ldapConfig).result(User(wsReq.username!!, wsReq.password!!))
            }
        } catch (e: Throwable) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized user: ${e.message}")
        }
        when {
            wsReq.isValidateSaml -> {
                return try {
                    val samlToken: String? = wsReq.validateTarget
                    issuer.validateSamlToken(samlToken)
                    ResponseEntity.status(HttpStatus.OK).body(wsReq.getResponse(samlToken!!))
                } catch (e: Throwable) {
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation failed" + e.message)
                }
            }
            wsReq.isIssueSamlFromUNT -> {
                return try {
                    val samlToken: String = issuer.issueSamlToken(
                        wsReq.username!!,
                        wsReq.username,
                        AccessTokenIssuer.DEFAULT_SAML_AUTHLEVEL
                    )
                    ResponseEntity.status(HttpStatus.OK).body(wsReq.getResponse(samlToken))
                } catch (e: Throwable) {
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: ${e.message}")
                }
            }
            wsReq.isExchangeOidcToSaml -> {
                return try {
                    val samlToken: String = issuer.exchangeOidcToSamlToken(wsReq.decodedOidcToken!!, wsReq.username)
                    ResponseEntity.status(HttpStatus.OK).body(wsReq.getResponse(samlToken))
                } catch (e: Throwable) {
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.message)
                }
            }
            else -> return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request is unsupported")
        }
    }
}
