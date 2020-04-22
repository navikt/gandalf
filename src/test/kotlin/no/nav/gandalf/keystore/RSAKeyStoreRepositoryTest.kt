package no.nav.gandalf.keystore

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.KeyType
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import no.nav.gandalf.RSAKeyStoreTestExtension
import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.domain.RSAKeyStore
import no.nav.gandalf.service.RSAKeyStoreService
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDateTime

@RunWith(SpringRunner::class)
@SpringBootTest
class RSAKeyStoreRepositoryTest {

    @Autowired
    private lateinit var rsaKeyStoreService: RSAKeyStoreService

    @Autowired
    private lateinit var keyStoreTestExt: RSAKeyStoreTestExtension

    @Before
    fun init() {
        keyStoreTestExt.initKeyStoreLock()
        rsaKeyStoreService.resetCache()
    }

    @Test
    @Throws(Exception::class)
    fun `check Current RSAKey Caching With Empty DB`() {
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
    fun `check Current RSAKey Caching With Expired Key`() {
        // db with one expired key, 0 outdated keys
        rsaKeyStoreService.currRSAKeyStore = keyStoreTestExt.addRSAKey(LocalDateTime.now().minusSeconds(RSAKeyStoreRepositoryImpl.keyRotationTime + 10)) // expired, but not outdated
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
    fun `check Current RSAKey Caching With Key Added By Another Pod`() {
        // db with one expired key, 0 outdated keys
        val dbKey = keyStoreTestExt.addRSAKey(LocalDateTime.now().minusSeconds(RSAKeyStoreRepositoryImpl.keyRotationTime + 10)) // expired, but not outdate
        // set cache value
        rsaKeyStoreService.currRSAKeyStore = dbKey
        assertTrue(rsaKeyStoreService.currRSAKeyStore != null)
        assertTrue(rsaKeyStoreService.currRSAKeyStore!!.hasExpired())
        // add new valid key to DB (added by another pod)
        val newDbKey = keyStoreTestExt.addNewRSAKey()
        val currKey: RSAKey = rsaKeyStoreService.currentRSAKey // should read new key from DB and set new cache value
        assertTrue(rsaKeyStoreService.currRSAKeyStore != null)
        assertTrue(!rsaKeyStoreService.currRSAKeyStore!!.hasExpired())
        assertTrue(rsaKeyStoreService.currRSAKeyStore!!.rSAKey == currKey) // check returned value
        assertTrue(rsaKeyStoreService.currRSAKeyStore!!.rSAKey == newDbKey.rSAKey) // check cache value
    }

    @Test
    @Throws(Exception::class)
    fun `check JWKSet Caching With Empty DB`() {
        val jwkSet: JWKSet? = rsaKeyStoreService.getPublicJWKSet() // should return empty set
        assertTrue(jwkSet != null)
        assertTrue(jwkSet!!.keys.isEmpty())
        // TODO
        // assertTrue(rsaKeyStoreService.currPublicJWKSet!! == jwkSet) // check cache value
    }

    @Test
    @Throws(Exception::class)
    fun `check JWKSet Caching With New Key`() {
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
   fun `check JWKSet Caching With Empty DB And With ExpiredKey`() {
       // db with one expired key, 0 outdated keys
       rsaKeyStoreService.currRSAKeyStore = keyStoreTestExt.addRSAKey(LocalDateTime.now().minusSeconds(RSAKeyStoreRepositoryImpl.keyRotationTime + 10)) // expired, but not outdated
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
   fun checkJWKSetCachingWithKeyAddedByAnotherPod() {
       // db with one expired key, 0 outdated keys
       val oldDbKey: RSAKeyStore? =  keyStoreTestExt.addRSAKey(LocalDateTime.now().minusSeconds(RSAKeyStoreRepositoryImpl.keyRotationTime + 10)) // expired, but not outdated
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
       val newDbKey: RSAKeyStore = keyStoreTestExt.addNewRSAKey()
       // get public jwkset
       jwkSet = rsaKeyStoreService.publicJWKSet // should contain new key from DB
       assertTrue(jwkSet != null)
       assertTrue(jwkSet!!.keys.size == 2)
       assertTrue(jwkSet.keys[0].keyID == newDbKey.rSAKey.keyID)
       assertTrue(jwkSet.keys[1].keyID == oldDbKey.rSAKey.keyID)
   }
//   @Test
//   @Throws(Exception::class)
//   fun checkJWKSetCachingWithKeyAddedByAnotherPodAndGetCurrentKeyFirst() {
//       // db with one expired key, 0 outdated keys
//       val oldDbKey: RSAKeyStore = keyStoreTestExt.addRSAKey(LocalDateTime.now().minusSeconds(RSAKeyStoreRepositoryInternal.keyRotationTime + 10)) // expired, but not outdated
//       assertTrue(oldDbKey != null)
//       assertTrue(oldDbKey.hasExpired())

//       // set cache values
//       keyStore.currRSAKeyStore = oldDbKey
//       keyStore.currPublicJWKSet = keyStore.getPublicJWKSet(listOf(oldDbKey))

//       // get public jwkset
//       var jwkSet: JWKSet = keyStore.getPublicJWKSet() // should contain just one key
//       assertTrue(jwkSet != null)
//       assertTrue(jwkSet.keys.size == 1)
//       assertTrue(jwkSet.keys[0].keyID == oldDbKey.getRSAKey().getKeyID())

//       // add new valid key to DB (added by another pod)
//       val newDbKey: RSAKeyStore = keyStoreTestExt.addNewRSAKey()

//       // call current key, test that this call does nothing wrong to the cached jwkset
//       keyStore.getCurrentRSAKey()

//       // get public jwkset
//       jwkSet = keyStore.getPublicJWKSet() // should contain new key from DB
//       assertTrue(jwkSet != null)
//       assertTrue(jwkSet.keys.size == 2)
//       assertTrue(jwkSet.keys[0].keyID == newDbKey.getRSAKey().getKeyID())
//       assertTrue(jwkSet.keys[1].keyID == oldDbKey.getRSAKey().getKeyID())
//   }
}