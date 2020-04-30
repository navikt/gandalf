package no.nav.gandalf.repository

import no.nav.gandalf.domain.KeyStoreLock
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface KeyStoreLockRepository : JpaRepository<KeyStoreLock, Long>