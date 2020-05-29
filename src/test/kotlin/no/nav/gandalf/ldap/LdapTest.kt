package no.nav.gandalf.ldap

import no.nav.gandalf.config.LdapConfig
import no.nav.gandalf.model.User
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
class LdapTest {

    private val inMemoryLdap = InMemoryLdap()

    @Before
    fun setup() {
        inMemoryLdap.start()
    }

    @After
    fun clean() {
        inMemoryLdap.stop()
    }

    private val ldapConfig = LdapConfig(
        url = "localhost",
        base = "ou=ServiceAccounts,dc=test,dc=local",
        port = 11389,
        remote = "false"
    )

    @Test
    fun `Authenticated User Under OU=ServiceAccounts`() {
        val ldap = Ldap(ldapConfig)
        val authenticatedUser = User("srvPDP", "password")
        assert(ldap.result(user = authenticatedUser))
    }

    @Test
    fun `Authenticated User Under OU=ApplAccounts,OU=ServiceAccounts`() {
        val ldap = Ldap(ldapConfig)
        val authenticatedUser = User("srvaltutkanal", "password")
        assert(ldap.result(user = authenticatedUser))
    }

    @Test
    fun `UnAuthorized User`() {
        val ldap = Ldap(ldapConfig)
        val authenticatedUser = User("srvPDS", "password")
        assert(!ldap.result(user = authenticatedUser))
    }
}
