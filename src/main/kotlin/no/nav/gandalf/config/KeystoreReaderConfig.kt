package no.nav.gandalf.config

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.nio.file.Files
import java.nio.file.Path
import java.util.Base64

private val log = KotlinLogging.logger { }
private const val TARGET = "keystore.file"

@Configuration
data class KeystoreReaderConfig(
    @Value("\${nav.keystore.file}")
    val keystoreFile: String?,
    @Value("\${nav.keystore.password}")
    val keystorePassword: String,
    @Value("\${spring.profiles.active}")
    val profile: String
) {

    fun loadKeyStoreFromBase64ToFile() =
        when (profile) {
            "test", "local" -> {
                log.info("Loading $profile keystore")
                keystoreFile!!
            }
            else -> {
                log.info("Loading $profile keystore")
                System.setProperty("javax.net.ssl.keyStoreType", "JKS")
                decodeFile()
            }
        }

    fun decodeFile() =
        when {
            !keystoreFile.isNullOrEmpty() -> {
                log.debug("Base64Encoded keystore: $keystoreFile")
                writeByteArraysToFile(TARGET, Base64.getDecoder().decode(keystoreFile)).toString()
            }
            else -> {
                log.error("Could not decode remote keystore file, keystore is empty")
                throw RuntimeException()
            }
        }

    fun writeByteArraysToFile(fileName: String, content: ByteArray): Path =
        try {
            Files.createTempFile(fileName, ".jks").toAbsolutePath().apply {
                Files.write(this, content)
            }
        } catch (t: Throwable) {
            log.error("Error Message: ${t.message}")
            throw t
        }
}
