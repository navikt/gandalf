package no.nav.gandalf.accesstoken

import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.SignedJWT
import no.nav.gandalf.accesstoken.oauth.OidcObject
import no.nav.gandalf.service.RsaKeysProvider
import no.nav.gandalf.utils.compare
import no.nav.gandalf.utils.getAlteredOriginalToken
import no.nav.gandalf.utils.getOriginalJwkSet
import no.nav.gandalf.utils.getOriginalToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.time.ZonedDateTime

@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
@DirtiesContext
class OIDCObjectTest {

    @Autowired
    private lateinit var tokenIssuer: AccessTokenIssuer

    @Autowired
    private lateinit var rsaKeyStoreProvider: RsaKeysProvider

    @Test
    fun `Compare Generated Token To Original Token ver1`() {
        assertDoesNotThrow {
            // gammelt token
            val jwtOriginal = SignedJWT.parse(getOriginalToken()).jwtClaimsSet
            val username = jwtOriginal.subject
            // test constructor
            // get token based on values from original token for comparison
            val oidcObj = OidcObject(
                AccessTokenIssuer.toZonedDateTime(jwtOriginal.issueTime),
                AccessTokenIssuer.OIDC_DURATION_TIME,
            )
            oidcObj.id = jwtOriginal.jwtid
            oidcObj.subject = username
            oidcObj.issuer = jwtOriginal.issuer
            oidcObj.version = AccessTokenIssuer.OIDC_VERSION
            oidcObj.setAudience(username, AccessTokenIssuer.getDomainFromIssuerURL(jwtOriginal.issuer))
            oidcObj.azp = username
            oidcObj.resourceType = AccessTokenIssuer.getIdentType(username)
            val token = oidcObj.getSignedToken(getCurrentRSAKey(), AccessTokenIssuer.OIDC_SIGNINGALG)
            val jwt = token.jwtClaimsSet
            compare(jwtOriginal, jwt)
        }
    }

    @Test
    fun `Compare Generated Token To Original Token ver2`() {
        assertDoesNotThrow {
            // Gammelt token
            val originalToken = getOriginalToken()
            val jwtOriginal = SignedJWT.parse(originalToken).jwtClaimsSet
            val username = jwtOriginal.subject
            // Use test constructor
            // get token based on values from original token for comparison
            var oidcObj = OidcObject(
                AccessTokenIssuer.toZonedDateTime(jwtOriginal.issueTime),
                AccessTokenIssuer.toZonedDateTime(jwtOriginal.expirationTime),
            )
            oidcObj.id = jwtOriginal.jwtid
            oidcObj.subject = username
            oidcObj.issuer = jwtOriginal.issuer
            oidcObj.version = AccessTokenIssuer.OIDC_VERSION
            oidcObj.setAudience(username, AccessTokenIssuer.getDomainFromIssuerURL(jwtOriginal.issuer))
            oidcObj.azp = username
            oidcObj.resourceType = AccessTokenIssuer.getIdentType(username)
            var token = oidcObj.getSignedToken(getCurrentRSAKey(), AccessTokenIssuer.OIDC_SIGNINGALG)
            var jwt = token.jwtClaimsSet
            compare(jwtOriginal, jwt)
            // Use test constructor
            oidcObj = OidcObject(originalToken)
            token = oidcObj.getSignedToken(getCurrentRSAKey(), AccessTokenIssuer.OIDC_SIGNINGALG)
            jwt = token.jwtClaimsSet
            compare(jwtOriginal, jwt)
        }
    }

    @Test
    fun `Compare Generated Token To Original Token ver3`() {
        assertDoesNotThrow {
            // gammelt token
            val originalToken = getOriginalToken()
            val jwtOriginal = SignedJWT.parse(originalToken).jwtClaimsSet
            // test constructor
            // get token based on values from original token for comparison
            val oidcObj = OidcObject(originalToken)
            val token = oidcObj.getSignedToken(getCurrentRSAKey(), AccessTokenIssuer.OIDC_SIGNINGALG)
            val jwt = token.jwtClaimsSet
            compare(jwtOriginal, jwt)
        }
    }

    @Test
    fun `Validate a valid token`() {
        assertDoesNotThrow {
            val oidcObj = OidcObject(tokenIssuer.issueToken("test").serialize())
            oidcObj.validate(tokenIssuer.issuer, oidcObj.issueTime, getCurrentRSAKey()) // now is IssueTime
        }
    }

    @Test
    fun `Validation should fail with - Unknown Issuer`() {
        val exception: IllegalArgumentException = assertThrows(IllegalArgumentException::class.java) {
            val oidcObj = OidcObject(tokenIssuer.issueToken("test").serialize())
            oidcObj.issuer = "tull"
            oidcObj.validate(tokenIssuer, oidcObj.issueTime) // now is IssueTime
        }
        val expectedMessage = "Validation failed: 'issuer' is null or unknown"
        val actualMessage = exception.message
        assertEquals(expectedMessage, actualMessage!!)
    }

    @Test
    fun `Validation should fail with - Has Expired`() {
        val exception: IllegalArgumentException = assertThrows(IllegalArgumentException::class.java) {
            val oidcObj = OidcObject(tokenIssuer.issueToken("test").serialize())
            val expiredDate = OidcObject.toDate(ZonedDateTime.now().plusSeconds(3665))
            oidcObj.validate(tokenIssuer.issuer, expiredDate, getCurrentRSAKey()) // now is after expirationTime
        }
        val expectedMessage = "Validation failed: token has expired"
        val actualMessage = exception.message
        assertEquals(expectedMessage, actualMessage!!)
    }

    @Test
    fun `Validation should fail with - Is Not Before`() {
        val exception: IllegalArgumentException = assertThrows(IllegalArgumentException::class.java) {
            val oidcObj = OidcObject(tokenIssuer.issueToken("test").serialize())
            val notBeforeDate = OidcObject.toDate(ZonedDateTime.now().minusSeconds(70))
            oidcObj.validate(tokenIssuer.issuer, notBeforeDate, getCurrentRSAKey())
            // now is before notBeforeTime
        }
        val expectedMessage = "Validation failed: notBeforeTime validation failed"
        val actualMessage = exception.message
        assertEquals(expectedMessage, actualMessage!!)
    }

    @Test
    fun `Validation should fail with - Unknown Key`() {
        val exception: IllegalArgumentException = assertThrows(IllegalArgumentException::class.java) {
            val originalToken = getAlteredOriginalToken()
            val oidcObj = OidcObject(originalToken)
            // now is IssueTime
            oidcObj.validate(tokenIssuer, oidcObj.issueTime)
        }
        val expectedMessage = "Validation failed: failed to find key 10e9ed16-eb87-494a-a4ff-351651d4b98e in keys " +
            "provided by issuer https://security-token-service.nais.preprod.local"
        val actualMessage = exception.message
        assertEquals(expectedMessage, actualMessage!!)
    }

    @Test
    fun `Validate an Original Token`() {
        assertDoesNotThrow {
            val alteredToken = getOriginalToken()
            val oidcObj = OidcObject(alteredToken)
            val jwk = getOriginalJwkSet().getKeyByKeyId(oidcObj.keyId) as RSAKey
            oidcObj.validate(tokenIssuer.issuer, oidcObj.issueTime, jwk)
        }
    }

    @Test
    fun `Validation should fail with - Altered Token`() {
        val exception: IllegalArgumentException = assertThrows(IllegalArgumentException::class.java) {
            val alteredToken = getAlteredOriginalToken()
            val oidcObj = OidcObject(alteredToken)
            val jwk = getOriginalJwkSet().getKeyByKeyId(oidcObj.keyId) as RSAKey
            oidcObj.validate(tokenIssuer.issuer, oidcObj.issueTime, jwk)
        }
        val expectedMessage = "Validation failed: Signature validation failed"
        val actualMessage = exception.message
        assertEquals(expectedMessage, actualMessage!!)
    }

    @Test
    fun `Validate NotBeforeTime ClockSkew OK`() {
        assertDoesNotThrow {
            val originalToken = tokenIssuer.issueToken("test")
            val oidcObj = OidcObject(originalToken.serialize())
            oidcObj.notBeforeTime = OidcObject.toDate(ZonedDateTime.now().plusSeconds(55))
            oidcObj.validate(tokenIssuer.issuer, oidcObj.issueTime, getCurrentRSAKey())
        }
    }

    @Test
    fun `Validate NotBeforeTime ClockSkew NOT OK`() {
        val exception: IllegalArgumentException = assertThrows(IllegalArgumentException::class.java) {
            val originalToken = tokenIssuer.issueToken("test")
            val oidcObj = OidcObject(originalToken.serialize())
            oidcObj.notBeforeTime = OidcObject.toDate(ZonedDateTime.now().plusSeconds(65))
            oidcObj.validate(tokenIssuer.issuer, oidcObj.issueTime, getCurrentRSAKey())
        }
        val expectedMessage = "Validation failed: notBeforeTime validation failed"
        val actualMessage = exception.message
        assertEquals(expectedMessage, actualMessage!!)
    }

    @Test
    fun `Validate ExpiresIn ClockSkew OK`() {
        assertDoesNotThrow {
            val originalToken = tokenIssuer.issueToken("test")
            val oidcObj = OidcObject(originalToken.serialize())
            oidcObj.expirationTime = OidcObject.toDate(ZonedDateTime.now().minusSeconds(55))
            oidcObj.validate(tokenIssuer.issuer, oidcObj.issueTime, getCurrentRSAKey())
        }
    }

    @Test
    fun `Validate ExpiresIn ClockSkew NOT OK`() {
        val exception: IllegalArgumentException = assertThrows(IllegalArgumentException::class.java) {
            val originalToken = tokenIssuer.issueToken("test")
            val oidcObj = OidcObject(originalToken.serialize())
            oidcObj.expirationTime = OidcObject.toDate(ZonedDateTime.now().minusSeconds(65))
            oidcObj.validate(tokenIssuer.issuer, oidcObj.issueTime, getCurrentRSAKey())
        }
        val expectedMessage = "Validation failed: token has expired"
        val actualMessage = exception.message
        assertEquals(expectedMessage, actualMessage!!)
    }

    private fun getCurrentRSAKey(): RSAKey {
        return rsaKeyStoreProvider.currentRSAKey
    }
}
