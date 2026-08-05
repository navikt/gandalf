package no.nav.gandalf.xml

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.atomic.AtomicBoolean
import javax.xml.crypto.URIReference
import javax.xml.crypto.URIReferenceException
import javax.xml.crypto.dom.DOMCryptoContext

class SecureXmlSecurityTest {
    @Test
    fun `rejects XML containing a doctype`() {
        val xmlWithDoctype = """<!DOCTYPE root><root/>"""

        assertThrows<Exception> {
            SecureXml.documentBuilderFactory().newDocumentBuilder().parse(xmlWithDoctype.byteInputStream())
        }
    }

    @Test
    fun `rejects a doctype before resolving its external entity`() {
        val resolverCalled = AtomicBoolean(false)
        val builder = SecureXml.documentBuilderFactory().newDocumentBuilder()
        builder.setEntityResolver { _, _ ->
            resolverCalled.set(true)
            null
        }

        assertThrows<Exception> {
            builder.parse("<!DOCTYPE root SYSTEM \"https://example.invalid/test.dtd\"><root/>".byteInputStream())
        }

        check(!resolverCalled.get())
    }

    @Test
    fun `rejects non-fragment XML signature references`() {
        val reference =
            object : URIReference {
                override fun getURI() = "https://example.invalid/reference.xml"

                override fun getType() = null
            }

        assertThrows<URIReferenceException> {
            SecureXml.sameDocumentUriDereferencer().dereference(reference, object : DOMCryptoContext() {})
        }
    }
}
