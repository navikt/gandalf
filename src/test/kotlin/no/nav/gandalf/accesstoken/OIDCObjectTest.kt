package no.nav.gandalf.accesstoken

import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.SignedJWT
import java.util.Date
import no.nav.gandalf.service.RSAKeyStoreService
import no.nav.gandalf.utils.compare
import no.nav.gandalf.utils.getAlteredOriginalToken
import no.nav.gandalf.utils.getOriginalJwkSet
import no.nav.gandalf.utils.getOriginalToken
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
class OIDCObjectTest {

    @Autowired
    private lateinit var tokenIssuer: AccessTokenIssuer

    @Autowired
    private lateinit var rsaKeyStoreService: RSAKeyStoreService

    @Before
    fun init() {
        rsaKeyStoreService.resetRepository()
    }

    @Test
    fun `Compare Generated Token To Original Token ver1`() {
        try {
            // gammelt token
            val jwtOriginal = SignedJWT.parse(getOriginalToken()).jwtClaimsSet
            val username = jwtOriginal.subject
            // test constructor
            // get token based on values from original token for comparison
            val oidcObj = OidcObject(AccessTokenIssuer.toZonedDateTime(jwtOriginal.issueTime), AccessTokenIssuer.OIDC_DURATION_TIME)
            oidcObj.id = jwtOriginal.jwtid
            oidcObj.subject = username
            oidcObj.issuer = jwtOriginal.issuer
            oidcObj.version = AccessTokenIssuer.OIDC_VERSION
            oidcObj.setAudience(username, AccessTokenIssuer.getDomainFromIssuerURL(jwtOriginal.issuer))
            oidcObj.azp = username
            oidcObj.resourceType = AccessTokenIssuer.getIdentType(username)
            val token = oidcObj.getSignedToken(getCurrentRSAKey()!!, AccessTokenIssuer.OIDC_SIGNINGALG)
            val jwt = token.jwtClaimsSet
            compare(jwtOriginal, jwt)
        } catch (e: Exception) {
            fail("Error: " + e.message)
        }
    }

    @Test
    fun `Compare Generated Token To Original Token ver2`() {
        try {
            // Gammelt token
            val originalToken = getOriginalToken()
            val jwtOriginal = SignedJWT.parse(originalToken).jwtClaimsSet
            val username = jwtOriginal.subject
            // Use test constructor
            // get token based on values from original token for comparison
            var oidcObj = OidcObject(AccessTokenIssuer.toZonedDateTime(jwtOriginal.issueTime), AccessTokenIssuer.toZonedDateTime(jwtOriginal.expirationTime))
            oidcObj.id = jwtOriginal.jwtid
            oidcObj.subject = username
            oidcObj.issuer = jwtOriginal.issuer
            oidcObj.version = AccessTokenIssuer.OIDC_VERSION
            oidcObj.setAudience(username, AccessTokenIssuer.getDomainFromIssuerURL(jwtOriginal.issuer))
            oidcObj.azp = username
            oidcObj.resourceType = AccessTokenIssuer.getIdentType(username)
            var token = oidcObj.getSignedToken(getCurrentRSAKey()!!, AccessTokenIssuer.OIDC_SIGNINGALG)
            var jwt = token.jwtClaimsSet
            compare(jwtOriginal, jwt)
            // Use test constructor
            oidcObj = OidcObject(originalToken)
            token = oidcObj.getSignedToken(getCurrentRSAKey()!!, AccessTokenIssuer.OIDC_SIGNINGALG)
            jwt = token.jwtClaimsSet
            compare(jwtOriginal, jwt)
        } catch (e: Exception) {
            fail("Error: " + e.message)
        }
    }

    @Test
    fun `Compare Generated Token To Original Token ver3`() {
        try {
            // gammelt token
            val originalToken = getOriginalToken()
            val jwtOriginal = SignedJWT.parse(originalToken).jwtClaimsSet
            // test constructor
            // get token based on values from original token for comparison
            val oidcObj = OidcObject(originalToken)
            val token = oidcObj.getSignedToken(getCurrentRSAKey()!!, AccessTokenIssuer.OIDC_SIGNINGALG)
            val jwt = token.jwtClaimsSet
            compare(jwtOriginal, jwt)
        } catch (e: Exception) {
            fail("Error: " + e.message)
        }
    }

    @Test
    fun `Validate Valid Token`() {
        try {
            val originalToken = getOriginalToken()
            val oidcObj = OidcObject(originalToken)
            oidcObj.getSignedToken(getCurrentRSAKey()!!, AccessTokenIssuer.OIDC_SIGNINGALG)
            val jwk = getOriginalJwkSet().getKeyByKeyId(oidcObj.keyId) as RSAKey
            oidcObj.validate(tokenIssuer.issuer, oidcObj.issueTime, jwk) // now is IssueTime
        } catch (e: Exception) {
            fail("Error: " + e.message)
        }
    }

    @Test
    fun `Validate Unknown Issuer`() {
        try {
            val originalToken = getOriginalToken()
            val oidcObj = OidcObject(originalToken)
            oidcObj.issuer = "tull"
            oidcObj.getSignedToken(getCurrentRSAKey()!!, AccessTokenIssuer.OIDC_SIGNINGALG)
            oidcObj.validate(tokenIssuer, oidcObj.issueTime) // now is IssueTime
        } catch (e: Exception) {
            assertTrue(e.message!!.indexOf("issuer") >= 0)
        }
    }

    @Test
    fun `Validate - Has Expired`() {
        try {
            val originalToken = getOriginalToken()
            val oidcObj = OidcObject(originalToken)
            oidcObj.getSignedToken(getCurrentRSAKey()!!, AccessTokenIssuer.OIDC_SIGNINGALG)
            val jwk = getOriginalJwkSet().getKeyByKeyId(oidcObj.keyId) as RSAKey
            oidcObj.validate(tokenIssuer, jwk) // now is after expirationTime
        } catch (e: Exception) {
            assertTrue(e.message!!.indexOf("expired") >= 0)
        }
    }

    @Test
    fun `Validate - Is Not Before`() {
        try {
            val originalToken = getOriginalToken()
            val oidcObj = OidcObject(originalToken)
            oidcObj.getSignedToken(getCurrentRSAKey()!!, AccessTokenIssuer.OIDC_SIGNINGALG)
            val jwk = getOriginalJwkSet().getKeyByKeyId(oidcObj.keyId) as RSAKey
            oidcObj.validate(tokenIssuer.issuer, Date(oidcObj.notBeforeTime!!.time - 2), jwk) // now is before notBeforeTime
        } catch (e: Exception) {
            assertTrue(e.message!!.indexOf("notBeforeTime") >= 0)
        }
    }

    @Test
    fun `Validate - Unknown Key`() {
        try {
            val originalToken = getAlteredOriginalToken()
            val oidcObj = OidcObject(originalToken)
            // now is IssueTime
            oidcObj.validate(tokenIssuer, oidcObj.issueTime)
        } catch (e: Exception) {
            println(e.message)
            assertTrue(e.message!!.indexOf("Failed to get keys from by issuer") >= 0)
        }
    }

    @Test
    fun `Validate - Original Token`() {
        try {
            val alteredToken = getOriginalToken()
            val oidcObj = OidcObject(alteredToken)
            val jwk = getOriginalJwkSet().getKeyByKeyId(oidcObj.keyId) as RSAKey
            oidcObj.validate(tokenIssuer.issuer, oidcObj.issueTime, jwk) // now is IssueTime
        } catch (e: Exception) {
            fail("Error: " + e.message)
        }
    }

    @Test
    fun `Validate Altered Token`() {
        try {
            val alteredToken = getAlteredOriginalToken()
            val oidcObj = OidcObject(alteredToken)
            val jwk = getOriginalJwkSet().getKeyByKeyId(oidcObj.keyId) as RSAKey
            oidcObj.validate(tokenIssuer.issuer, oidcObj.issueTime, jwk) // now is IssueTime
        } catch (e: Exception) {
            assertTrue(e.message!!.indexOf("Signature validation failed") >= 0)
        }
    }

    private fun getCurrentRSAKey(): RSAKey? {
        return rsaKeyStoreService.currentRSAKey
    }
}
