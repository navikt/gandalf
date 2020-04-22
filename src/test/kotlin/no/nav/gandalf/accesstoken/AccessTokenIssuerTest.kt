package no.nav.gandalf.accesstoken

import junit.framework.TestCase
import no.nav.gandalf.accesstoken.AccessTokenIssuer.Companion.OIDC_DURATION_TIME
import no.nav.gandalf.accesstoken.AccessTokenIssuer.Companion.OIDC_VERSION
import no.nav.gandalf.accesstoken.OidcObject.Companion.AUTHTIME_CLAIM
import no.nav.gandalf.accesstoken.OidcObject.Companion.AZP_CLAIM
import no.nav.gandalf.accesstoken.OidcObject.Companion.RESOURCETYPE_CLAIM
import no.nav.gandalf.accesstoken.OidcObject.Companion.VERSION_CLAIM
import no.nav.gandalf.service.AccessTokenResponseService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit4.SpringRunner

private const val ACCESS_TOKEN_TYPE = "bearer"

@RunWith(SpringRunner::class)
@DataJpaTest
class AccessTokenIssuerTest {

    @Autowired
    lateinit var issuer: AccessTokenIssuer

    @Before
    fun setUpTests() {
        // fikk problemer med systemproperty med bindestrek. Disse blir ikke satt før etter bean creation ved nais bygg...? Må derfor sette denne eksplisitt for at testen skal kunne kjøres
        // issuer!!.issuer = applicationEnv.issuer
        // issuer.issuerSrvUser = PropertyUtil.get(SRVSTS_USERNAME);
        // keyStoreTestExt!!.initKeyStoreLock()
        // keyStore.resetCache()
    }

    @Test
    @Throws(Exception::class)
    fun `'Token validation' issue and validate OIDC token`() {
        val username = "testuser"
        val token = issuer.issueToken(username)
        TestCase.assertNotNull(token)
        val jwt = token!!.jwtClaimsSet
        TestCase.assertEquals(jwt.subject, username)
        TestCase.assertEquals(jwt.getClaim(AZP_CLAIM), username)
        TestCase.assertEquals(jwt.issuer, issuer.issuer)
        TestCase.assertEquals(jwt.getStringClaim(VERSION_CLAIM), OIDC_VERSION)
        TestCase.assertTrue(jwt.jwtid != null)
        TestCase.assertTrue(jwt.getClaim(RESOURCETYPE_CLAIM) == AccessTokenIssuer.IdentType.INTERNBRUKER.value)

        // sjekk audience
        val l = token.jwtClaimsSet.audience
        TestCase.assertEquals(2, l.size)
        TestCase.assertEquals(l[0], username)
        TestCase.assertEquals("preprod.local", l[1])

        // sjekk time settings
        TestCase.assertEquals(0, jwt.notBeforeTime.compareTo(jwt.issueTime))
        TestCase.assertEquals(jwt.getLongClaim(AUTHTIME_CLAIM) as Long, jwt.issueTime.time / 1000)
        TestCase.assertEquals((jwt.expirationTime.time - jwt.issueTime.time) / 1000, OIDC_DURATION_TIME)

        // Test response
        val (_, token_type, expires_in) = AccessTokenResponseService(oidcToken = token).tokenResponse
        TestCase.assertTrue(expires_in == OIDC_DURATION_TIME)
        TestCase.assertTrue(token_type.equals(ACCESS_TOKEN_TYPE, ignoreCase = true))

        // test validation
        try {
            issuer.validateOidcToken(token.serialize())
            TestCase.assertTrue(true)
        } catch (e: Exception) {
            println("Error: " + e.message)
            TestCase.assertTrue(false)
        }
    }
}