package no.nav.gandalf.repository

import no.nav.gandalf.domain.RSAKeyStore
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface RSAKeyStoreRepository : JpaRepository<RSAKeyStore, Long>