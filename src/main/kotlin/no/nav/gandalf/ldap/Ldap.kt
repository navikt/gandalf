package no.nav.gandalf.ldap

import com.unboundid.ldap.sdk.LDAPConnection
import com.unboundid.ldap.sdk.LDAPException
import com.unboundid.ldap.sdk.LDAPSearchException
import com.unboundid.ldap.sdk.SearchResult
import com.unboundid.ldap.sdk.SearchScope
import mu.KotlinLogging
import no.nav.gandalf.config.LdapConfig
import no.nav.gandalf.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger { }

@Component
class Ldap(
    @Autowired val ldapConfig: LdapConfig
) {

    fun result(user: User) =
        try {
            search(user)
            true
        } catch (t: Throwable) {
            throw t
        }

    fun search(
        user: User
    ): SearchResult = tryToAuthenticate(user) {
        val connection = LDAPConnection(ldapConfig.url, ldapConfig.port)
        connection.search(
            ldapConfig.base,
            SearchScope.SUB,
            "(cn=${user.username})"
        ).apply {
            when {
                this.entryCount > 0 -> {
                    val entry = this.searchEntries[0]
                    log.info { "User found: ${user.username}, entry: ${entry.dn}" }
                    connection.bind(entry.dn, user.password)
                }
                else -> {
                    throw Exception("User not found: ${user.username}")
                }
            }
        }
    }

    fun tryToAuthenticate(
        user: User,
        block: () -> SearchResult
    ) =
        try {
            block()
        } catch (t: Throwable) {
            when (t) {
                is LDAPException -> {
                    log.error { "Could not Authenticate user: ${user.username}, message: ${t.message}" }
                    throw t
                }
                is LDAPSearchException -> {
                    log.error { "Could not find user: ${user.username}, message: ${t.message}" }
                    throw t
                }
                else -> throw t
            }
        }
}
