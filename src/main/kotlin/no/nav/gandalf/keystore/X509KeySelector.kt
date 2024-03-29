package no.nav.gandalf.keystore

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import javax.xml.crypto.AlgorithmMethod
import javax.xml.crypto.KeySelector
import javax.xml.crypto.KeySelectorException
import javax.xml.crypto.KeySelectorResult
import javax.xml.crypto.XMLCryptoContext
import javax.xml.crypto.XMLStructure
import javax.xml.crypto.dsig.keyinfo.KeyInfo
import javax.xml.crypto.dsig.keyinfo.X509Data

private val log = KotlinLogging.logger { }

@Component
class X509KeySelector(
    @Value("\${nav.truststore.path}") private val truststoreFile: String?,
    @Value("\${nav.truststore.password}") private val truststorePassword: String?,
) : KeySelector() {
    private var trustManager: X509TrustManager? = null

    init {
        setOrThrow(TRUSTSTORE_FILENAME_PROPERTYNAME, truststoreFile)
        setOrThrow(TRUSTSTORE_PASSWORD_PROPERTYNAME, truststorePassword)
    }

    private final fun setOrThrow(
        propertyName: String,
        value: String?,
    ) {
        System.setProperty(
            propertyName,
            value ?: throw NullPointerException("$propertyName, is not set!"),
        )
    }

    @Throws(KeySelectorException::class)
    override fun select(
        keyInfo: KeyInfo,
        purpose: Purpose,
        method: AlgorithmMethod,
        context: XMLCryptoContext,
    ): KeySelectorResult {
        for (`object` in keyInfo.content) {
            val info = `object` as XMLStructure
            if (info is X509Data) {
                for (cert in info.content) {
                    if (cert is X509Certificate) {
                        if (algEquals(method.algorithm, cert.publicKey.algorithm)) {
                            try {
                                if (trustManager == null) {
                                    trustManager = x509TrustManager
                                }
                                trustManager!!.checkServerTrusted(arrayOf(cert), "RSA")
                            } catch (e: CertificateException) {
                                log.info("The certificate is not trusted by a Root CA" + e.message)
                                throw RuntimeException("This certificate is not trusted by a Root CA", e)
                            }
                        }
                        return KeySelectorResult { cert.publicKey }
                    }
                }
            }
        }
        throw KeySelectorException("Failed to find X509 Certificate")
    }

    val x509TrustManager: X509TrustManager
        get() {
            log.info("OidcTokenIssuer - Setup trustManager with: getX509TrustManager")
            var trustStore: KeyStore? = null
            var tsis: InputStream?
            readKeyStoreAndHandle {
                when {
                    truststoreFile.isNullOrEmpty() -> {
                        throw RuntimeException(
                            "Failed to load truststore, system property '$TRUSTSTORE_FILENAME_PROPERTYNAME' is null or empty!",
                        )
                    }
                    truststorePassword.isNullOrEmpty() -> {
                        log.error("System property '$TRUSTSTORE_PASSWORD_PROPERTYNAME' is null or empty!")
                        throw RuntimeException(
                            "Failed to load truststore, system property '$TRUSTSTORE_PASSWORD_PROPERTYNAME' is null or empty!",
                        )
                    }
                    else -> {
                        trustStore = KeyStore.getInstance("JKS")
                        tsis = FileInputStream(truststoreFile)
                        trustStore!!.load(tsis, truststorePassword.toCharArray())
                        if (trustStore!!.size() == 0) {
                            log.error("Error: truststore is empty. Loaded from file '$truststoreFile'")
                            throw RuntimeException("Error: truststore is empty")
                        }
                    }
                }
            }

            val tmfactory =
                trustManageFacHandle {
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
                        this.run {
                            init(trustStore)
                        }
                    }
                }
            for (trustManager in tmfactory.trustManagers) when (trustManager) {
                is X509TrustManager -> {
                    return trustManager
                }
            }
            log.error("Failed to get X509TrustManager")
            throw RuntimeException("Failed to get X509TrustManager")
        }

    companion object {
        const val TRUSTSTORE_FILENAME_PROPERTYNAME = "javax.net.ssl.trustStore"
        const val TRUSTSTORE_PASSWORD_PROPERTYNAME = "javax.net.ssl.trustStorePassword"

        fun algEquals(
            algURI: String,
            algName: String,
        ): Boolean {
            return algName.equals(
                "RSA",
                ignoreCase = true,
            ) && algURI.equals("http://www.w3.org/2000/09/xmldsig#rsa-sha1", ignoreCase = true)
        }
    }

    fun trustManageFacHandle(block: () -> TrustManagerFactory): TrustManagerFactory {
        try {
            return block.invoke()
        } catch (e: NoSuchAlgorithmException) {
            log.error("Failed to get instance of trustmanagerfactory" + e.message)
            throw RuntimeException("Failed to get instance of trustmanagerfactory", e)
        } catch (e: KeyStoreException) {
            log.error("Failed to init trustmanagerfactory" + e.message)
            throw RuntimeException("Failed to init trustmanagerfactory", e)
        }
    }

    fun readKeyStoreAndHandle(block: () -> Unit) {
        try {
            block.invoke()
        } catch (e: KeyStoreException) {
            log.error("Failed to get trustStore instance" + e.message)
            throw RuntimeException("Failed to get trustStore instance", e)
        } catch (e: NoSuchAlgorithmException) {
            log.error("Failed to load truststore" + e.message)
            throw RuntimeException("Failed to load truststore", e)
        } catch (e: CertificateException) {
            log.error("Failed to load truststore" + e.message)
            throw RuntimeException("Failed to load truststore", e)
        } catch (e: IOException) {
            log.error("Failed to load truststore" + e.message)
            throw RuntimeException("Failed to load truststore", e)
        }
    }
}
