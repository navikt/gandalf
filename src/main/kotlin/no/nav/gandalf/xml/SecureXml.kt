package no.nav.gandalf.xml

import org.xml.sax.EntityResolver
import org.xml.sax.SAXException
import javax.xml.XMLConstants
import javax.xml.crypto.URIDereferencer
import javax.xml.crypto.URIReference
import javax.xml.crypto.URIReferenceException
import javax.xml.crypto.XMLCryptoContext
import javax.xml.crypto.dsig.XMLSignatureFactory
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory

/**
 * Central boundary for XML security configuration.
 *
 * RED ZONE: The feature and attribute configuration here is security-critical.
 * It must fail closed: XML from callers may never cause local or network access.
 */
internal object SecureXml {
    fun documentBuilder() =
        documentBuilderFactory().newDocumentBuilder().apply {
            setEntityResolver(rejectingEntityResolver())
        }

    fun documentBuilderFactory(): DocumentBuilderFactory =
        DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            isXIncludeAware = false
            isExpandEntityReferences = false
            setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
            setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
        }

    fun transformerFactory(): TransformerFactory =
        TransformerFactory.newInstance().apply {
            setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
            setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
            setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "")
        }

    fun sameDocumentUriDereferencer(): URIDereferencer {
        val providerDereferencer = XMLSignatureFactory.getInstance("DOM").uriDereferencer
        return URIDereferencer { reference: URIReference, context: XMLCryptoContext ->
            val uri = reference.uri
            if (uri == null || !uri.startsWith("#")) {
                throw URIReferenceException("Only same-document XML signature references are allowed")
            }
            providerDereferencer.dereference(reference, context)
        }
    }

    fun rejectingEntityResolver(): EntityResolver =
        EntityResolver { _, _ ->
            throw SAXException("External XML entities are not allowed")
        }
}
