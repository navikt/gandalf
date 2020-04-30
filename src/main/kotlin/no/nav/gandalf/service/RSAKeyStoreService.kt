package no.nav.gandalf.service

import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import no.nav.gandalf.domain.RSAKeyStore
import no.nav.gandalf.repository.KeyStoreLockRepositoryImpl
import no.nav.gandalf.repository.RSAKeyStoreRepositoryImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class RSAKeyStoreService {

    @Autowired
    lateinit var rsaKeyRepositoryImpl: RSAKeyStoreRepositoryImpl
    @Autowired
    lateinit var keyStoreRepositoryImpl: KeyStoreLockRepositoryImpl

    var currRSAKeyStore: RSAKeyStore? = null // satt public for testing
    var currPublicJWKSet: JWKSet? = null // satt public for testing

    // currPublicJWKSet er utdatert, settes til null for å trigge lesing fra DB ved neste kall til getPublicJWKSet
    @get:Throws(Exception::class)
    val currentRSAKey: RSAKey
        get() {
            when {
                currRSAKeyStore == null || currRSAKeyStore!!.hasExpired() -> {
                    currRSAKeyStore = currentDBKeyUpdateIfNeeded
                    currPublicJWKSet = null // currPublicJWKSet er utdatert, settes til null for å trigge lesing fra DB ved neste kall til getPublicJWKSet
                }
            }
            return currRSAKeyStore!!.rSAKey
        }

    // les fra DB
    val publicJWKSet: JWKSet?
        get() {
            when {
                currPublicJWKSet == null || currRSAKeyStore == null || currRSAKeyStore!!.hasExpired() -> {
                    currPublicJWKSet = getPublicJWKSet(rsaKeyRepositoryImpl.findAllOrdered()) // les fra DB
                }
            }
            return currPublicJWKSet
        }

    // evt slett bare oldest hvis man rensker db før prodsetting
    // generate and add a new key

    // lock keystore before read, in case an update of keystore is needed
    // newest key has expired, update needed
    // delete outdated keys

    @get:Throws(Exception::class)
    val currentDBKeyUpdateIfNeeded: RSAKeyStore
        get() {
            // lock keystore before read, in case an update of keystore is needed
            keyStoreRepositoryImpl.lockKeyStore(false)
            val keyList: List<RSAKeyStore> = rsaKeyRepositoryImpl.findAllOrdered()
            println("keyList: " + keyList.size)
            if (keyList.isNotEmpty() && !keyList[0].hasExpired()) {
                return keyList[0]
            }
            println(keyList.size >= RSAKeyStoreRepositoryImpl.minNoofKeys)
            // newest key has expired, update needed
            // delete outdated keys
            if (keyList.size >= RSAKeyStoreRepositoryImpl.minNoofKeys) {
                for (i in keyList.size - 1 downTo RSAKeyStoreRepositoryImpl.minNoofKeys - 1) { // evt slett bare oldest hvis man rensker db før prodsetting
                    if (keyList[i].expires.plusSeconds(2 * RSAKeyStoreRepositoryImpl.keyRotationTime).isAfter(LocalDateTime.now())) {
                        break
                    }
                    rsaKeyRepositoryImpl.delete(keyList[i])
                }
            }
            // generate and add a new key
            val rsaKey: RSAKeyStore = RSAKeyStoreRepositoryImpl.generateNewRSAKey()
            rsaKeyRepositoryImpl.save(rsaKey)
            return rsaKey
        }

    fun getPublicJWKSet(keyList: List<RSAKeyStore>? = null): JWKSet {
        val jwkList: MutableList<JWK> = ArrayList()
        if (keyList != null) {
            for (key in keyList) {
                jwkList.add(key.rSAKey)
            }
        }
        return JWKSet(jwkList).toPublicJWKSet()
    }

    fun lock(isTest: Boolean) {
        keyStoreRepositoryImpl.lockKeyStore(isTest)
    }

    fun findAllOrdered() = rsaKeyRepositoryImpl.findAllOrdered()

    fun addRSAKey(localDateTime: LocalDateTime) = rsaKeyRepositoryImpl.addRSAKey(localDateTime)

    fun addNewRSAKey() = rsaKeyRepositoryImpl.addNewRSAKey()

    // Kun Test
    fun resetCache() {
        currRSAKeyStore = null
        currPublicJWKSet = null
    }

    // Kun Test
    fun resetRepository() {
        rsaKeyRepositoryImpl.clear()
        keyStoreRepositoryImpl.clear()
    }
}