package no.nav.gandalf.ldap

import com.unboundid.ldap.listener.InMemoryDirectoryServer
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig
import com.unboundid.ldap.listener.InMemoryListenerConfig
import com.unboundid.ldap.sdk.LDAPException
import com.unboundid.ldap.sdk.OperationType
import com.unboundid.util.ssl.SSLUtil
import com.unboundid.util.ssl.TrustAllTrustManager
import java.security.GeneralSecurityException

class InMemoryLdap {
    private var inMemConf: InMemoryDirectoryServerConfig? = null

    // Kan brukes hvis du vil teste SSL
    private val KeyStore = "src/test/resources/inmds.jks"

    // private SSLServerSocketFactory tlsSF;
    private var imDS: InMemoryDirectoryServer? = null
    private fun initLDAPServer() {
        try {
            inMemConf = InMemoryDirectoryServerConfig("dc=test,dc=local")
            // val tlsCF = SSLUtil(TrustAllTrustManager()).createSSLSocketFactory()
            // tlsSF = new SSLUtil(new KeyStoreKeyManager(KeyStore, "password".toCharArray(), "JKS", "inmds"), new TrustStoreTrustManager(KeyStore)).createSSLServerSocketFactory();
        } catch (e: LDAPException) {
            e.printStackTrace()
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
        }
    }

    private fun configServer() {
        println("ConfigServer is called - getting config in place")
        initLDAPServer()
        try {
            inMemConf!!.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("LDAP", 11389))
            // InMemoryListenerConfig.createLDAPSConfig("LDAPS", null, 11636, tlsSF, tlsCF));

            // must bind before compare, equal to non-anonymous access./
            inMemConf!!.setAuthenticationRequiredOperationTypes(OperationType.COMPARE)
            inMemConf!!.schema = null
            imDS = InMemoryDirectoryServer(inMemConf)
            imDS!!.importFromLDIF(true, "src/test/resources/users.ldif")
        } catch (e: LDAPException) {
            e.printStackTrace()
        }
    }

    @Throws(LDAPException::class)
    fun start() {
        println("Start In-memory ldap server")
        configServer()
        imDS!!.startListening("LDAP")
        println("Server is up and running")
    }

    fun stop() {
        imDS!!.shutDown(true)
    }
}
