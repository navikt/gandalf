package no.nav.gandalf.keystore

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.domain.KeyStoreLock
import no.nav.gandalf.domain.RSAKeyStore
import no.nav.gandalf.repository.KeyStoreLockRepository
import no.nav.gandalf.repository.RSAKeyStoreRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.Lock
import org.springframework.stereotype.Component
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.LocalDateTime
import java.util.*
import javax.persistence.LockModeType
import javax.transaction.Transactional

@Component
@Transactional
@Primary
class RSAKeyStoreRepositoryImpl(
        @Autowired val rsaKeyStoreRepository: RSAKeyStoreRepository,
        @Autowired val keyStoreLockRepository: KeyStoreLockRepository
) {

    fun findAllOrdered() =
            rsaKeyStoreRepository.findAll().sortedByDescending { it.id }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Throws(Exception::class)
    fun lockKeyStore(test: Boolean) {
        val lockedList = keyStoreLockRepository.findAll().sortedBy { it.id == 1L }
        when {
            test && lockedList.isEmpty() -> {
                val keyStoreLock = KeyStoreLock(1, false)
                keyStoreLockRepository.save(keyStoreLock)
            }
            else -> {
                if (lockedList.isEmpty()) {
                    throw Exception("Failed to lock keystore. KeyStoreLock is empty.")
                }
            }
        }
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
            lockKeyStore(false)
            val keyList: List<RSAKeyStore> = findAllOrdered()
            println("keyList: " + keyList.size)
            if (keyList.isNotEmpty() && !keyList[0].hasExpired()) {
                return keyList[0]
            }
            println(keyList.size >= minNoofKeys)
            // newest key has expired, update needed
            // delete outdated keys
            if (keyList.size >= minNoofKeys) {
                for (i in keyList.size - 1 downTo minNoofKeys - 1) { // evt slett bare oldest hvis man rensker db før prodsetting
                    if (keyList[i].expires.plusSeconds(2 * keyRotationTime).isAfter(LocalDateTime.now())) {
                        break
                    }
                    rsaKeyStoreRepository.delete(keyList[i])
                }
            }
            // generate and add a new key
            val rsaKey: RSAKeyStore = generateNewRSAKey()
            rsaKeyStoreRepository.save(rsaKey)
            return rsaKey
        }

    companion object {
        const val minNoofKeys = 2

        // i seconds, satt til 1 døgn
        const val keyRotationTime = 24 * 60 * 60.toLong()

        @Throws(NoSuchAlgorithmException::class, JOSEException::class)
        fun generateNewRSAKey(): RSAKeyStore {
            // Generate the RSA key pair
            val gen = KeyPairGenerator.getInstance("RSA")
            gen.initialize(2048) // Set the desired key length
            val keyPair = gen.generateKeyPair()

            // Convert to JWK format
            val jwk = RSAKey.Builder(keyPair.public as RSAPublicKey)
                    .privateKey(keyPair.private as RSAPrivateKey)
                    .keyID(UUID.randomUUID().toString()) // Give the key some ID (optional)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(AccessTokenIssuer.OIDC_SIGNINGALG)
                    .build()
            return RSAKeyStore(jwk, keyRotationTime)
        }
    }

    // kun for testing
    @Throws(NoSuchAlgorithmException::class, JOSEException::class)
    fun addRSAKey(expiryTime: LocalDateTime): RSAKeyStore {
        val keyStore: RSAKeyStore = generateNewRSAKey()
        keyStore.expires = expiryTime
        rsaKeyStoreRepository.save(keyStore)
        return keyStore
    }

    // kun for testing
    @Throws(NoSuchAlgorithmException::class, JOSEException::class)
    fun addNewRSAKey(): RSAKeyStore {
        val rsaKey: RSAKeyStore = generateNewRSAKey()
        rsaKeyStoreRepository.save(rsaKey)
        return rsaKey
    }

    // kun testing
    fun clear() {
        keyStoreLockRepository.deleteAll()
        rsaKeyStoreRepository.deleteAll()
        keyStoreLockRepository.flush()
        rsaKeyStoreRepository.flush()
    }
}