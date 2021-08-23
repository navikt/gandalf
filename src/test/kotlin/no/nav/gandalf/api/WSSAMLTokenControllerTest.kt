package no.nav.gandalf.api

import no.nav.gandalf.SpringBootWireMockSetup
import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.utils.WS_SAMLTOKEN
import no.nav.gandalf.utils.getOidcToSamlRequest
import no.nav.gandalf.utils.getSamlRequest
import no.nav.gandalf.utils.getValidateSamlRequest
import org.apache.http.entity.ContentType
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
class WSSAMLTokenControllerTest : SpringBootWireMockSetup() {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var issuer: AccessTokenIssuer

    @Test
    fun `SAML - WS - User Not In Ldap`() {
        val xmlReq = setupValidateRequest("srvPD", "password", issuer)
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
        val xmlReq = setupOIDCtoSAMLRequest("srvPDP", "password", issuer)
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
        val xmlReq = setupValidateRequest("srvPDP", "password", issuer)
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

    @Test
    fun `SAML - WS - exchange oidc from tokendings and idporten to SAML`() {
    }

    @Test
    fun `SAML - WS - exchange oidc from tokendings and Azure AD B2C to SAML`() {
    }
}

internal fun setupValidateRequest(username: String, password: String, issuer: AccessTokenIssuer): String {
    val samlToken: String? = issuer.issueSamlToken(username, username, "0")
    return getValidateSamlRequest(username, password, samlToken!!)
}

internal fun setupOIDCtoSAMLRequest(username: String, password: String, issuer: AccessTokenIssuer): String {
    val oidcToken: String? = issuer.issueToken(username).serialize()
    return getOidcToSamlRequest(username, password, oidcToken!!)
}
