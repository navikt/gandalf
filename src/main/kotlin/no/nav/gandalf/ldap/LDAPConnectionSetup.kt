package no.nav.gandalf.ldap

import com.unboundid.ldap.sdk.DisconnectType
import com.unboundid.ldap.sdk.LDAPConnection
import com.unboundid.ldap.sdk.LDAPConnectionOptions
import com.unboundid.ldap.sdk.LDAPConnectionPool
import com.unboundid.ldap.sdk.LDAPException
import com.unboundid.util.ssl.SSLUtil
import com.unboundid.util.ssl.TrustAllTrustManager
import mu.KotlinLogging
import no.nav.gandalf.config.LdapConfig
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger { }

@Component
class LDAPConnectionSetup(
    val ldapConfig: LdapConfig,
) : AutoCloseable {
    private val connectOptions =
        LDAPConnectionOptions().apply {
            connectTimeoutMillis = ldapConfig.timeout
        }

    private final var ldapConnection =
        LDAPConnection(
            SSLUtil(TrustAllTrustManager()).createSSLSocketFactory(),
            connectOptions,
        )

    final var ldapConnectionPool: LDAPConnectionPool? = null

    init {
        try {
            with(ldapConnection) { connect(ldapConfig.url, ldapConfig.port) }
            ldapConnectionPool = LDAPConnectionPool(ldapConnection, NUM_CONNECTIONS)
            log.info { "Successfully connected to $ldapConfig" }
        } catch (e: LDAPException) {
            log.error { "LDAP operations against $ldapConfig will fail - $e" }
            with(ldapConnection) {
                setDisconnectInfo(
                    DisconnectType.IO_ERROR,
                    "Error when connecting to LDAPS $ldapConfig",
                    e,
                )
            }
        }
    }

    override fun close() {
        log.debug { "Closing ldap connection $ldapConfig" }
        ldapConnection.close()
    }

    val connectionOk = ldapConnectionPool != null

    val pool get() = ldapConnectionPool ?: throw IllegalStateException("LDAP connection not established")

    val testUsername = ldapConfig.srvTestUsername

    val testPassword = ldapConfig.srvTestPassword

    companion object {
        const val NUM_CONNECTIONS = 30
    }
}
