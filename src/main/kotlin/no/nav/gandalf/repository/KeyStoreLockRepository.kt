package no.nav.gandalf.repository

import no.nav.gandalf.domain.KeyStoreLock
import org.springframework.data.jpa.repository.JpaRepository

interface KeyStoreLockRepository : JpaRepository<KeyStoreLock, Long>
