package no.nav.gandalf.ldap

import com.unboundid.ldap.sdk.LDAPException
import com.unboundid.ldap.sdk.ResultCode
import mu.KotlinLogging
import no.nav.gandalf.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import kotlin.math.min

private val log = KotlinLogging.logger { }

private const val attributeName = "cn"

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
                        acc || authenticated(
                            dn,
                            user.password,
                            acc
                        )
                    }.also {
                        when (it) {
                            true -> log.info { "Successful bind of ${user.username} to ${ldap.ldapConfig}" }
                            false -> throw LDAPException(ldapException).also { log.error { "Could not bind ${user.username} to ${ldap.ldapConfig}. Error message: ${ldapException?.message ?: "no message"}" } }
                        }
                    }
                } catch (t: Throwable) {
                    throw t
                }
            }
        }

    private fun resolveDNs(user: String): List<String> = user.let { username ->
        val dnPrefix = "$attributeName=$username"
        val dnPostfix = ldap.ldapConfig.base
        val srvAccounts = listOf("OU=ApplAccounts,OU=ServiceAccounts", "OU=ServiceAccounts")
        srvAccounts.map { srvAccount -> "$dnPrefix,$srvAccount,$dnPostfix" }
    }

    private fun authenticated(dn: String, pwd: String, alreadyAuthenticated: Boolean) =
        when {
            alreadyAuthenticated -> true
            else ->
                try {
                    testSrvUserPassBeforeBind(dn, pwd)
                    (ldap.pool.bind(dn, pwd).resultCode == ResultCode.SUCCESS)
                } catch (e: LDAPException) {
                    ldapException = e
                    false
                }
        }

    private fun testSrvUserPassBeforeBind(dn: String, pwd: String) {
        if (ldap.testUsername.isNotEmpty() && dn.contains(ldap.testUsername, ignoreCase = true)) {
            val lengthOfRequestPw = pwd.length
            val lengthOfVaultPw = ldap.testPassword.length
            // val difference = StringUtils.difference(ldap.testPassword, pwd)
            log.info { "Username in Vault: ${ldap.testUsername}" }
            log.info { "Got password from request with length: $lengthOfRequestPw to match Vault password length: $lengthOfVaultPw for: $dn" }
            when (pwd) {
                ldap.testPassword -> {
                    log.info { "Password in Vault for: $dn MATCH password in request" }
                }
                else -> {
                    log.info { "Password in Vault for: $dn do NOT match password in request" }
                    (0 until min(lengthOfRequestPw, lengthOfVaultPw)).forEach {
                        if (pwd[it] != ldap.testPassword[it]) {
                            log.info { "Request Char: ${pwd[it]} != Vault Char: ${ldap.testPassword[it]}" }
                        }
                    }
                }
            }
        }
    }
}
