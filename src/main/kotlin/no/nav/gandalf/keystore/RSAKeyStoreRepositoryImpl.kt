package no.nav.gandalf.keystore

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.domain.RSAKeyStore
import no.nav.gandalf.repository.KeyStoreLockRepository
import no.nav.gandalf.repository.RSAKeyStoreRepository
import org.springframework.beans.factory.annotation.Autowired
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
class RSAKeyStoreRepositoryImpl(
        @Autowired val rsaKeyStoreRepository: RSAKeyStoreRepository,
        @Autowired val keyStoreLockRepository: KeyStoreLockRepository
) {

    fun findAllOrdered() =
        rsaKeyStoreRepository.findAll().sortedByDescending { it.id }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Throws(Exception::class)
    fun lockKeyStore(){
        val lockedList = keyStoreLockRepository.findAll().sortedBy { it.locked == 1L }
        if(lockedList.isEmpty()){
            throw Exception("Failed to lock keystore. KeyStoreLock is empty.")
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
            lockKeyStore()
            val keyList: List<RSAKeyStore> = findAllOrdered()
            if (keyList.isNotEmpty() && !keyList[0].hasExpired()) {
                return keyList[0]
            }
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
        const val keyRotationTime = 24 * 60 * 60 // i seconds, satt til 1 døgn
                .toLong()

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
}