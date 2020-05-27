package no.nav.gandalf.ldap

import no.nav.gandalf.config.LdapConfig
import no.nav.gandalf.model.User
import no.nav.gandalf.utils.InMemoryLdap
import org.junit.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension::class)
@SpringBootTest
class LdapTest {

    private val inMemoryLdap = InMemoryLdap().start()

    @Test
    fun `Authenticated User`() {
        val ldap = Ldap(
            LdapConfig(
                url = "localhost",
                base = "dc=test,dc=local",
                port = 11389,
                remote = "false"
            )
        )
        val authenticatedUser = User("srvPDP", "password")
        assert(ldap.result(user = authenticatedUser))
    }
}
