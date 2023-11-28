package no.nav.gandalf.repository

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.TypedQuery
import jakarta.transaction.Transactional
import mu.KotlinLogging
import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.domain.RsaKeys
import org.springframework.stereotype.Component
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.LocalDateTime
import java.util.UUID

private val log = KotlinLogging.logger { }

@Component
class RsaKeysRepository {

    @PersistenceContext
    private lateinit var entityManager: EntityManager
    private var keyRotationTimeSeconds = DEFAULT_KEY_ROTATION

    fun setKeyRotationTimeSeconds(keyRotationTimeSeconds: Long) {
        this.keyRotationTimeSeconds = keyRotationTimeSeconds
    }

    @get:Transactional
    val keys: RsaKeys
        get() {
            val rsaKeys = read()
            if (rsaKeys.expired(LocalDateTime.now())) {
                log.info("RSA KEY DB rotating keys")
                log.debug("Before rotate: " + dumpIds(rsaKeys))
                val newKey: RSAKey = generateNewRSAKey()
                val expiry = LocalDateTime.now().plusSeconds(keyRotationTimeSeconds)
                rsaKeys.rotateKeys(newKey, expiry)
                log.debug("AFTER rotate: " + dumpIds(rsaKeys))
                log.info("RSA KEY DB rotated, next expiry: $expiry")
            }
            return rsaKeys
        }

    private fun dumpIds(r: RsaKeys) = "Keys: C ${r.getCurrentKey().keyID} P ${r.getPreviousKey()
        .keyID} N ${r.getNextKey().keyID} exp ${r.expiry}"

    private fun read(): RsaKeys {
        val query: TypedQuery<RsaKeys> = entityManager.createQuery("FROM RsaKeys", RsaKeys::class.java)
        // Denne skal alltid inneholde 1 record, men ta høyde for initielt kall
        val resultList: List<RsaKeys>? = query.resultList
        return when {
            resultList.isNullOrEmpty() -> {
                initKeys()
            }
            else -> resultList[0]
        }
    }

    // Kalles kun for tom base
    private fun initKeys(): RsaKeys {
        log.info("RSA KEY DB will be initialised")
        val key1: RSAKey = generateNewRSAKey()
        val key2: RSAKey = generateNewRSAKey()
        val key3: RSAKey = generateNewRSAKey()
        val expiry = LocalDateTime.now().plusSeconds(keyRotationTimeSeconds)
        val initKeys = RsaKeys(1L, key1, key2, key3, expiry)
        entityManager.persist(initKeys)
        log.info("RSA KEY DB initialised, next expiry: $expiry")
        return initKeys
    }

    companion object {
        const val DEFAULT_KEY_ROTATION = 24 * 60 * 60 // i seconds, satt til 1 døgn
            .toLong()

        @Throws(NoSuchAlgorithmException::class, JOSEException::class)
        fun generateNewRSAKey(): RSAKey {
            // Generate the RSA key pair
            val gen = KeyPairGenerator.getInstance("RSA")
            gen.initialize(2048) // Set the desired key length
            val keyPair = gen.generateKeyPair()

            // Convert to JWK format
            return RSAKey.Builder(keyPair.public as RSAPublicKey)
                .privateKey(keyPair.private as RSAPrivateKey)
                .keyID(UUID.randomUUID().toString())
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(AccessTokenIssuer.OIDC_SIGNINGALG)
                .build()
        }
    }
}
