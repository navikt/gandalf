package no.nav.gandalf.xml

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.xml.sax.SAXException
import java.util.concurrent.atomic.AtomicBoolean
import javax.xml.crypto.URIReference
import javax.xml.crypto.URIReferenceException
import javax.xml.crypto.dom.DOMCryptoContext
import javax.xml.transform.stream.StreamSource

class SecureXmlSecurityTest {
    @Test
    fun `disables XInclude and entity expansion`() {
        val factory = SecureXml.documentBuilderFactory()

        assertFalse(factory.isXIncludeAware)
        assertFalse(factory.isExpandEntityReferences)
    }

    @Test
    fun `rejects XML containing a doctype`() {
        val xmlWithDoctype = """<!DOCTYPE root><root/>"""

        assertThrows<SAXException> {
            SecureXml.documentBuilderFactory().newDocumentBuilder().parse(xmlWithDoctype.byteInputStream())
        }
    }

    @Test
    fun `rejects a doctype before resolving its external entity`() {
        assertRejectedWithoutResolving("<!DOCTYPE root SYSTEM \"https://example.invalid/test.dtd\"><root/>")
    }

    @Test
    fun `rejects external general entities`() {
        assertRejectedWithoutResolving(
            "<!DOCTYPE root [<!ENTITY entity SYSTEM \"https://example.invalid/entity\">]><root>&entity;</root>",
        )
    }

    @Test
    fun `rejects external parameter entities`() {
        assertRejectedWithoutResolving(
            "<!DOCTYPE root [<!ENTITY % entity SYSTEM \"https://example.invalid/entity\">%entity;]><root/>",
        )
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

    private fun assertRejectedWithoutResolving(xml: String) {
        val resolverCalled = AtomicBoolean(false)
        val builder = SecureXml.documentBuilderFactory().newDocumentBuilder()
        builder.setEntityResolver { _, _ ->
            resolverCalled.set(true)
            null
        }

        assertThrows<SAXException> {
            builder.parse(xml.byteInputStream())
        }

        assertFalse(resolverCalled.get(), "External entity resolver must not be called")
    }
}
