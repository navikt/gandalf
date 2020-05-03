package no.nav.gandalf.repository

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.LocalDateTime
import java.util.UUID
import javax.transaction.Transactional
import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.domain.RSAKeyStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Transactional
@Component
@Primary
class RSAKeyStoreRepositoryImpl {

    @Autowired lateinit var rsaKeyStoreRepository: RSAKeyStoreRepository

    fun findAllOrdered() =
            rsaKeyStoreRepository.findAll().sortedByDescending { it.id }

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

    fun delete(rsaKey: RSAKeyStore) {
        rsaKeyStoreRepository.delete(rsaKey)
    }

    fun save(rsaKey: RSAKeyStore) {
        rsaKeyStoreRepository.save(rsaKey)
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
        rsaKeyStoreRepository.deleteAll()
        rsaKeyStoreRepository.flush()
    }
}
