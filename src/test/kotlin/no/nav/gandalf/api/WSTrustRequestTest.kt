package no.nav.gandalf.api

import com.nimbusds.jwt.SignedJWT
import io.prometheus.client.CollectorRegistry
import no.nav.gandalf.TestKeySelector
import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.accesstoken.saml.SamlObject
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
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
@EnableConfigurationProperties
@AutoConfigureWireMock(port = 0)
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
    fun `WS FROM UNT to SAML Request`() {
        val xmlReq: String = getSamlRequest(username, password)
        val wsReq = WSTrustRequest()
        wsReq.read(xmlReq)
        Assert.assertTrue(wsReq.isIssueSamlFromUNT)
    }

    @Test
    @Throws(Exception::class)
    fun `WS - FROM OIDC To SAML Request`() {
        val oidcToken: String = issuer.issueToken(username).serialize()
        val xmlReq: String = getOidcToSamlRequest(username, password, oidcToken)
        val wsReq = WSTrustRequest()
        wsReq.read(xmlReq)
        Assert.assertTrue(wsReq.isExchangeOidcToSaml)

        val decodedOidc = wsReq.decodedOidcToken
        Assert.assertTrue(decodedOidc != null)
        val signedJWT = SignedJWT.parse(decodedOidc)
        val claimSet = signedJWT.jwtClaimsSet
        println(claimSet)
    }

    @Test
    @Throws(Exception::class)
    fun `Issue and validate SAML Request`() {
        var samlToken: String = issuer.issueSamlToken(username, username, "0")
        val xmlReq: String = getValidateSamlRequest(username, password, samlToken)
        println("###getValidateSamlRequest: \n$xmlReq")
        val wsReq = WSTrustRequest()
        wsReq.read(xmlReq)
        Assert.assertTrue(wsReq.isValidateSaml)
        try {
            samlToken = wsReq.validateTarget!!
            issuer.validateSamlToken(samlToken, testKeySelector)
            val samlObj = SamlObject()
            samlObj.read(samlToken)
        } catch (e: Exception) {
            fail(e.message)
        }
    }
}
