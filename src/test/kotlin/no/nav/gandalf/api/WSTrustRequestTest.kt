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
import org.junit.Ignore
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
        var oidcToken: String? = issuer.issueToken(username)!!.serialize()
        val xmlReq: String = getOidcToSamlRequest(username, password, oidcToken!!)
        val wsReq = WSTrustRequest()
        wsReq.read(xmlReq)
        oidcToken = wsReq.decodedOidcToken
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

    // From old STS, dont know really purpose, but cant delete
    @Test
    @Ignore
    fun test1() {
        val oidcToken = "eyJraWQiOiIxYzkzMGJkMi0zMDA4LTRjM2EtODE1NC04OTY4ZDBmNDNhMWEiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJzcnZzZWN1cml0eS10b2tlbi0iLCJhdWQiOlsic3J2c2VjdXJpdHktdG9rZW4tIiwicHJlcHJvZC5sb2NhbCJdLCJ2ZXIiOiIxLjAiLCJuYmYiOjE1NDA0NTk4MjgsImF6cCI6InNydnNlY3VyaXR5LXRva2VuLSIsImlkZW50VHlwZSI6IlN5c3RlbXJlc3N1cnMiLCJhdXRoX3RpbWUiOjE1NDA0NTk4MjgsImlzcyI6Imh0dHBzOlwvXC9zZWN1cml0eS10b2tlbi1zZXJ2aWNlLm5haXMucHJlcHJvZC5sb2NhbCIsImV4cCI6MTU0MDQ2MzQyOCwiaWF0IjoxNTQwNDU5ODI4LCJqdGkiOiI0YjBhMGY4MC01Mjg2LTQzMGUtYjI1Mi0wZGRlZWVkYWRiNjEifQ.UFIps1x2nrKzjFDJ_9hbgr_PqhFlAW8n2BYFlOIH4G4lMgWmHUWrGnss2eLKBKlNYq3aa2tZcCVaX9s5VSjSC7IofVOoGKVOU508_9Pxt8ld6hj3CQUbjLMdc9nOHi2bsOSGogaIwvbugEPBZ20xsnumkkv5TkfBYxHvwU0bXEH5u1vTW8XVMbvoil8I-SUDoSqbmBuytmv6DX95BOxRW4NvGFDcjj9jNp7fs9J3JDQnSrlosgM3zJMbidBjxbpX_To7B-6UypEvyaNoBDmZVAZWUIHhOi4VFvjByZ8ScFtSLJtrEPGaihdjYjdDmbGu_jMtO2LrPQb8aVJuI47yWg"
        val req: String = getOidcToSamlRequest(username, password, oidcToken)
        println("wsReqOidcToSaml: $req")
    }
}
