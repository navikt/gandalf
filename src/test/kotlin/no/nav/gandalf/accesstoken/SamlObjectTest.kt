package no.nav.gandalf.accesstoken

import no.nav.gandalf.TestKeySelector
import no.nav.gandalf.accesstoken.saml.SamlObject
import no.nav.gandalf.keystore.KeyStoreReader
import no.nav.gandalf.utils.getAlteredSamlToken
import no.nav.gandalf.utils.getSamlToken
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.time.ZonedDateTime
import javax.xml.crypto.KeySelector

@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
@DirtiesContext
class SamlObjectTest {
    @Autowired
    private lateinit var keySelector: KeySelector

    @Autowired
    private lateinit var keyStoreReader: KeyStoreReader

    @Test
    fun `Read SAML Token`() {
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
    fun `Read And Validate SAML Token`() {
        val notOnOrAfter = ZonedDateTime.parse("2019-05-14T08:47:06.255Z")
        val now = notOnOrAfter.minusSeconds(2)
        assertDoesNotThrow {
            // read and validate saml token with now = notOnOrAfter - 2 seconds
            val samlObj = SamlObject(now)
            samlObj.read(getSamlToken())
            samlObj.validate(keySelector)
        }
    }

    @Test
    fun `Read And Validate Altered SAML Token`() {
        val notOnOrAfter = ZonedDateTime.parse("2019-05-14T08:47:06.255Z")
        val now = notOnOrAfter.minusSeconds(2)
        Assert.assertThrows(OAuthException::class.java) {
            // read and validate saml token with now = notOnOrAfter - 2 seconds
            val samlObj = SamlObject(now)
            samlObj.read(getAlteredSamlToken())
            assertTrue(samlObj.nameID != null && samlObj.nameID.equals("srvsecurity-token-tull"))
            samlObj.validate(keySelector)
        }
    }

    @Test
    fun `Read And Validate Invalid SAML Token`() {
        Assert.assertThrows(OAuthException::class.java) {
            // saml token has notOnOrAfter = "2018-05-07T10:21:59Z"
            val samlObj = SamlObject()
            samlObj.read(getSamlToken())
            samlObj.validate(keySelector)
        }
    }

    @Test
    @Throws(Exception::class)
    fun `Issue Signed SAML Token`() {
        val samlIssued = SamlObject()
        samlIssued.issuer = "ISO2"
        samlIssued.setDuration(3600)
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
    fun `Issue And Validate SAML Token`() {
        var samlObj = SamlObject()
        samlObj.issuer = "ISO2"
        samlObj.setDuration(3600)
        samlObj.nameID = "srvPDP"
        samlObj.authenticationLevel = "0"
        samlObj.consumerId = "srvPDP"
        samlObj.identType = "Systemressurs"
        val samlToken = samlObj.getSignedSaml(keyStoreReader)
        samlObj = SamlObject()
        samlObj.read(samlToken)
        val keySelector = TestKeySelector()
        assertDoesNotThrow {
            samlObj.validate(keySelector)
        }
    }
}
