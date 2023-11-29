package no.nav.gandalf.keystore

import no.nav.gandalf.config.KeystoreReaderConfig
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import java.io.File
import java.util.Base64

@SpringBootTest
class KeystoreConfigTest {
    private val file = ClassPathResource("keystore.jks")
    private val base64EncodedFile = Base64.getEncoder().encodeToString(file.inputStream.readBytes())

    @Test
    fun `Load A Remote KeyStore and Decode`() {
        val keystoreReaderConfig =
            KeystoreReaderConfig(
                keystoreFile = base64EncodedFile,
                keystorePassword = "testkeystore1234",
                profile = "remote"
            )
        // Decoded file, som blitt lagret på temp uri - skal vare identisk med orginalen
        assert(
            File(keystoreReaderConfig.loadKeyStoreFromBase64ToFile())
                .readText() == File(file.uri).readText()
        )
    }

    @Test
    fun `Keystore is empty`() {
        val keystoreReaderConfig =
            KeystoreReaderConfig(
                keystoreFile = null,
                keystorePassword = "testkeystore1234",
                profile = "remote"
            )
        assertThrows<RuntimeException> {
            keystoreReaderConfig.loadKeyStoreFromBase64ToFile()
        }
    }
}
