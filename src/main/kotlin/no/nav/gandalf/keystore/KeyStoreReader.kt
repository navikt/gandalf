package no.nav.gandalf.keystore

import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger { }

@Component
class KeyStoreReader(
    @Value("\${nav.keystore.file}") private val keystoreFile: String,
    @Value("\${nav.keystore.password}") private val keystorePassword: String
) {
    private var keyStore: KeyStore? = null
    private var privateKey: PrivateKey? = null
    private var cert: X509Certificate? = null

    @get:Throws(UnrecoverableKeyException::class, KeyStoreException::class, NoSuchAlgorithmException::class)
    val signingCertificate: X509Certificate?
        get() {
            when (cert) {
                null -> {
                    readKeyStore()
                }
            }
            return cert
        }

    @Throws(UnrecoverableKeyException::class, KeyStoreException::class, NoSuchAlgorithmException::class)
    fun getPrivateKey(): PrivateKey? {
        when (privateKey) {
            null -> {
                readKeyStore()
            }
        }
        return privateKey
    }

    @Throws(KeyStoreException::class, UnrecoverableKeyException::class, NoSuchAlgorithmException::class)
    private fun readKeyStore() {
        log.info("readKeyStore - Updating keystore for application")
        var tsis: InputStream? = null
        log.debug("Using keystorefile: $keystoreFile")
        readKeyStoreAndHandle {
            when {
                keystoreFile.isEmpty() -> {
                    throw RuntimeException("Failed to load keystore, system property '$keystoreFile' is null or empty!")
                }
                keystorePassword.isEmpty() -> {
                    throw RuntimeException("Failed to load keystore, system property 'local-keystore.password' is null or empty!")
                }
                else -> {
                    keyStore = KeyStore.getInstance("JKS")
                    tsis = FileInputStream(keystoreFile)
                    keyStore?.run {
                        this.load(tsis, keystorePassword.toCharArray())
                    }
                    if (keyStore!!.size() == 0) {
                        log.error("Error: keystore is empty. Loaded from file '$keystoreFile'")
                        throw RuntimeException("Error: keystore is empty")
                    }
                }
            }
        }

        // find certificate and key to sign
        keyStore!!.aliases().apply {
            while (this.hasMoreElements()) {
                val keyAlias = this.nextElement()
                cert = keyStore!!.getCertificate(keyAlias) as X509Certificate // eller keyStore.getCertificateChain(keyAlias)[0];
                val keyUsage = cert!!.keyUsage
                privateKey = keyStore!!.getKey(keyAlias, keystorePassword.toCharArray()) as PrivateKey
                if (privateKey != null && (keyUsage == null || keyUsage[0] || keyUsage[1])) { // if keyUsage is not specified or is digitalSignature or nonRepudation
                    break
                }
            }
        }
    }

    fun readKeyStoreAndHandle(
        block: () -> Unit
    ) {
        try {
            block.invoke()
        } catch (e: KeyStoreException) {
            log.error("Failed to get keyStore instance" + e.message)
            throw RuntimeException("Failed to get keyStore instance", e)
        } catch (e: NoSuchAlgorithmException) {
            log.error("Failed to load keytstore" + e.message)
            throw RuntimeException("Failed to load keystore", e)
        } catch (e: CertificateException) {
            log.error("Failed to load keytstore" + e.message)
            throw RuntimeException("Failed to load keystore", e)
        } catch (e: IOException) {
            log.error("Failed to load keytstore" + e.message)
            throw RuntimeException("Failed to load keystore", e)
        }
    }
}
