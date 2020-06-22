package no.nav.gandalf.repository

import no.nav.gandalf.domain.KeyStoreLock
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import java.util.Optional
import javax.persistence.LockModeType

interface KeyStoreLockRepository : JpaRepository<KeyStoreLock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findById(id: Long): Optional<KeyStoreLock>
}
