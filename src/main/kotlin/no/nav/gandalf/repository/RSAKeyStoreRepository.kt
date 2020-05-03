package no.nav.gandalf.repository

import no.nav.gandalf.domain.RSAKeyStore
import org.springframework.data.jpa.repository.JpaRepository

interface RSAKeyStoreRepository : JpaRepository<RSAKeyStore, Long>
