package no.nav.gandalf.api

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.gandalf.utils.ControllerUtil
import no.nav.gandalf.utils.GRANT_TYPE
import no.nav.gandalf.utils.OIDC_TOKEN_VALIDATE
import no.nav.gandalf.utils.SAML_TOKEN
import no.nav.gandalf.utils.SAML_TOKEN_VALIDATE
import no.nav.gandalf.utils.SCOPE
import no.nav.gandalf.utils.TOKEN
import no.nav.gandalf.utils.TOKEN_SUBJECT
import no.nav.gandalf.utils.TOKEN_TYPE
import no.nav.gandalf.utils.getDatapowerSAMLBase64Encoded
import no.nav.gandalf.utils.getOpenAmOIDC
import no.nav.security.mock.oauth2.http.objectMapper
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
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
class TokenInfoControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @PostConstruct
    fun setupKnownIssuers() {
        ControllerUtil().setupKnownIssuers()
    }

    @Test
    fun `Validate Valid SAML Token`() {
        val result = mvc.perform(
            MockMvcRequestBuilders.get(SAML_TOKEN)
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.access_token").isString)
            .andExpect(jsonPath("$.token_type").value(TOKEN_TYPE))
            .andExpect(jsonPath("$.issued_token_type").value("urn:ietf:params:oauth:token-type:saml2"))
            .andExpect(jsonPath("$.expires_in").isNumber).andReturn()

        val mockedToken = objectMapper.readValue<MockTokenTest>(result.response.contentAsString)

        mvc.perform(
            MockMvcRequestBuilders.post(SAML_TOKEN_VALIDATE)
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
                .param(TOKEN_SUBJECT, mockedToken.access_token)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(true))
    }

    @Test
    fun `Validate Expired SAML Token`() {
        mvc.perform(
            MockMvcRequestBuilders.post(SAML_TOKEN_VALIDATE)
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
                .param(TOKEN_SUBJECT, getDatapowerSAMLBase64Encoded)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(false))
            .andExpect(
                jsonPath("$.message").isString
            )
    }

    @Test
    fun `Validate a valid OIDC Token`() {
        val result = mvc.perform(
            MockMvcRequestBuilders.get(TOKEN)
                .param(GRANT_TYPE, "client_credentials")
                .param(SCOPE, "openid")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.header().stringValues("Cache-Control", "no-store"))
            .andExpect(MockMvcResultMatchers.header().stringValues("Pragma", "no-cache"))
            .andExpect(jsonPath("$.access_token").isString)
            .andExpect(jsonPath("$.token_type").value(TOKEN_TYPE))
            .andExpect(jsonPath("$.expires_in").value(3600)).andReturn()

        val mockedToken = objectMapper.readValue<MockTokenTest>(result.response.contentAsString)

        mvc.perform(
            MockMvcRequestBuilders.post(OIDC_TOKEN_VALIDATE)
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
                .param(TOKEN_SUBJECT, mockedToken.access_token)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(true))
    }

    @Test
    fun `Validate Expired OIDC Token`() {
        mvc.perform(
            MockMvcRequestBuilders.post(OIDC_TOKEN_VALIDATE)
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
                .param(TOKEN_SUBJECT, getOpenAmOIDC())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(false))
            .andExpect(jsonPath("$.message").value("Validation failed: token has expired"))
    }
}
