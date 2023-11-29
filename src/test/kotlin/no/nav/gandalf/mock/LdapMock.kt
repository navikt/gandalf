package no.nav.gandalf.mock

import mu.KotlinLogging
import no.nav.gandalf.ldap.InMemoryLdap
import org.springframework.context.annotation.Configuration

private val log = KotlinLogging.logger { }

@Configuration
class LdapMock {
    final val inMemoryLdap = InMemoryLdap()

    init {
        log.info { "Setting up LDAP" }
        if (!started) {
            inMemoryLdap.start()
            started = true
        }
    }

    companion object {
        var started = false
    }
}
