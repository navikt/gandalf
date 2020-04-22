package no.nav.gandalf

import com.nimbusds.jose.JOSEException
import no.nav.gandalf.domain.KeyStoreLock
import no.nav.gandalf.domain.RSAKeyStore
import no.nav.gandalf.keystore.RSAKeyStoreRepositoryImpl
import no.nav.gandalf.repository.KeyStoreLockRepository
import no.nav.gandalf.repository.RSAKeyStoreRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.stereotype.Component
import java.security.NoSuchAlgorithmException
import java.time.LocalDateTime
import javax.persistence.LockModeType
import javax.transaction.Transactional

@Component
@Transactional
class RSAKeyStoreTestExtension(
        var rsaKeyStoreRepository: RSAKeyStoreRepository,
        var keyStoreLockRepository: KeyStoreLockRepository
) {

    // kun for testing
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun lockKeyStore() {
        val lockedList = keyStoreLockRepository.findAll().sortedBy { it.id == 1L }
        if (lockedList.isEmpty()) {
            val keyStoreLock = KeyStoreLock(1, false)
            keyStoreLockRepository.save(keyStoreLock)
        }
    }

    // kun for testing
    @Throws(NoSuchAlgorithmException::class, JOSEException::class)
    fun addRSAKey(expiryTime: LocalDateTime): RSAKeyStore {
        val keyStore: RSAKeyStore = RSAKeyStoreRepositoryImpl.generateNewRSAKey()
        keyStore.expires = expiryTime
        rsaKeyStoreRepository.save(keyStore)
        return keyStore
    }

    // kun for testing
    @Throws(NoSuchAlgorithmException::class, JOSEException::class)
    fun addNewRSAKey(): RSAKeyStore {
        val rsaKey: RSAKeyStore = RSAKeyStoreRepositoryImpl.generateNewRSAKey()
        rsaKeyStoreRepository.save(rsaKey)
        return rsaKey
    }


    fun clear() {
        keyStoreLockRepository.deleteAllInBatch()
    }
}