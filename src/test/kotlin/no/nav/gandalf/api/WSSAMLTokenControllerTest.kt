package no.nav.gandalf.api

import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.ldap.InMemoryLdap
import no.nav.gandalf.utils.ControllerUtil
import no.nav.gandalf.utils.WS_SAMLTOKEN
import no.nav.gandalf.utils.getOidcToSamlRequest
import no.nav.gandalf.utils.getSamlRequest
import no.nav.gandalf.utils.getValidateSamlRequest
import org.apache.http.entity.ContentType
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import javax.annotation.PostConstruct

@RunWith(SpringRunner::class)
@SpringBootTest(
    properties = [
        "application.external.issuer.difi.maskinporten=http://localhost:\${wiremock.server.port}/",
        "application.external.issuer.difi.oidc=http://localhost:\${wiremock.server.port}/idporten-oidc-provider",
        "application.jwks.endpoint.azuread=http://localhost:\${wiremock.server.port}/jwk",
        "application.jwks.endpoint.openam=http://localhost:\${wiremock.server.port}/isso/oauth2/connect/jwk_uri"
    ],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
@DirtiesContext
class WSSAMLTokenControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var issuer: AccessTokenIssuer

    private val controllerUtil = ControllerUtil()

    private val inMemoryLdap = InMemoryLdap()

    @PostConstruct
    fun setupKnownIssuers() {
        controllerUtil.setupKnownIssuers()
        controllerUtil.runLdap(inMemoryLdap)
    }

    @After
    fun clear() {
        controllerUtil.stopLdap(inMemoryLdap)
    }

    @Test
    fun `SAML - WS - User Not In Ldap`() {
        val xmlReq = setupValidateRequest("srvPD", "password")
        mvc.perform(
            MockMvcRequestBuilders.post(WS_SAMLTOKEN)
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
                .contentType(ContentType.TEXT_XML.mimeType)
                .content(xmlReq)
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(MockMvcResultMatchers.content().contentType("text/xml;charset=UTF-8"))
    }

    @Test
    fun `SAML - WS - isIssueSamlFromUNT`() {
        val xmlReq: String = getSamlRequest("srvPDP", "password")
        mvc.perform(
            MockMvcRequestBuilders.post(WS_SAMLTOKEN)
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
                .contentType(ContentType.TEXT_XML.mimeType)
                .content(xmlReq)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType("text/xml;charset=UTF-8"))
        // TODO Validate xpath response
        // .andExpect(MockMvcResultMatchers.xpath("/*/soapenv:Body/").exists())
    }

    @Test
    fun `SAML - WS - isExchangeOidcToSaml`() {
        val xmlReq = setupOIDCtoSAMLRequest("srvPDP", "password")
        mvc.perform(
            MockMvcRequestBuilders.post(WS_SAMLTOKEN)
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
                .contentType(ContentType.TEXT_XML.mimeType)
                .content(xmlReq)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType("text/xml;charset=UTF-8"))
        // TODO Validate xpath response
        // .andExpect(MockMvcResultMatchers.xpath("/*/soapenv:Body/").exists())
    }

    @Test
    fun `SAML - WS - isValidateSaml`() {
        val xmlReq = setupValidateRequest("srvPDP", "password")
        mvc.perform(
            MockMvcRequestBuilders.post(WS_SAMLTOKEN)
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
                .contentType(ContentType.TEXT_XML.mimeType)
                .content(xmlReq)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType("text/xml;charset=UTF-8"))
        // TODO Validate response
        // .andExpect(MockMvcResultMatchers.xpath("/*/soapenv:Body/").exists())
    }

    private fun setupValidateRequest(username: String, password: String): String {
        val samlToken: String? = issuer.issueSamlToken(username, username, "0")
        return getValidateSamlRequest(username, password, samlToken!!)
    }

    private fun setupOIDCtoSAMLRequest(username: String, password: String): String {
        val oidcToken: String? = issuer.issueToken(username)!!.serialize()
        return getOidcToSamlRequest(username, password, oidcToken!!)
    }
}
