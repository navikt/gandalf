package no.nav.gandalf.api

import mu.KotlinLogging
import no.nav.gandalf.accesstoken.AccessTokenIssuer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger { }

@RestController
@RequestMapping("v1/sts", consumes = ["text/xml"], produces = ["text/xml"])
class WSSAMLTokenController {

    @Autowired
    private lateinit var issuer: AccessTokenIssuer

    @PostMapping("/ws/samltoken")
    fun getSAMLTokenWS(
        xmlRequest: String?
    ): ResponseEntity<Any> {
        // parse xml
        val wsReq = WSTrustRequest()
        try {
            wsReq.read(xmlRequest!!)
        } catch (e: Exception) {
            badRequestResponse("Error: ${e.message}")
        }
        // check authorization
        try {
            // Bind to ldap
            if (wsReq.username == null && wsReq.password == null) {
                throw RuntimeException()
            }
        } catch (e: Exception) {
            return unauthorizedResponse(e, "Unauthorized user: " + wsReq.username)
        }
        when {
            wsReq.isValidateSaml -> {
                return try {
                    val samlToken: String? = wsReq.validateTarget
                    issuer.validateSamlToken(samlToken)
                    ResponseEntity.status(HttpStatus.OK).body(wsReq.getResponse(samlToken!!))
                } catch (e: Exception) {
                    badRequestResponse("Validation failed" + e.message)
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
                } catch (e: Exception) {
                    serverErrorResponse(e)
                }
            }
            wsReq.isExchangeOidcToSaml -> {
                return try {
                    val samlToken: String = issuer.exchangeOidcToSamlToken(wsReq.decodedOidcToken!!, wsReq.username)
                    ResponseEntity.status(HttpStatus.OK).body(wsReq.getResponse(samlToken))
                } catch (e: Exception) {
                    badRequestResponse("Error: " + e.message)
                }
            }
            else -> return badRequestResponse("Request is unsupported")
        }
    }
}
