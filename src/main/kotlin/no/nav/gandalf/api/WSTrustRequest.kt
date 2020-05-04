package no.nav.gandalf.api

import no.nav.gandalf.accesstoken.SamlObject
import org.apache.commons.codec.binary.Base64
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.IOException
import java.io.StringReader
import java.io.StringWriter
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.time.ZonedDateTime
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.TransformerFactoryConfigurationError
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

internal val REQUEST_TYPE_ISSUE: String = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue"
internal val REQUEST_TYPE_VALIDATE: String = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Validate"
internal val KEY_TYPE_BEARER: String = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Bearer"
internal val TOKEN_TYPE_SAML: String = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0"

class WSTrustRequest(
        var username: String? = null,
        var password: String? = null,
        var reqType: String? = null,
        var keyType: String? = null,
        var tokenType: String? = null,
        var onBehalfOf: String? = null,
        var validateTarget: String? = null
) {

    val isIssueSamlFromUNT: Boolean
        get() = (((reqType == REQUEST_TYPE_ISSUE) && (tokenType == TOKEN_TYPE_SAML) && (keyType == KEY_TYPE_BEARER) &&
                (onBehalfOf == null || onBehalfOf!!.isEmpty())))

    val isExchangeOidcToSaml: Boolean
        get() {
            return (((reqType == REQUEST_TYPE_ISSUE) && (tokenType == TOKEN_TYPE_SAML) && (keyType == KEY_TYPE_BEARER) && (
                    onBehalfOf != null) && !onBehalfOf!!.isEmpty()))
        }

    val isIssue: Boolean
        get() {
            return (reqType == REQUEST_TYPE_ISSUE)
        }

    val isValidate: Boolean
        get() {
            return (reqType == REQUEST_TYPE_VALIDATE)
        }

    val isValidateSaml: Boolean
        get() {
            return ((reqType == REQUEST_TYPE_VALIDATE) && (tokenType == TOKEN_TYPE_SAML))
        }

    @get:Throws(UnsupportedEncodingException::class)
    val decodedOidcToken: String?
        get() {
            if (onBehalfOf != null) {
                return String(Base64.decodeBase64(onBehalfOf), Charset.forName("UTF-8"))
            }
            return null
        }

    fun read(xmlReq: String) {
        try {
            val dbFact: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            dbFact.isNamespaceAware = true
            val docBuilder: DocumentBuilder = dbFact.newDocumentBuilder()
            val doc: Document = docBuilder.parse(InputSource(StringReader(xmlReq)))
            doc.documentElement.normalize()

            // get soap envelope
            val soapEnvelope: Node? = findChild(doc, "Envelope", false)

            // get UsernameToken in soap header
            val security: Node? = findChild(findChild(soapEnvelope, "Header", false), "Security", false)
            val unt: Node? = findChild(security, "UsernameToken", false)
            username = findChild(unt, "Username", false)!!.textContent
            password = findChild(unt, "Password", false)!!.textContent

            // get RequestSecurityToken in soap body
            val req: Node? = findChild(findChild(soapEnvelope, "Body", false), "RequestSecurityToken", false)
            reqType = findChild(req, "RequestType", false)!!.textContent
            keyType = findChild(req, "KeyType", false)!!.textContent

            // get tokenType
            tokenType = findChild(findChild(req, "SecondaryParameters", true) ?: req, "TokenType", false)!!.textContent
            if (isIssue) {
                // get onBehalfOf if specified
                val onBehalfOfNode: Node? = findChild(req, "OnBehalfOf", true)
                if (onBehalfOfNode != null) {
                    onBehalfOf = onBehalfOfNode.textContent
                }
            } else if (isValidateSaml) {
                // get saml assertion
                var n: Node? = findChild(req, "ValidateTarget", false)
                n = findChild(n, "SecurityTokenReference", false)
                n = findChild(n, "Embedded", false)
                n = findChild(n, "Assertion", false)
                validateTarget = nodeToString(n)
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Error when reading xml request: " + e.message)
        }
    }


    @Throws(ParserConfigurationException::class, SAXException::class, IOException::class)
    fun getResponse(samlToken: String): String? {
        val samlObj = SamlObject()
        samlObj.read(samlToken)
        return getResponse(samlToken, samlObj.issueInstant!!, samlObj.notOnOrAfter!!)
    }

    private fun getResponse(samlToken: String, issueInstant: ZonedDateTime, notOnOrAfter: ZonedDateTime) = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
            "<soapenv:Envelope xmlns:wsa=\"http://www.w3.org/2005/08/addressing\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n" +
            "<soapenv:Header>\r\n" +  //"<wsa:MessageID>urn:uuid:816f058a-62ca-4b27-9123-c654d35d7fab</wsa:MessageID>\r\n" +
            "<wsa:Action>http://docs.oasis-open.org/ws-sx/ws-trust/200512/RSTRC/IssueFinal</wsa:Action>\r\n" +
            "<wsa:To>http://www.w3.org/2005/08/addressing/anonymous</wsa:To>\r\n" +
            "</soapenv:Header>\r\n" +
            "<soapenv:Body>\r\n" +
            "<wst:RequestSecurityTokenResponseCollection xmlns:wst=\"http://docs.oasis-open.org/ws-sx/ws-trust/200512\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\r\n" +
            "<wst:RequestSecurityTokenResponse Context=\"supportLater\">\r\n" +
            "<wst:TokenType>http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0</wst:TokenType>\r\n" +
            "<wst:RequestedSecurityToken>" + samlToken +
            "</wst:RequestedSecurityToken>\r\n" +
            "<wst:Lifetime>\r\n" +
            "<wsu:Created>" + issueInstant + "</wsu:Created>\r\n" +
            "<wsu:Expires>" + notOnOrAfter + "</wsu:Expires>\r\n" +
            "</wst:Lifetime>\r\n" +
            "</wst:RequestSecurityTokenResponse>\r\n" +
            "</wst:RequestSecurityTokenResponseCollection>\r\n" +
            "</soapenv:Body>\r\n" +
            "</soapenv:Envelope>")

    private fun findChild(pNode: Node?, childName: String, acceptNull: Boolean): Node? {
        if (pNode != null) {
            val nList: NodeList? = pNode.childNodes
            if (nList != null) {
                for (i in 0 until nList.length) {
                    if (nList.item(i).nodeName.endsWith(childName)) {
                        return nList.item(i)
                    }
                }
            }
        }
        if (!acceptNull) {
            throw IllegalArgumentException("Failed to find $childName")
        }
        return null
    }

    @Throws(TransformerFactoryConfigurationError::class, TransformerException::class)
    private fun nodeToString(n: Node?): String? {
        val writer = StringWriter()
        val transformer: Transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty("omit-xml-declaration", "yes")
        transformer.transform(DOMSource(n), StreamResult(writer))
        return writer.toString()
    }
}
