package no.nav.gandalf.ldap

import no.nav.gandalf.config.LdapConfig
import no.nav.gandalf.model.User
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows
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
        base = "dc=test,dc=local",
        port = 11389,
        remote = "false",
        timeout = 1_000
    )

    @Test
    fun `Authenticated User Under OU=ServiceAccounts`() {
        val ldap = LDAPAuthentication(LDAPConnectionSetup(ldapConfig = ldapConfig))
        val authenticatedUser = User("srvPDP", "password")
        assert(ldap.result(user = authenticatedUser))
    }

    @Test
    fun `Authenticated User Under OU=ApplAccounts,OU=ServiceAccounts`() {
        val ldap = LDAPAuthentication(LDAPConnectionSetup(ldapConfig = ldapConfig))
        val authenticatedUser = User("srvaltutkanal", "password")
        assert(ldap.result(user = authenticatedUser))
    }

    @Test
    fun `UnAuthorized User`() {
        val ldap = LDAPAuthentication(LDAPConnectionSetup(ldapConfig = ldapConfig))
        val authenticatedUser = User("srvPDS", "password")
        assertThrows<Exception> { ldap.result(user = authenticatedUser) }
    }
}
