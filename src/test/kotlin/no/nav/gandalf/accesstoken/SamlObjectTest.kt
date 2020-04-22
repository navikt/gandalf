package no.nav.gandalf.accesstoken

import no.nav.gandalf.TestKeySelector
import no.nav.gandalf.keystore.KeyStoreReader
import no.nav.gandalf.utils.getAlteredSamlToken
import no.nav.gandalf.utils.getSamlToken
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.xml.sax.SAXException
import java.io.IOException
import java.time.ZonedDateTime
import javax.xml.crypto.KeySelector
import javax.xml.crypto.MarshalException
import javax.xml.crypto.dsig.XMLSignatureException
import javax.xml.parsers.ParserConfigurationException

@RunWith(SpringRunner::class)
@SpringBootTest
class SamlObjectTest {

    @Autowired
    private lateinit var keySelector: KeySelector

    @Autowired
    private lateinit var keyStoreReader: KeyStoreReader


    @Test
    @Throws(ParserConfigurationException::class, SAXException::class, IOException::class)
    fun `Read Saml Token`() {
        // read saml token
        val samlObj = SamlObject()
        samlObj.read(getSamlToken())
        Assert.assertTrue(samlObj.issuer != null && samlObj.issuer.equals("IS02"))
        Assert.assertTrue(samlObj.nameID != null && samlObj.nameID.equals("srvsecurity-token-"))
        Assert.assertTrue(samlObj.dateNotBefore != null && samlObj.dateNotBefore!!.compareTo(ZonedDateTime.parse("2019-05-14T07:47:06.255Z")) == 0)
        Assert.assertTrue(samlObj.notOnOrAfter != null && samlObj.notOnOrAfter!!.compareTo(ZonedDateTime.parse("2019-05-14T08:47:06.255Z")) == 0)
        Assert.assertTrue(samlObj.consumerId != null && samlObj.consumerId.equals("srvsecurity-token-"))
        Assert.assertTrue(samlObj.identType != null && samlObj.identType.equals("Systemressurs"))
        Assert.assertTrue(samlObj.authenticationLevel != null && samlObj.authenticationLevel.equals("0"))
    }

    @Test
    @Throws(ParserConfigurationException::class, SAXException::class, IOException::class, MarshalException::class, XMLSignatureException::class)
    fun `Read And Validate Saml Token`() {
        val notOnOrAfter = ZonedDateTime.parse("2019-05-14T08:47:06.255Z")
        val now = notOnOrAfter.minusSeconds(2)
        try {
            // read and validate saml token with with now = notOnOrAfter - 2 seconds
            val samlObj = SamlObject(now)
            samlObj.read(getSamlToken())
            samlObj.validate(keySelector)
        } catch (e: Exception) {
            Assert.assertTrue(false)
        }
    }

    @Test
    @Throws(ParserConfigurationException::class, SAXException::class, IOException::class, MarshalException::class, XMLSignatureException::class)
    fun `Read And Validate Altered Saml Token`() {
        val notOnOrAfter = ZonedDateTime.parse("2019-05-14T08:47:06.255Z")
        val now = notOnOrAfter.minusSeconds(2)
        try {
            // read and validate saml token with with now = notOnOrAfter - 2 seconds
            val samlObj = SamlObject(now)
            samlObj.read(getAlteredSamlToken())
            Assert.assertTrue(samlObj.nameID != null && samlObj.nameID.equals("srvsecurity-token-tull"))
            samlObj.validate(keySelector)
            Assert.assertTrue(false)
        } catch (e: Exception) {
            Assert.assertTrue(true) // signature validation har feilet som den skulle
        }
    }

    @Test
    @Throws(ParserConfigurationException::class, SAXException::class, IOException::class, MarshalException::class, XMLSignatureException::class)
    fun `Read And Validate Invalid Saml Token`() {
        try {
            // saml token has notOnOrAfter = "2018-05-07T10:21:59Z"
            val samlObj = SamlObject()
            samlObj.read(getSamlToken())
            samlObj.validate(keySelector)
            Assert.assertTrue(false) // denne skal ikke validere uten feil
        } catch (e: Exception) {
            Assert.assertTrue(true) // validation har feilet slik den skulle
        }
    }

    @Test
    @Throws(Exception::class)
    fun issueSignedSamlToken() {
        val samlIssued = SamlObject()
        samlIssued.issuer = "ISO2"
        samlIssued.setDuration(60 * 60)
        samlIssued.nameID = "srvPDP"
        samlIssued.authenticationLevel = "0"
        samlIssued.consumerId = "srvPDP"
        samlIssued.identType = "Systemressurs"
        val samlToken = samlIssued.getSignedSaml(keyStoreReader)
        val samlRead = SamlObject()
        samlRead.read(samlToken)
        Assert.assertTrue(samlIssued.id.equals(samlRead.id))
        Assert.assertTrue(samlIssued.issuer.equals(samlRead.issuer))
        Assert.assertTrue(samlIssued.nameID.equals(samlRead.nameID))
        Assert.assertTrue(samlIssued.dateNotBefore!!.compareTo(samlRead.dateNotBefore) == 0)
        Assert.assertTrue(samlIssued.notOnOrAfter!!.compareTo(samlRead.notOnOrAfter) == 0)
        Assert.assertTrue(samlIssued.issueInstant!!.compareTo(samlRead.issueInstant) == 0)
        Assert.assertTrue(samlIssued.authenticationLevel.equals(samlRead.authenticationLevel))
        Assert.assertTrue(samlIssued.consumerId.equals(samlRead.consumerId))
        Assert.assertTrue(samlIssued.identType.equals(samlRead.identType))
        Assert.assertTrue(samlIssued.id.equals(samlRead.id))
    }


    @Test
    @Throws(Exception::class)
    fun issueAndValidateSamlToken() {
        var samlObj = SamlObject()
        samlObj.issuer = "ISO2"
        samlObj.setDuration(60 * 60)
        samlObj.nameID = "srvPDP"
        samlObj.authenticationLevel = "0"
        samlObj.consumerId = "srvPDP"
        samlObj.identType = "Systemressurs"
        val samlToken = samlObj.getSignedSaml(keyStoreReader)
        samlObj = SamlObject()
        samlObj.read(samlToken)
        val keySelector = TestKeySelector()
        try {
            samlObj.validate(keySelector)
            Assert.assertTrue(true)
        } catch (e: Exception) {
            Assert.assertTrue(false)
        }
    }
}