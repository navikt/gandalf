package no.nav.gandalf.ldap

import mu.KotlinLogging
import java.nio.charset.Charset
import kotlin.math.min

private val log = KotlinLogging.logger { }

internal fun testSrvUserPassBeforeBind(ldap: LDAPConnectionSetup, dn: String, pwd: String) {
    if (ldap.testUsername.isNotEmpty() && dn.contains(ldap.testUsername, ignoreCase = true)) {
        val lengthOfRequestPw = pwd.length
        val lengthOfVaultPw = ldap.testPassword.length
        log.info { "Charset: ${Charset.defaultCharset().displayName()}" }
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
