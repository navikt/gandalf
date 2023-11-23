package no.nav.gandalf.accesstoken.saml

import com.nimbusds.oauth2.sdk.OAuth2Error
import java.io.IOException
import java.io.StringReader
import java.io.StringWriter
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.xml.crypto.KeySelector
import javax.xml.crypto.MarshalException
import javax.xml.crypto.XMLStructure
import javax.xml.crypto.dsig.CanonicalizationMethod
import javax.xml.crypto.dsig.DigestMethod
import javax.xml.crypto.dsig.Reference
import javax.xml.crypto.dsig.SignatureMethod
import javax.xml.crypto.dsig.Transform
import javax.xml.crypto.dsig.XMLSignature
import javax.xml.crypto.dsig.XMLSignatureException
import javax.xml.crypto.dsig.XMLSignatureFactory
import javax.xml.crypto.dsig.dom.DOMSignContext
import javax.xml.crypto.dsig.dom.DOMValidateContext
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec
import javax.xml.crypto.dsig.spec.TransformParameterSpec
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import no.nav.gandalf.accesstoken.ClockSkew
import no.nav.gandalf.accesstoken.OAuthException
import no.nav.gandalf.keystore.KeyStoreReader
import org.slf4j.LoggerFactory
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import org.xml.sax.SAXException

class SamlObject : ClockSkew {
    var issuer: String? = null
    var id: String? = null
        private set
    var nameID: String? = null
    var dateNotBefore: ZonedDateTime? = null
    var notOnOrAfter: ZonedDateTime? = null
        private set
    var issueInstant: ZonedDateTime? = null
    var now: ZonedDateTime
    var authenticationLevel: String? = null
    var consumerId: String? = null
    var identType: String? = null
    var auditTrackingId: String? = null
    private var signatureNode: Node? = null
    val format: DateTimeFormatter = DateTimeFormatter.ISO_INSTANT // DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private val log = LoggerFactory.getLogger(javaClass)

    constructor() {
        // this.now = ZonedDateTime.now();
        now = ZonedDateTime.parse(ZonedDateTime.now().format(format))
        setId("SAML-" + UUID.randomUUID().toString())
    }

    constructor(now: ZonedDateTime) {
        this.now = ZonedDateTime.parse(now.format(format))
        setId("SAML-" + UUID.randomUUID().toString())
    }

    @Throws(ParserConfigurationException::class, SAXException::class, IOException::class)
    fun read(samlToken: String?) {
        log.info("Reading SAML token from String")
        // get document elements
        val dbFact = DocumentBuilderFactory.newInstance()
        dbFact.isNamespaceAware = true
        val docBuilder = dbFact.newDocumentBuilder()
        val doc = docBuilder.parse(InputSource(StringReader(samlToken!!)))
        doc.documentElement.normalize()
        // read Id
        var map = doc.firstChild.attributes
        var node = map.getNamedItem("ID")
        if (node != null) {
            id = node.nodeValue
        }
        // read IssueInstant
        node = map.getNamedItem("IssueInstant")
        if (node != null) {
            issueInstant = ZonedDateTime.parse(node.nodeValue)
        }
        // read Issuer
        var nList = doc.getElementsByTagName("saml2:Issuer")
        issuer = if (nList.length != 0) {
            nList.item(0).textContent
        } else {
            null
        }
        // read NameID (Subject)
        nList = doc.getElementsByTagName("saml2:NameID")
        nameID = if (nList.length != 0) {
            nList.item(0).textContent
        } else {
            null
        }
        // read Conditions
        nList = doc.getElementsByTagName("saml2:Conditions")
        if (nList.length != 0) {
            // read Conditions: NotBefore
            map = nList.item(0).attributes
            node = map.getNamedItem("NotBefore")
            dateNotBefore = if (node != null) {
                ZonedDateTime.parse(node.nodeValue)
            } else {
                null
            }
            // read Conditions: NotOnOrAfter
            node = map.getNamedItem("NotOnOrAfter")
            notOnOrAfter = if (node != null) {
                ZonedDateTime.parse(node.nodeValue)
            } else {
                null
            }
        }
        // read Attributes: identType, authenticationLevel, consumerId, auditTrackingId
        nList = doc.getElementsByTagName("saml2:Attribute")
        val vList = doc.getElementsByTagName("saml2:AttributeValue")
        var attrName: String
        for (i in 0 until nList.length) {
            attrName = nList.item(i).attributes.getNamedItem("Name").nodeValue
            when (attrName) {
                "authenticationLevel" -> {
                    authenticationLevel = vList.item(i).textContent
                }
                "consumerId" -> {
                    consumerId = vList.item(i).textContent
                }
                "identType" -> {
                    identType = vList.item(i).textContent
                }
                "auditTrackingId" -> {
                    auditTrackingId = vList.item(i).textContent
                }
            }
        }
        // read Signature
        nList = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature")
        signatureNode = if (nList.length != 0) {
            nList.item(0)
        } else {
            null
        }
    }

    @Throws(MarshalException::class, XMLSignatureException::class)
    fun validate(keySelector: KeySelector?) {
        val maxClockSkew = getMaxClockSkew()
        val nbfClockSkew = now.toInstant().plusSeconds(maxClockSkew)
        val noaClockSkew = now.toInstant().minusSeconds(maxClockSkew)
        val message: String
        // validate issuer
        if (issuer.isNullOrEmpty()) {
            message = "Invalid SAML token: Issuer is empty"
            throw OAuthException(OAuth2Error.INVALID_REQUEST.setDescription(message)).also { log.warn(message) }
        }

        // validate NameID
        if (nameID.isNullOrEmpty()) {
            message = "Invalid SAML token: NameID is empty"
            throw OAuthException(OAuth2Error.INVALID_REQUEST.setDescription(message)).also { log.warn(message) }
        }

        // validate NotBefore
        if (dateNotBefore != null && nbfClockSkew.isBefore(dateNotBefore!!.toInstant())) {
            message = "Invalid SAML token: condition nbf: $dateNotBefore, is before: $nbfClockSkew"
            throw OAuthException(
                OAuth2Error.INVALID_REQUEST.setDescription(message)
            ).also { log.warn(message) }
        }
        // validate NotOnOrAfter
        if (noaClockSkew.compareTo(notOnOrAfter!!.toInstant()) == 0 ||
            noaClockSkew.isAfter(notOnOrAfter!!.toInstant())
        ) {
            message = "Invalid SAML token: condition notOnOrAfter: $notOnOrAfter, is not on or after: $noaClockSkew"
            throw OAuthException(
                OAuth2Error.INVALID_REQUEST.setDescription(message).also { log.warn(message) }
            )
        }

        // no validation on attributes...?

        // validate Signature
        if (signatureNode == null) {
            message = "Invalid SAML token: Signature is missing"
            throw OAuthException(
                OAuth2Error.INVALID_REQUEST.setDescription(message)
            ).also { log.warn(message) }
        }
        val valContext = DOMValidateContext(keySelector, signatureNode)
        if (SUPPORT_RSA_SHA1) {
            valContext.setProperty("org.jcp.xml.dsig.secureValidation", false)
        }

        valContext.setIdAttributeNS(signatureNode!!.parentNode as Element, null, "ID")
        val factory = XMLSignatureFactory.getInstance("DOM")
        val signature = factory.unmarshalXMLSignature(valContext)
        if (!signature.validate(valContext)) {
            if (!signature.signatureValue.validate(valContext)) {
                message = "Invalid SAML token: Signature validation failed"
                throw OAuthException(
                    OAuth2Error.INVALID_REQUEST.setDescription(message)
                ).also { log.warn(message) }
            }
            for (ref in signature.signedInfo.references) {
                if (!(ref as Reference).validate(valContext)) {
                    message = "Invalid SAML token: Signature validation failed on reference ${ref.uri}"
                    throw OAuthException(
                        OAuth2Error.INVALID_REQUEST.setDescription(message)
                    ).also { log.warn(message) }
                }
            }
            message = "Invalid SAML token: validation failed"
            throw OAuthException(OAuth2Error.INVALID_REQUEST.setDescription(message)).also { log.warn(message) }
        }
        message = "SAML validation is OK for issuer: $issuer"
        log.info(message)
    }

    fun expiresIn(): Long {
        return if (notOnOrAfter != null) {
            ChronoUnit.SECONDS.between(now, notOnOrAfter)
        } else -1
    }

    private fun setId(id: String) {
        this.id = id
    }

    fun setDuration(duration: Long) {
        issueInstant = now
        dateNotBefore = issueInstant
        notOnOrAfter = issueInstant!!.plusSeconds(duration)
    }

    override fun toString(): String {
        return "Issuer: " + issuer + " nameID: " + nameID + " notBefore: " + dateNotBefore + " notOnOrAfter: " + notOnOrAfter +
            " ConsumerId: " + consumerId + " IdentType: " + identType + " Authlevel: " + authenticationLevel
    }

    override fun getMaxClockSkew(): Long {
        return CLOCK_SKEW
    }

    override fun setMaxClockSkew(maxClockSkewSeconds: Long?) {
        if (maxClockSkewSeconds != null) CLOCK_SKEW = maxClockSkewSeconds
    }

    @Throws(Exception::class)
    fun getSignedSaml(keyStoreReader: KeyStoreReader): String {
        return try {
            val samlToken = getUnsignedSaml(this)
            val docFac = DocumentBuilderFactory.newInstance()
            docFac.isNamespaceAware = true
            val docBuilder = docFac.newDocumentBuilder()
            val doc = docBuilder.parse(InputSource(StringReader(samlToken)))
            doc.documentElement.normalize()
            val assertionNode = doc.firstChild
            val nextSibling = assertionNode.firstChild.nextSibling

            // SignedInfo
            val signFac = XMLSignatureFactory.getInstance("DOM")
            val tList: MutableList<Transform> = ArrayList()
            tList.add(signFac.newTransform(Transform.ENVELOPED, null as TransformParameterSpec?))
            tList.add(signFac.newTransform(CanonicalizationMethod.EXCLUSIVE, null as TransformParameterSpec?))
            val ref = signFac.newReference("#$id", signFac.newDigestMethod(DigestMethod.SHA1, null), tList, null, null)
            val si = signFac.newSignedInfo(
                signFac.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, null as C14NMethodParameterSpec?),
                signFac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                listOf(ref)
            )
            val cert: X509Certificate? = keyStoreReader.signingCertificate
            if (cert == null) {
                log.error("Failed to find signing certificate in keystore")
                throw IllegalArgumentException("Failed to find signing certificate in keystore")
            }
            val privateKey: PrivateKey? = keyStoreReader.getPrivateKey()
            if (privateKey == null) {
                log.error("Failed to find PrivateKey in keystore")
                throw IllegalArgumentException("Failed to find PrivateKey in keystore")
            }
            val kiFac = signFac.keyInfoFactory
            val x509IssuerSerial = kiFac.newX509IssuerSerial(cert.issuerDN.name, cert.serialNumber)
            val dList: MutableList<Any?> = ArrayList()
            dList.add(cert)
            dList.add(x509IssuerSerial)
            val x509data = kiFac.newX509Data(dList)
            val kiList: MutableList<XMLStructure> = ArrayList()
            kiList.add(x509data)
            val ki = kiFac.newKeyInfo(kiList)

            // Signature
            val signature = signFac.newXMLSignature(si, ki)
            val context = DOMSignContext(privateKey, assertionNode, nextSibling)
            context.setIdAttributeNS(assertionNode as Element, null, "ID")
            signature.sign(context)

            // Transform to XML
            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            transformer.setOutputProperty("omit-xml-declaration", "yes")
            val source = DOMSource(doc)
            val result = StreamResult(StringWriter())
            transformer.transform(source, result)
            result.writer.toString()
        } catch (e: Exception) {
            log.error("Error: " + e.message, e)
            throw Exception("Error: " + e.message, e)
        }
    }

    companion object {
        private var CLOCK_SKEW = 60L
        private const val SUPPORT_RSA_SHA1 = true
    }
}
