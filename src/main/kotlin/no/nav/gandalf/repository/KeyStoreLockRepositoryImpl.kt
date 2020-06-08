package no.nav.gandalf.repository

import no.nav.gandalf.domain.KeyStoreLock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.jpa.repository.Lock
import org.springframework.stereotype.Component
import javax.persistence.LockModeType
import javax.transaction.Transactional

@Transactional
@Component
class KeyStoreLockRepositoryImpl(
    @Value("\${spring.profiles.active}")
    val profile: String
) {

    @Autowired
    var keyStoreLockRepository: KeyStoreLockRepository? = null

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Throws(Exception::class)
    fun lockKeyStore() {
        val lockedList = keyStoreLockRepository?.findById(1)
        when (profile) {
            "test" -> {
                val keyStoreLock = KeyStoreLock(1, false)
                keyStoreLockRepository!!.save(keyStoreLock)
            }
            else -> {
                if (lockedList == null || lockedList.isEmpty) {
                    throw Exception("Failed to lock keystore. KeyStoreLock is empty.")
                }
            }
        }
    }

    // kun testing
    fun clear() {
        keyStoreLockRepository!!.deleteAll()
        keyStoreLockRepository!!.flush()
    }
}
