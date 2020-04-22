package no.nav.gandalf.accesstoken

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.SignedJWT
import no.nav.gandalf.keystore.RSAKeyStoreRepositoryImpl
import no.nav.gandalf.service.RSAKeyStoreService
import no.nav.gandalf.utils.compare
import no.nav.gandalf.utils.getAlteredOriginalToken
import no.nav.gandalf.utils.getOriginalJwkSet
import no.nav.gandalf.utils.getOriginalToken
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.security.NoSuchAlgorithmException
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest
class OidcObjectTest {

    @Autowired
    private lateinit var tokenIssuer: AccessTokenIssuer

    @Autowired
    private lateinit var rsaKeyStoreService: RSAKeyStoreService

    @Autowired
    private lateinit var rsaKeyStoreRepositoryImpl: RSAKeyStoreRepositoryImpl


    @Before
    fun init() {
        rsaKeyStoreRepositoryImpl.lockKeyStore(test = true)
        rsaKeyStoreService.resetCache()
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
            val jwt = token!!.jwtClaimsSet
            compare(jwtOriginal, jwt)
        } catch (e: Exception) {
            println("Error: " + e.message)
            Assert.assertTrue(false)
        }
    }

    @Test
    fun `Compare Generated Token To Original Token ver2`() {
        try {
            // gammelt token
            val originalToken = getOriginalToken()
            val jwtOriginal = SignedJWT.parse(originalToken).jwtClaimsSet
            val username = jwtOriginal.subject
            // test constructor
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
            var jwt = token!!.jwtClaimsSet
            compare(jwtOriginal, jwt)
            // test constructor
            oidcObj = OidcObject(originalToken)
            token = oidcObj.getSignedToken(getCurrentRSAKey()!!, AccessTokenIssuer.OIDC_SIGNINGALG)
            jwt = token!!.jwtClaimsSet
            compare(jwtOriginal, jwt)
        } catch (e: Exception) {
            println("Error: " + e.message)
            Assert.assertTrue(false)
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
            val jwt = token!!.jwtClaimsSet
            compare(jwtOriginal, jwt)
        } catch (e: Exception) {
            println("Error: " + e.message)
            Assert.assertTrue(false)
        }
    }

    @Test
    @Throws(NoSuchAlgorithmException::class, JOSEException::class)
    fun `Validate Valid Token`() {
        try {
            val originalToken = getOriginalToken()
            val oidcObj = OidcObject(originalToken)
            oidcObj.getSignedToken(getCurrentRSAKey()!!, AccessTokenIssuer.OIDC_SIGNINGALG)
            oidcObj.validate(tokenIssuer, oidcObj.issueTime) // now is IssueTime
        } catch (e: Exception) {
            Assert.assertTrue(false)
        }
    }

    @Test
    @Throws(NoSuchAlgorithmException::class, JOSEException::class)
    fun `Validate Unknown Issuer`() {
        try {
            val originalToken = getOriginalToken()
            val oidcObj = OidcObject(originalToken)
            oidcObj.issuer = "tull"
            oidcObj.getSignedToken(getCurrentRSAKey()!!, AccessTokenIssuer.OIDC_SIGNINGALG)
            oidcObj.validate(tokenIssuer, oidcObj.issueTime) // now is IssueTime
        } catch (e: Exception) {
            Assert.assertTrue(e.message!!.indexOf("issuer") >= 0)
        }
    }

    @Test
    @Throws(NoSuchAlgorithmException::class, JOSEException::class)
    fun `Validate - Has Expired`() {
        try {
            val originalToken = getOriginalToken()
            val oidcObj = OidcObject(originalToken)
            oidcObj.getSignedToken(getCurrentRSAKey()!!, AccessTokenIssuer.OIDC_SIGNINGALG)
            oidcObj.validate(tokenIssuer) // now is after expirationTime
        } catch (e: Exception) {
            Assert.assertTrue(e.message!!.indexOf("expired") >= 0)
        }
    }

    @Test
    @Throws(NoSuchAlgorithmException::class, JOSEException::class)
    fun `Validate - Is Not Before`() {
        try {
            val originalToken = getOriginalToken()
            val oidcObj = OidcObject(originalToken)
            oidcObj.getSignedToken(getCurrentRSAKey()!!, AccessTokenIssuer.OIDC_SIGNINGALG)
            oidcObj.validate(tokenIssuer, Date(oidcObj.notBeforeTime!!.time - 2)) // now is before notBeforeTime
        } catch (e: Exception) {
            Assert.assertTrue(e.message!!.indexOf("notBeforeTime") >= 0)
        }
    }


    @Test
    @Throws(NoSuchAlgorithmException::class, JOSEException::class)
    fun `Validate - Unknown Key`() {
        try {
            val originalToken = getAlteredOriginalToken()
            val oidcObj = OidcObject(originalToken)
            oidcObj.validate(tokenIssuer, oidcObj.issueTime) // now is IssueTime
        } catch (e: Exception) {
            Assert.assertTrue(e.message!!.indexOf("failed to find key") >= 0)
        }
    }

    @Test
    @Throws(NoSuchAlgorithmException::class, JOSEException::class)
    fun `Validate - Original Token`() {
        try {
            val alteredToken = getOriginalToken()
            val oidcObj = OidcObject(alteredToken)
            val jwk = getOriginalJwkSet().getKeyByKeyId(oidcObj.keyId) as RSAKey
            oidcObj.validate(tokenIssuer.issuer, oidcObj.issueTime, jwk) // now is IssueTime
        } catch (e: Exception) {
            Assert.assertTrue(false)
        }
    }

    @Test
    @Throws(NoSuchAlgorithmException::class, JOSEException::class)
    fun `Validate Altered Token`() {
        try {
            val alteredToken = getAlteredOriginalToken()
            val oidcObj = OidcObject(alteredToken)
            val jwk = getOriginalJwkSet().getKeyByKeyId(oidcObj.keyId) as RSAKey
            oidcObj.validate(tokenIssuer.issuer, oidcObj.issueTime, jwk) // now is IssueTime
        } catch (e: Exception) {
            Assert.assertTrue(e.message!!.indexOf("Signature validation failed") >= 0)
        }
    }

    private fun getCurrentRSAKey(): RSAKey? {
        return rsaKeyStoreService.currentRSAKey
    }
}