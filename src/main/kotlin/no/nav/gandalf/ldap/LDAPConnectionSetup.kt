package no.nav.gandalf.ldap

import com.unboundid.ldap.sdk.DisconnectType
import com.unboundid.ldap.sdk.LDAPConnection
import com.unboundid.ldap.sdk.LDAPConnectionOptions
import com.unboundid.ldap.sdk.LDAPException
import com.unboundid.util.ssl.SSLUtil
import com.unboundid.util.ssl.TrustAllTrustManager
import mu.KotlinLogging
import no.nav.gandalf.config.LdapConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger { }

@Component
class LDAPConnectionSetup(
    @Autowired val ldapConfig: LdapConfig
) : AutoCloseable {

    private val connectOptions = LDAPConnectionOptions().apply {
        connectTimeoutMillis = ldapConfig.timeout
    }

    var ldapConnection = LDAPConnection(
        SSLUtil(TrustAllTrustManager()).createSSLSocketFactory(),
        connectOptions
    )

    init {
        try {
            ldapConnection.run {
                connect(ldapConfig.url, ldapConfig.port)
            }
            log.debug { "Successfully connected to $ldapConfig" }
        } catch (e: LDAPException) {
            log.error { "LDAP operations against $ldapConfig will fail - $e" }
            ldapConnection.run {
                setDisconnectInfo(
                    DisconnectType.IO_ERROR,
                    "Error when connecting to LDAPS $ldapConfig", e
                )
            }
        }
    }

    override fun close() {
        log.debug { "Closing ldap connection $ldapConfig" }
        ldapConnection.close()
    }

    val connectionOk = ldapConnection.isConnected
}
