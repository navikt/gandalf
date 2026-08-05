package no.nav.gandalf.xml

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.atomic.AtomicBoolean
import javax.xml.crypto.URIReference
import javax.xml.crypto.URIReferenceException
import javax.xml.crypto.dom.DOMCryptoContext
import javax.xml.transform.stream.StreamSource

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
    fun `rejects external general entities`() {
        assertThrows<Exception> {
            SecureXml.documentBuilder().parse(
                "<!DOCTYPE root [<!ENTITY entity SYSTEM \"https://example.invalid/entity\">]><root>&entity;</root>".byteInputStream(),
            )
        }
    }

    @Test
    fun `rejects external parameter entities`() {
        assertThrows<Exception> {
            SecureXml.documentBuilder().parse(
                "<!DOCTYPE root [<!ENTITY % entity SYSTEM \"https://example.invalid/entity\">%entity;]><root/>".byteInputStream(),
            )
        }
    }

    @Test
    fun `rejects external XSLT imports`() {
        val stylesheet =
            """
            <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                <xsl:import href="https://example.invalid/stylesheet.xsl"/>
            </xsl:stylesheet>
            """.trimIndent()

        assertThrows<Exception> {
            SecureXml.transformerFactory().newTransformer(StreamSource(stylesheet.reader()))
        }
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
