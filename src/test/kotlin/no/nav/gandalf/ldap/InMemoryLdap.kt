package no.nav.gandalf.ldap

import com.unboundid.ldap.listener.InMemoryDirectoryServer
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig
import com.unboundid.ldap.listener.InMemoryListenerConfig
import com.unboundid.ldap.sdk.OperationType
import com.unboundid.ldap.sdk.schema.Schema
import com.unboundid.util.ssl.KeyStoreKeyManager
import com.unboundid.util.ssl.SSLUtil
import com.unboundid.util.ssl.TrustAllTrustManager
import com.unboundid.util.ssl.TrustStoreTrustManager
import mu.KotlinLogging

class InMemoryLdap : AutoCloseable {
    private val lPort = 11389
    private val log = KotlinLogging.logger { }
    private val lName = "LDAPS"

    private val imConf =
        InMemoryDirectoryServerConfig("dc=test,dc=local").apply {

            try {

                val kStore = "src/test/resources/inmds.jks"
                val tlsCF = SSLUtil(TrustAllTrustManager()).createSSLSocketFactory()
                val tlsSF =
                    SSLUtil(
                        KeyStoreKeyManager(kStore, "password".toCharArray(), "JKS", "inmds"),
                        TrustStoreTrustManager(kStore)
                    ).createSSLServerSocketFactory()

                setListenerConfigs(
                    InMemoryListenerConfig.createLDAPSConfig(
                        lName,
                        null,
                        lPort,
                        tlsSF,
                        tlsCF
                    )
                )

                // require authentication for most operations except bind
                setAuthenticationRequiredOperationTypes(
                    OperationType.COMPARE,
                    OperationType.SEARCH,
                    OperationType.ADD,
                    OperationType.MODIFY,
                    OperationType.DELETE
                )
                // let the embedded server use identical schema as apache DS configured for AD support (group and sAMAcc..)
                schema = Schema.getSchema("src/test/resources/apacheDS.ldif")
            } catch (e: Exception) {
                log.error { "$e" }
            }
        }

    private val imDS =
        InMemoryDirectoryServer(imConf).apply {
            try {
                importFromLDIF(true, "src/test/resources/users.ldif")
            } catch (e: Exception) {
                log.error { "$e" }
            }
        }

    fun start() = imDS.startListening(lName)

    fun isAlive() = imDS.connection.isConnected

    override fun close() = imDS.shutDown(true)
}
