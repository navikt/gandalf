package no.nav.gandalf.accesstoken

import java.io.IOException
import java.time.ZonedDateTime
import javax.xml.crypto.KeySelector
import javax.xml.crypto.MarshalException
import javax.xml.crypto.dsig.XMLSignatureException
import javax.xml.parsers.ParserConfigurationException
import no.nav.gandalf.TestKeySelector
import no.nav.gandalf.keystore.KeyStoreReader
import no.nav.gandalf.utils.getAlteredSamlToken
import no.nav.gandalf.utils.getSamlToken
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.xml.sax.SAXException

@RunWith(SpringRunner::class)
@SpringBootTest
@TestPropertySource(locations=["classpath:application-test.properties"])
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
        assertTrue(samlObj.issuer != null && samlObj.issuer.equals("IS02"))
        assertTrue(samlObj.nameID != null && samlObj.nameID.equals("srvsecurity-token-"))
        assertTrue(samlObj.dateNotBefore != null && samlObj.dateNotBefore!!.compareTo(ZonedDateTime.parse("2019-05-14T07:47:06.255Z")) == 0)
        assertTrue(samlObj.notOnOrAfter != null && samlObj.notOnOrAfter!!.compareTo(ZonedDateTime.parse("2019-05-14T08:47:06.255Z")) == 0)
        assertTrue(samlObj.consumerId != null && samlObj.consumerId.equals("srvsecurity-token-"))
        assertTrue(samlObj.identType != null && samlObj.identType.equals("Systemressurs"))
        assertTrue(samlObj.authenticationLevel != null && samlObj.authenticationLevel.equals("0"))
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
            fail("Error: " + e.message)
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
            assertTrue(samlObj.nameID != null && samlObj.nameID.equals("srvsecurity-token-tull"))
            samlObj.validate(keySelector)
            fail()
        } catch (e: Exception) {
            // signature validation har feilet som den skulle
            assertTrue(true)
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
            // denne skal ikke validere uten feil
            fail()
        } catch (e: Exception) {
            // validation har feilet slik den skulle
            assertTrue(true)
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
        assertTrue(samlIssued.id.equals(samlRead.id))
        assertTrue(samlIssued.issuer.equals(samlRead.issuer))
        assertTrue(samlIssued.nameID.equals(samlRead.nameID))
        assertTrue(samlIssued.dateNotBefore!!.compareTo(samlRead.dateNotBefore) == 0)
        assertTrue(samlIssued.notOnOrAfter!!.compareTo(samlRead.notOnOrAfter) == 0)
        assertTrue(samlIssued.issueInstant!!.compareTo(samlRead.issueInstant) == 0)
        assertTrue(samlIssued.authenticationLevel.equals(samlRead.authenticationLevel))
        assertTrue(samlIssued.consumerId.equals(samlRead.consumerId))
        assertTrue(samlIssued.identType.equals(samlRead.identType))
        assertTrue(samlIssued.id.equals(samlRead.id))
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
            assertTrue(true)
        } catch (e: Exception) {
            fail("Error: " + e.message)
        }
    }
}
