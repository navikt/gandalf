package no.nav.gandalf.accesstoken

import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import com.nimbusds.jwt.SignedJWT
import java.time.ZonedDateTime
import javax.annotation.PostConstruct
import junit.framework.TestCase
import no.nav.gandalf.TestKeySelector
import no.nav.gandalf.accesstoken.AccessTokenIssuer.Companion.OIDC_DURATION_TIME
import no.nav.gandalf.accesstoken.AccessTokenIssuer.Companion.OIDC_VERSION
import no.nav.gandalf.accesstoken.AccessTokenIssuer.Companion.getIdentType
import no.nav.gandalf.accesstoken.oauth.OidcObject
import no.nav.gandalf.accesstoken.oauth.OidcObject.Companion.AUTHTIME_CLAIM
import no.nav.gandalf.accesstoken.oauth.OidcObject.Companion.AZP_CLAIM
import no.nav.gandalf.accesstoken.oauth.OidcObject.Companion.CLIENT_ORGNO_CLAIM
import no.nav.gandalf.accesstoken.oauth.OidcObject.Companion.RESOURCETYPE_CLAIM
import no.nav.gandalf.accesstoken.oauth.OidcObject.Companion.VERSION_CLAIM
import no.nav.gandalf.accesstoken.saml.SamlObject
import no.nav.gandalf.config.LocalIssuer
import no.nav.gandalf.model.AccessTokenResponse
import no.nav.gandalf.model.IdentType
import no.nav.gandalf.service.ExchangeTokenService
import no.nav.gandalf.utils.ControllerUtil
import no.nav.gandalf.utils.azureADJwksUrl
import no.nav.gandalf.utils.diffTokens
import no.nav.gandalf.utils.difiOIDCJwksUrl
import no.nav.gandalf.utils.getAlteredSamlToken
import no.nav.gandalf.utils.getAlteredSamlTokenWithEksternBrukerOgAuthLevel
import no.nav.gandalf.utils.getAlteredSamlTokenWithInternBrukerOgAuthLevel
import no.nav.gandalf.utils.getAzureAdOIDC
import no.nav.gandalf.utils.getDifiOidcToken
import no.nav.gandalf.utils.getDpSamlToken
import no.nav.gandalf.utils.getIDASelvutstedtSaml
import no.nav.gandalf.utils.getMaskinportenToken
import no.nav.gandalf.utils.getOpenAmAndDPSamlExchangePair
import no.nav.gandalf.utils.getOpenAmOIDC
import no.nav.gandalf.utils.getSamlToken
import no.nav.gandalf.utils.openAMJwksUrl
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

private const val ACCESS_TOKEN_TYPE = "bearer"

@RunWith(SpringRunner::class)
@SpringBootTest(
    properties = [
        "application.external.issuer.difi.oidc=http://localhost:\${wiremock.server.port}$difiOIDCJwksUrl",
        "application.jwks.endpoint.azuread=http://localhost:\${wiremock.server.port}$azureADJwksUrl",
        "application.jwks.endpoint.openam=http://localhost:\${wiremock.server.port}$openAMJwksUrl"
    ],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@DirtiesContext
class AccessTokenIssuerTest {

    @Autowired
    private lateinit var env: LocalIssuer

    @Autowired
    private lateinit var issuer: AccessTokenIssuer

    @PostConstruct
    fun setupKnownIssuers() {
        ControllerUtil().setupKnownIssuers()
    }

    @Test
    @Throws(Exception::class)
    fun `'Token validation' issue and validate OIDC token`() {
        val username = "n123456"
        val token = issuer.issueToken(username)
        TestCase.assertNotNull(token)
        val jwt = token.jwtClaimsSet
        TestCase.assertEquals(jwt.subject, username)
        TestCase.assertEquals(jwt.getClaim(AZP_CLAIM), username)
        TestCase.assertEquals(jwt.issuer, issuer.issuer)
        TestCase.assertEquals(jwt.getStringClaim(VERSION_CLAIM), OIDC_VERSION)
        TestCase.assertTrue(jwt.jwtid != null)
        TestCase.assertTrue(jwt.getClaim(RESOURCETYPE_CLAIM) == IdentType.INTERNBRUKER.value)
        // sjekk audience
        val audience = token.jwtClaimsSet.audience
        TestCase.assertEquals(2, audience.size)
        TestCase.assertEquals(audience[0], username)
        TestCase.assertEquals("preprod.local", audience[1])
        // sjekk time settings
        TestCase.assertEquals(0, jwt.notBeforeTime.compareTo(jwt.issueTime))
        TestCase.assertEquals(jwt.getLongClaim(AUTHTIME_CLAIM) as Long, jwt.issueTime.time / 1000)
        TestCase.assertEquals((jwt.expirationTime.time - jwt.issueTime.time) / 1000, OIDC_DURATION_TIME)
        // Test response
        val (_, token_type, expires_in) = AccessTokenResponse(oidcToken = token)
        TestCase.assertTrue(expires_in == OIDC_DURATION_TIME)
        TestCase.assertTrue(token_type.equals(ACCESS_TOKEN_TYPE, ignoreCase = true))
        // test validation
        assertDoesNotThrow {
            issuer.validateOidcToken(token.serialize())
            TestCase.assertTrue(true)
        }
    }

    @Test
    fun `Validate OpenAm OIDC`() {
        val oidcToken: String = getOpenAmOIDC()
        assertDoesNotThrow {
            val signedJWT = SignedJWT.parse(oidcToken)
            val claimSet = signedJWT.jwtClaimsSet
            issuer.validateOidcToken(oidcToken, claimSet.issueTime)
            assertTrue(true)
        }
        verify(getRequestedFor(urlEqualTo(openAMJwksUrl)))
    }

    @Test
    fun `Validate AzureAd OIDC`() {
        val oidcToken: String = getAzureAdOIDC()
        val signedJWT: SignedJWT
        try {
            signedJWT = SignedJWT.parse(oidcToken)
            val claimSet = signedJWT.jwtClaimsSet
            issuer.validateOidcToken(oidcToken, claimSet.issueTime)
            assertTrue(true)
        } catch (e: java.lang.Exception) {
            fail { e.printStackTrace().toString() }
        }
        verify(getRequestedFor(urlEqualTo(azureADJwksUrl)))
    }

    @Test
    fun `Exchange Valid SAML To OIDC Token`() {
        val samlToken: String = getSamlToken()
        // get notOnOrAfter Date
        val samlObj = SamlObject()
        samlObj.read(samlToken)
        val beforeAfter: Long = 2
        val now: ZonedDateTime = samlObj.notOnOrAfter!!.minusSeconds(beforeAfter)
        val subject: String? = samlObj.nameID
        // exchangeToken med valid date
        val token = issuer.exchangeSamlToOidcToken(samlToken, now)
        assertTrue(token.jwtClaimsSet.expirationTime != null)
        assertTrue(token.jwtClaimsSet.subject == subject)
        assertTrue(token.jwtClaimsSet.audience.size == 2)
        assertTrue(token.jwtClaimsSet.audience.contains(samlObj.nameID))
        assertEquals(token.jwtClaimsSet.getClaim("azp"), samlObj.consumerId)
        // Test response
        val response = ExchangeTokenService().getResponseFrom(token)
        assertTrue(response.expires_in == beforeAfter + AccessTokenIssuer.EXCHANGE_TOKEN_EXTENDED_TIME)
        assertTrue(response.token_type.equals(ACCESS_TOKEN_TYPE, ignoreCase = true))
    }

    @Test
    @Throws(Exception::class)
    fun `Exchange Valid IDA Selvutstedt Token`() {
        val notOnOrAfter = ZonedDateTime.parse("2018-06-06T09:58:18.472Z")
        val beforeAfter: Long = 2
        val now = notOnOrAfter.minusSeconds(beforeAfter)
        // exchangeToken med valid date
        val token = issuer.exchangeSamlToOidcToken(getIDASelvutstedtSaml(), now)
        assertTrue(token.jwtClaimsSet.expirationTime != null)
        assertTrue(token.jwtClaimsSet.subject == "Z991643")
        // Test response
        val response = ExchangeTokenService().getResponseFrom(token)
        assertTrue(response.expires_in == beforeAfter + AccessTokenIssuer.EXCHANGE_TOKEN_EXTENDED_TIME)
        assertTrue(response.token_type.equals(ACCESS_TOKEN_TYPE, ignoreCase = true))
    }

    @Test
    fun `Get SAML Authentication Level Test`() {
        val now = ZonedDateTime.parse("2018-05-07T10:21:59Z").minusSeconds(5)
        val samlObj = SamlObject(now)
        // exchangeToken med valid date
        Assert.assertThrows(RuntimeException::class.java) {
            samlObj.read(getAlteredSamlTokenWithEksternBrukerOgAuthLevel())
            samlObj.validate(issuer.getKeySelector())
            // feiler i valideringen, men gjøre ikke noe for det som skal testes her
            assertTrue(issuer.getAuthenticationLevel(samlObj).equals("Level3", ignoreCase = true))
        }
        Assert.assertThrows(RuntimeException::class.java) {
            samlObj.read(getAlteredSamlTokenWithInternBrukerOgAuthLevel())
            samlObj.validate(issuer.getKeySelector())
            // feiler i valideringen som jo er riktig, men det gjør ikke noe for det som skal testes her
            assertTrue(issuer.getAuthenticationLevel(samlObj) == null)
        }
    }

    @Test
    fun `Exchange SAML to Oidc - condition NotOnOrAfter should fail`() {
        val samlToken = getSamlToken()
        val samlObj = SamlObject()
        samlObj.read(samlToken)
        val now: ZonedDateTime = samlObj.notOnOrAfter!!.plusSeconds(65)
        // exchangeToken med invalid date
        val nowWithSkew = now.toInstant().minusSeconds(samlObj.getMaxClockSkew())
        val exception: RuntimeException = Assert.assertThrows(RuntimeException::class.java) {
            issuer.exchangeSamlToOidcToken(samlToken, now)
        }
        val expectedMessage =
            "{\"error_description\":\"Invalid SAML token: condition notOnOrAfter: ${samlObj.notOnOrAfter}, " +
                "is not on or after: $nowWithSkew\",\"error\":\"invalid_request\"}"
        val actualMessage = exception.message
        assertEquals(expectedMessage, actualMessage!!)
    }

    @Test
    fun `Exchange SAML Token Condition NotAfter`() {
        val samlToken = getSamlToken()
        val samlObj = SamlObject()
        samlObj.read(samlToken)
        val notOnOrAfter: ZonedDateTime? = samlObj.notOnOrAfter
        val now = notOnOrAfter!!.plusSeconds(62)
        // exchangeToken med invalid date
        val nowWithSkew = now.toInstant().minusSeconds(samlObj.getMaxClockSkew())
        val exception: RuntimeException = Assert.assertThrows(RuntimeException::class.java) {
            issuer.exchangeSamlToOidcToken(samlToken, now)
        }
        val expectedMessage =
            "{\"error_description\":\"Invalid SAML token: condition notOnOrAfter: $notOnOrAfter, " +
                "is not on or after: $nowWithSkew\",\"error\":\"invalid_request\"}"
        val actualMessage = exception.message
        assertEquals(expectedMessage, actualMessage!!)
    }

    @Test
    fun `Exchange SAML Token Condition NotBefore`() {
        val samlToken = getSamlToken()
        val samlObj = SamlObject()
        samlObj.read(samlToken)
        val notBefore: ZonedDateTime? = samlObj.dateNotBefore
        val now = notBefore!!.minusSeconds(65)
        // exchangeToken med invalid date
        val nowWithSkew = now.toInstant().plusSeconds(samlObj.getMaxClockSkew())
        val exception: RuntimeException = Assert.assertThrows(RuntimeException::class.java) {
            issuer.exchangeSamlToOidcToken(samlToken, now)
        }
        val expectedMessage =
            "{\"error_description\":\"Invalid SAML token: condition " +
                "nbf: $notBefore, is before: $nowWithSkew\",\"error\":\"invalid_request\"}"
        val actualMessage = exception.message
        assertEquals(expectedMessage, actualMessage!!)
    }

    @Test
    fun `Exchange SAML Token Altered`() {
        val samlToken: String = getAlteredSamlToken()
        val samlObj = SamlObject()
        samlObj.read(samlToken)
        val notOnOrAfter: ZonedDateTime? = samlObj.notOnOrAfter
        val now = notOnOrAfter!!.minusSeconds(2)
        // exchangeToken med altered samlToken
        val exception: RuntimeException = Assert.assertThrows(RuntimeException::class.java) {
            issuer.exchangeSamlToOidcToken(samlToken, now)
        }
        val expectedMessage =
            "{\"error_description\":\"Invalid SAML token: Signature validation failed on reference #SAML-4161a46a-ebc3-403f-9d3d-4eff65a070ae\",\"error\":\"invalid_request\"}"
        val actualMessage = exception.message
        assertEquals(expectedMessage, actualMessage!!)
    }

    @Test
    fun `Issue And Validate SAML Token`() {
        val samlToken = issuer.issueSamlToken("srvPDP", "srvPDP", AccessTokenIssuer.DEFAULT_SAML_AUTHLEVEL)
        val samlObj = SamlObject()
        samlObj.read(samlToken)
        val keySelector = TestKeySelector()
        assertDoesNotThrow {
            samlObj.validate(keySelector)
        }
    }

    @Test
    fun `Issue SAML And Compare To Datapower SAML`() {
        assertDoesNotThrow {
            val dpSamlToken: String = getDpSamlToken()
            val samlObj = SamlObject()
            samlObj.read(dpSamlToken)
            val samlToken = issuer.issueSamlToken(
                env.issuerUsername,
                env.issuerUsername,
                AccessTokenIssuer.DEFAULT_SAML_AUTHLEVEL,
                samlObj.issueInstant!!
            )
            val diff: List<String>? = diffTokens(dpSamlToken, samlToken)
            val realDiff: MutableList<String> = ArrayList()
            diff!!.forEach { line ->
                if (hasDifferences(line)) {
                    realDiff.add(line)
                    println("#Diff: $line")
                }
            }
            assertTrue(realDiff.size == 0)
            println("NO diff")
        }
    }

    @Test
    fun `Exchange OIDC To SAML Token`() {
        assertDoesNotThrow {
            val signedJWT = issuer.issueToken(env.issuerUsername)
            val oidcToken = signedJWT.serialize()
            val samlToken = issuer.exchangeOidcToSamlToken(oidcToken, env.issuerUsername)
            val samlObj = SamlObject()
            samlObj.read(samlToken)
            samlObj.validate(issuer.getKeySelector())
            assertTrue(samlObj.nameID.equals(env.issuerUsername, ignoreCase = true))
            assertTrue(samlObj.issuer.equals(AccessTokenIssuer.SAML_ISSUER, ignoreCase = true))
            val oidcObj = OidcObject(oidcToken)
            assertTrue(samlObj.nameID.equals(oidcObj.subject, ignoreCase = true))
            assertTrue(samlObj.identType.equals(oidcObj.resourceType, ignoreCase = true))
            assertTrue(samlObj.authenticationLevel.equals("0"))
            // sjekk auth level (denne blir satt til 0 -> default) og tidspunkt
        }
    }

    @Test
    fun `Filter Isso Issuer from others to make compare`() {
        val isso = issuer.filterIssoInternIssuer()
        assert(isso != null)
        assert(isso!!.issuer.contains(AccessTokenIssuer.ISSO_OIDC_ISSUER))
    }

    @Test
    fun `Exchange OpenAm OIDC To SAML Token`() {
        val l: List<String> = getOpenAmAndDPSamlExchangePair()
        val oidcToken = l[0]
        val dpSamlToken = l[1]
        try {
            val oidcObj = OidcObject(oidcToken)
            val samlObj = SamlObject()
            samlObj.read(dpSamlToken)
            println("Oidc token issued at: " + AccessTokenIssuer.toZonedDateTime(oidcObj.issueTime))
            println("Saml token issued at: " + samlObj.issueInstant)
            val samlToken =
                issuer.exchangeOidcToSamlToken(oidcToken, samlObj.consumerId, OidcObject.toDate(samlObj.issueInstant))
            val diff: List<String>? = diffTokens(dpSamlToken, samlToken)
            val realDiff: MutableList<String> = ArrayList()
            diff!!.forEach { line ->
                // filter known differences (Signature node er filtrert ut allerede)
                if (line.contains("Assertion Attribute ID has different") && line.contains("token2 has SAML-") ||
                    line.contains("saml2:Assertion has child saml2:AuthnStatement") ||
                    line.contains("Attribute NameFormat has different") ||
                    line.contains("Node saml2:Attribute:authenticationLevel token1 has textcontent 4 token2 has 0") ||
                    line.contains("Node saml2:SubjectConfirmationData Attribute NotBefore has different content: token1 has 2018-10-18T07:27:29Z token2 has 2018-10-18T07:27:32Z") ||
                    line.contains("Node saml2:SubjectConfirmationData Attribute NotOnOrAfter has different content: token1 has 2018-10-18T08:22:38Z token2 has 2018-10-18T08:23:08Z") ||
                    line.contains("Node saml2:Conditions Attribute NotBefore has different content: token1 has 2018-10-18T07:27:29Z token2 has 2018-10-18T07:27:32Z") ||
                    line.contains("Node saml2:Conditions Attribute NotOnOrAfter has different content: token1 has 2018-10-18T08:22:38Z token2 has 2018-10-18T08:23:08Z")
                ) {
                    println("NO diff")
                } else {
                    realDiff.add(line)
                    println("#Diff: $line")
                }
            }
            assertTrue(realDiff.size == 0)
        } catch (e: Exception) {
            fail(e.message)
        }
    }

    @Test
    fun `Exchange DIFI Token To OIDC - Utdated Token Not In Use by DIFI`() {
        val difiToken: String = getDifiOidcToken()
        val difiJwt = SignedJWT.parse(difiToken).jwtClaimsSet
        assertThrows<Exception> { issuer.getSubjectFromDifiToken(difiJwt.getClaim("consumer")) }
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun `Exchange Maskinporten Token To Oidc`() {
        val difiToken: String = getMaskinportenToken()
        val difiJwt = SignedJWT.parse(difiToken).jwtClaimsSet
        val subject = issuer.getSubjectFromDifiToken(difiJwt.getClaim("consumer"))
        val issueAt = difiJwt.issueTime
        // exchange token
        val oidcToken = issuer.exchangeDifiTokenToOidc(difiToken, issueAt)
        // check issued token
        val jwt = oidcToken.jwtClaimsSet
        assertEquals(jwt.subject, subject)
        assertEquals(jwt.getClaim(AZP_CLAIM), subject)
        assertEquals(jwt.issuer, issuer.issuer)
        assertEquals(jwt.getStringClaim(VERSION_CLAIM), OIDC_VERSION)
        assertEquals(jwt.getClaim(CLIENT_ORGNO_CLAIM), subject)
        assertNotNull(jwt.jwtid)
        assertEquals(jwt.getClaim(RESOURCETYPE_CLAIM), IdentType.SAMHANDLER.value)
        assertEquals(jwt.getStringClaim(OidcObject.TRACKING_CLAIM), difiJwt.jwtid)
        assertEquals(jwt.audience.size.toLong(), difiJwt.audience.size.toLong())
        for (i in jwt.audience.indices) {
            assertEquals(jwt.audience[i], difiJwt.audience[i])
        }
        assertEquals(0, jwt.issueTime.compareTo(issueAt).toLong())
        assertEquals(0, jwt.notBeforeTime.compareTo(issueAt).toLong())
        assertEquals(jwt.getLongClaim(AUTHTIME_CLAIM) as Long, jwt.issueTime.time / 1000)
        assertEquals((jwt.expirationTime.time - jwt.issueTime.time) / 1000, OIDC_DURATION_TIME)
    }

    @Test
    fun `Validate that the right IdentType get set`() {
        val srvUser = "srvUser"
        val samhandler = listOf("102010292")
        val internalUsers = listOf("justAUser", "10202928", "n10939", "n1093998", "9jhsnog", "n291029", "N291029")
        val externalUsers = listOf("12345678953")

        assertEquals(IdentType.SYSTEMRESSURS.value, getIdentType(srvUser))
        internalUsers.forEach {
            assertEquals(IdentType.INTERNBRUKER.value, getIdentType(it))
        }
        samhandler.forEach {
            assertEquals(IdentType.SAMHANDLER.value, getIdentType(it))
        }
        externalUsers.forEach {
            assertEquals(IdentType.EKSTERNBRUKER.value, getIdentType(it, "4"))
            assertEquals(IdentType.EKSTERNBRUKER.value, getIdentType(it, "3"))
        }
        assertThrows<RuntimeException> {
            externalUsers.forEach {
                assertEquals(IdentType.EKSTERNBRUKER.value, getIdentType(it, "1"))
                assertEquals(IdentType.EKSTERNBRUKER.value, getIdentType(it, "0"))
                assertEquals(IdentType.EKSTERNBRUKER.value, getIdentType(it, "5"))
            }
        }
    }
}

internal fun hasDifferences(line: String) =
    !(
        attributeIdDifferences(line) ||
            notBeforeSubjectConfirmationDataHasDifferences(line) ||
            notOnOrAfterSubjectConfirmationDataHasDifferences(line) ||
            notBeforeConditionDifferences(line) ||
            notOnOrAfterConditionHasDifferences(line)
        )

internal fun attributeIdDifferences(line: String) = line.contains(
    "Assertion Attribute ID has different"
) && line.contains("token2 has SAML-")

internal fun notBeforeSubjectConfirmationDataHasDifferences(line: String) = line.contains(
    "Node saml2:SubjectConfirmationData Attribute NotBefore has different content: token1 has 2018-10-24T08:58:33Z token2 has 2018-10-24T08:58:36Z"
)

internal fun notOnOrAfterSubjectConfirmationDataHasDifferences(line: String) = line.contains(
    "Node saml2:SubjectConfirmationData Attribute NotOnOrAfter has different content: token1 has 2018-10-24T09:58:39Z token2 has 2018-10-24T09:58:36Z"
)

internal fun notBeforeConditionDifferences(line: String) = line.contains(
    "Node saml2:Conditions Attribute NotBefore has different content: token1 has 2018-10-24T08:58:33Z token2 has 2018-10-24T08:58:36Z"
)

internal fun notOnOrAfterConditionHasDifferences(line: String) = line.contains(
    "Node saml2:Conditions Attribute NotOnOrAfter has different content: token1 has 2018-10-24T09:58:39Z token2 has 2018-10-24T09:58:36Z"
)
