package no.nav.gandalf.ldap

import com.unboundid.ldap.sdk.LDAPException
import com.unboundid.ldap.sdk.ResultCode
import mu.KotlinLogging
import no.nav.gandalf.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger { }

private const val ATTRIBUTE_NAME = "cn"

@Component
class LDAPAuthentication(
    @Autowired val ldap: LDAPConnectionSetup
) {
    var ldapException: LDAPException? = null

    fun result(user: User) =
        when {
            !ldap.connectionOk -> {
                throw LDAPException(ldapException).also { log.error { "Cannot authenticate, connection to ldap is down" } }
            }
            else -> {
                try {
                    resolveDNs(user.username).fold(false) { acc, dn ->
                        acc ||
                            authenticated(
                                dn,
                                user.password,
                                acc
                            )
                    }.also {
                        when (it) {
                            true -> log.info { "Successful bind of ${user.username} to ${ldap.ldapConfig}" }
                            false -> throw LDAPException(ldapException).also {
                                log.error { "Could not bind ${user.username} to ${ldap.ldapConfig}. Error message: ${ldapException?.message ?: "no message"}" }
                            }
                        }
                    }
                } catch (t: Throwable) {
                    throw t
                }
            }
        }

    private fun resolveDNs(user: String): List<String> =
        user.let { username ->
            val dnPrefix = "$ATTRIBUTE_NAME=$username"
            val dnPostfix = ldap.ldapConfig.base
            val srvAccounts = listOf("OU=ApplAccounts,OU=ServiceAccounts", "OU=ServiceAccounts")
            srvAccounts.map { srvAccount -> "$dnPrefix,$srvAccount,$dnPostfix" }
        }

    private fun authenticated(
        dn: String,
        pwd: String,
        alreadyAuthenticated: Boolean
    ) = when {
        alreadyAuthenticated -> true
        else ->
            try {
                (ldap.pool.bind(dn, pwd).resultCode == ResultCode.SUCCESS)
            } catch (e: LDAPException) {
                ldapException = e
                false
            }
    }
}
