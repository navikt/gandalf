package no.nav.gandalf.api

import io.prometheus.client.CollectorRegistry
import no.nav.gandalf.TestKeySelector
import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.accesstoken.SamlObject
import no.nav.gandalf.utils.getOidcToSamlRequest
import no.nav.gandalf.utils.getSamlRequest
import no.nav.gandalf.utils.getValidateSamlRequest
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.junit.jupiter.api.fail
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
@EnableConfigurationProperties
@DirtiesContext
@ActiveProfiles("test")
class WSTrustRequestTest {

    @Autowired
    private lateinit var issuer: AccessTokenIssuer

    @Autowired
    private lateinit var testKeySelector: TestKeySelector

    private val username = "srvsecurity-token-"
    private val password = "tull"

    @After
    fun clear() {
        CollectorRegistry.defaultRegistry.clear()
    }

    @Test
    fun `SAML Request`() {
        val xmlReq: String = getSamlRequest(username, password)
        val wsReq = WSTrustRequest()
        wsReq.read(xmlReq)
        Assert.assertTrue(wsReq.isIssueSamlFromUNT)
    }

    @Test
    @Throws(Exception::class)
    fun `OIDC To SAML Request`() {
        val oidcToken: String? = issuer.issueToken(username).serialize()
        val xmlReq: String = getOidcToSamlRequest(username, password, oidcToken!!)
        val wsReq = WSTrustRequest()
        wsReq.read(xmlReq)
        // oidcToken = wsReq.decodedOidcToken
        // val signedJWT = SignedJWT.parse(oidcToken)
        // val claimSet = signedJWT.jwtClaimsSet
        Assert.assertTrue(wsReq.isExchangeOidcToSaml)
    }

    @Test
    @Throws(Exception::class)
    fun `Validate SAML Request`() {
        var samlToken: String? = issuer.issueSamlToken(username, username, "0")
        val xmlReq: String = getValidateSamlRequest(username, password, samlToken!!)
        println("###getValidateSamlRequest: \n$xmlReq")
        val wsReq = WSTrustRequest()
        wsReq.read(xmlReq)
        Assert.assertTrue(wsReq.isValidateSaml)
        try {
            samlToken = wsReq.validateTarget
            issuer.validateSamlToken(samlToken, testKeySelector)
            val samlObj = SamlObject()
            samlObj.read(samlToken)
        } catch (e: Exception) {
            fail(e.message)
        }
    }
}
