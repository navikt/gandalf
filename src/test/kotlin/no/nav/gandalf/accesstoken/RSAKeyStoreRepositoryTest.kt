package no.nav.gandalf.accesstoken

import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.KeyType
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import no.nav.gandalf.domain.RSAKeyStore
import no.nav.gandalf.repository.RSAKeyStoreRepositoryImpl
import no.nav.gandalf.service.RSAKeyStoreService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDateTime
import java.util.*


@RunWith(SpringRunner::class)
@SpringBootTest
@DirtiesContext
@TestPropertySource(locations = ["classpath:application-test.properties"])
class RSAKeyStoreRepositoryTest {

    @Autowired
    private lateinit var rsaKeyStoreService: RSAKeyStoreService

    @Before
    fun init() {
        rsaKeyStoreService.resetRepository()
        rsaKeyStoreService.resetCache()
    }

    @Test
    @Throws(Exception::class)
    fun `Current RSAKey Caching With Empty DB`() {
        assertTrue(rsaKeyStoreService.currRSAKeyStore == null)
        val key: RSAKey = rsaKeyStoreService.currentRSAKey // should generate a new key
        assertTrue(rsaKeyStoreService.currRSAKeyStore != null)
        assertTrue(!rsaKeyStoreService.currRSAKeyStore!!.hasExpired())
        assertTrue(rsaKeyStoreService.currRSAKeyStore!!.rSAKey == key) // check cache value
        val key2: RSAKey = rsaKeyStoreService.currentRSAKey // should return same key
        assertTrue(key2 == key)
        assertTrue(rsaKeyStoreService.currRSAKeyStore!!.rSAKey == key) // check cache value
    }

    @Test
    @Throws(Exception::class)
    fun `Current RSAKey Caching With Expired Key`() {
        // db with one expired key, 0 outdated keys
        rsaKeyStoreService.currRSAKeyStore = rsaKeyStoreService.addRSAKey(LocalDateTime.now().minusSeconds(RSAKeyStoreRepositoryImpl.keyRotationTime + 10)) // expired, but not outdated
        assertTrue(rsaKeyStoreService.currRSAKeyStore != null)
        assertTrue(rsaKeyStoreService.currRSAKeyStore!!.hasExpired())
        val key: RSAKey = rsaKeyStoreService.currentRSAKey // should generate a new key and set new cache value
        assertTrue(rsaKeyStoreService.currRSAKeyStore != null)
        assertTrue(!rsaKeyStoreService.currRSAKeyStore!!.hasExpired())
        assertTrue(rsaKeyStoreService.currRSAKeyStore!!.rSAKey == key) // check cache value
        val key2: RSAKey = rsaKeyStoreService.currentRSAKey // should return same key
        assertTrue(key2 == key)
        assertTrue(rsaKeyStoreService.currRSAKeyStore!!.rSAKey == key) // check cache value
    }

    @Test
    @Throws(Exception::class)
    fun `Current RSAKey Caching With Key Added By Another Pod`() {
        // db with one expired key, 0 outdated keys
        val dbKey = rsaKeyStoreService.addRSAKey(LocalDateTime.now().minusSeconds(RSAKeyStoreRepositoryImpl.keyRotationTime + 10)) // expired, but not outdate
        // set cache value
        rsaKeyStoreService.currRSAKeyStore = dbKey
        assertTrue(rsaKeyStoreService.currRSAKeyStore != null)
        assertTrue(rsaKeyStoreService.currRSAKeyStore!!.hasExpired())
        // add new valid key to DB (added by another pod)
        val newDbKey = rsaKeyStoreService.addNewRSAKey()
        val currKey: RSAKey = rsaKeyStoreService.currentRSAKey // should read new key from DB and set new cache value
        assertTrue(rsaKeyStoreService.currRSAKeyStore != null)
        assertTrue(!rsaKeyStoreService.currRSAKeyStore!!.hasExpired())
        assertTrue(rsaKeyStoreService.currRSAKeyStore!!.rSAKey == currKey) // check returned value
        assertTrue(rsaKeyStoreService.currRSAKeyStore!!.rSAKey == newDbKey.rSAKey) // check cache value
    }

    @Test
    @Throws(Exception::class)
    fun `JWKSet Caching With Empty DB`() {
        val jwkSet: JWKSet? = rsaKeyStoreService.getPublicJWKSet() // should return empty set
        assertTrue(jwkSet != null)
        assertTrue(jwkSet!!.keys.isEmpty())
        // TODO
        // assertTrue(rsaKeyStoreService.currPublicJWKSet!! == jwkSet) // check cache value
    }

    @Test
    @Throws(Exception::class)
    fun `JWKSet Caching With New Key`() {
        // check empty DB
        var jwkSet: JWKSet? = rsaKeyStoreService.publicJWKSet // should return empty set
        assertTrue(jwkSet != null)
        assertTrue(jwkSet!!.keys.isEmpty())
        // generate new key in DB
        val currentKey = rsaKeyStoreService.currentRSAKey
        jwkSet = rsaKeyStoreService.publicJWKSet // should return set with one key and set the cache value
        assertTrue(jwkSet?.keys != null)
        assertTrue(jwkSet!!.keys.size == 1)
        assertTrue(rsaKeyStoreService.currPublicJWKSet!! == jwkSet) // check cache valu
        // check the key values in jwkSet
        val jwk = jwkSet.keys[0]
        assertTrue(jwk.keyUse == KeyUse.SIGNATURE)
        assertTrue(jwk.algorithm.name == AccessTokenIssuer.OIDC_SIGNINGALG.name)
        assertTrue(jwk.keyType === KeyType.RSA)
        assertTrue(jwk.keyID == currentKey.keyID)
    }

    @Test
    @Throws(Exception::class)
    fun `JWKSet Caching With Empty DB And With ExpiredKey`() {
        // db with one expired key, 0 outdated keys
        rsaKeyStoreService.currRSAKeyStore = rsaKeyStoreService.addRSAKey(LocalDateTime.now().minusSeconds(RSAKeyStoreRepositoryImpl.keyRotationTime + 10)) // expired, but not outdated
        assertTrue(rsaKeyStoreService.currRSAKeyStore != null)
        assertTrue(rsaKeyStoreService.currRSAKeyStore!!.hasExpired())
        val key: RSAKey = rsaKeyStoreService.currentRSAKey // should generate a new key and set new cache value
        assertTrue(rsaKeyStoreService.currRSAKeyStore != null)
        assertTrue(!rsaKeyStoreService.currRSAKeyStore!!.hasExpired())
        assertTrue(rsaKeyStoreService.currRSAKeyStore!!.rSAKey == key)
        val key2: RSAKey = rsaKeyStoreService.currentRSAKey // should return same key
        assertTrue(key2 == key)
        assertTrue(rsaKeyStoreService.currRSAKeyStore!!.rSAKey == key)
    }

    @Test
    @Throws(Exception::class)
    fun `JWKSet Caching With Key Added By Another Pod`() {
        // db with one expired key, 0 outdated keys
        val oldDbKey: RSAKeyStore? = rsaKeyStoreService.addRSAKey(LocalDateTime.now().minusSeconds(RSAKeyStoreRepositoryImpl.keyRotationTime + 10)) // expired, but not outdated
        assertTrue(oldDbKey != null)
        assertTrue(oldDbKey!!.hasExpired())
        // set cache values
        rsaKeyStoreService.currRSAKeyStore = oldDbKey
        rsaKeyStoreService.currPublicJWKSet = null
        // get public jwkset
        var jwkSet: JWKSet? = rsaKeyStoreService.publicJWKSet // should read from DB
        assertTrue(jwkSet != null)
        assertTrue(jwkSet!!.keys.size == 1)
        assertTrue(jwkSet.keys[0].keyID == oldDbKey.rSAKey.keyID)
        // add new valid key to DB (added by another pod)
        val newDbKey: RSAKeyStore = rsaKeyStoreService.addNewRSAKey()
        // get public jwkset
        jwkSet = rsaKeyStoreService.publicJWKSet // should contain new key from DB
        assertTrue(jwkSet != null)
        assertTrue(jwkSet!!.keys.size == 2)
        assertTrue(jwkSet.keys[0].keyID == newDbKey.rSAKey.keyID)
        assertTrue(jwkSet.keys[1].keyID == oldDbKey.rSAKey.keyID)
    }

    @Test
    @Throws(Exception::class)
    fun `JWKSet Caching With Key Added By Another Pod And Get Current Key First`() {
        // db with one expired key, 0 outdated keys
        val oldDbKey: RSAKeyStore? = rsaKeyStoreService.addRSAKey(LocalDateTime.now().minusSeconds(RSAKeyStoreRepositoryImpl.keyRotationTime + 10)) // expired, but not outdated
        assertTrue(oldDbKey != null)
        assertTrue(oldDbKey!!.hasExpired())

        // set cache values
        rsaKeyStoreService.currRSAKeyStore = oldDbKey
        rsaKeyStoreService.currPublicJWKSet = rsaKeyStoreService.getPublicJWKSet(listOf(oldDbKey))

        // get public jwkset
        var jwkSet: JWKSet? = rsaKeyStoreService.publicJWKSet // should contain just one key
        assertTrue(jwkSet != null)
        assertTrue(jwkSet!!.keys.size == 1)
        assertTrue(jwkSet.keys[0].keyID == oldDbKey.rSAKey.keyID)

        // add new valid key to DB (added by another pod)
        val newDbKey: RSAKeyStore = rsaKeyStoreService.addNewRSAKey()

        // call current key, test that this call does nothing wrong to the cached jwkset
        rsaKeyStoreService.currentRSAKey

        // get public jwkset
        jwkSet = rsaKeyStoreService.publicJWKSet // should contain new key from DB
        assertTrue(jwkSet != null)
        assertTrue(jwkSet!!.keys.size == 2)
        assertTrue(jwkSet.keys[0].keyID == newDbKey.rSAKey.keyID)
        assertTrue(jwkSet.keys[1].keyID == oldDbKey.rSAKey.keyID)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun `Check JWKSet Caching With Key Added And Not Matching Current RsaKey Store`() {
        val newKey: RSAKeyStore = rsaKeyStoreService.addRSAKey(LocalDateTime.now().plusSeconds(RSAKeyStoreRepositoryImpl.keyRotationTime + 10)) // not expired
        assertNotNull(newKey)
        assertFalse(newKey.hasExpired())

        // set cache values
        rsaKeyStoreService.currRSAKeyStore = newKey
        rsaKeyStoreService.currPublicJWKSet = JWKSet(listOf(RSAKeyStoreRepositoryImpl.generateNewRSAKey().rSAKey))

        // get public jwkset
        val jwkSet: JWKSet = rsaKeyStoreService.publicJWKSet!! // should read from DB
        assertNotNull(jwkSet)
        assertEquals(1, jwkSet.keys.size)
        assertEquals(jwkSet.keys[0].keyID, newKey.rSAKey.keyID)
        assertEquals(jwkSet.keys[0].keyID, rsaKeyStoreService.currPublicJWKSet!!.keys[0].keyID)
    }
}
