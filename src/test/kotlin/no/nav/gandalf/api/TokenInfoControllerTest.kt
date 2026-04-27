package no.nav.gandalf.api

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.gandalf.SpringBootWireMockSetup
import no.nav.gandalf.utils.DATAPOWER_SAML_BASE64_ENCODED
import no.nav.gandalf.utils.GRANT_TYPE
import no.nav.gandalf.utils.OIDC_TOKEN_VALIDATE
import no.nav.gandalf.utils.SAML_TOKEN
import no.nav.gandalf.utils.SAML_TOKEN_VALIDATE
import no.nav.gandalf.utils.SCOPE
import no.nav.gandalf.utils.TOKEN
import no.nav.gandalf.utils.TOKEN_SUBJECT
import no.nav.gandalf.utils.TOKEN_TYPE
import no.nav.gandalf.utils.getOpenAmOIDC
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

private val objectMapper = ObjectMapper()

@ActiveProfiles("test")
@DirtiesContext
class TokenInfoControllerTest : SpringBootWireMockSetup() {
    @Test
    fun `Validate Valid SAML Token`() {
        val result =
            mvc
                .perform(
                    MockMvcRequestBuilders
                        .get(SAML_TOKEN)
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password")),
                ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.access_token").isString)
                .andExpect(jsonPath("$.token_type").value(TOKEN_TYPE))
                .andExpect(jsonPath("$.issued_token_type").value("urn:ietf:params:oauth:token-type:saml2"))
                .andExpect(jsonPath("$.expires_in").isNumber)
                .andReturn()

        val mockedToken = objectMapper.readValue(result.response.contentAsString, MockTokenTest::class.java)

        mvc
            .perform(
                MockMvcRequestBuilders
                    .post(SAML_TOKEN_VALIDATE)
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
                    .param(TOKEN_SUBJECT, mockedToken.access_token),
            ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(true))
    }

    @Test
    fun `Validate Expired SAML Token`() {
        mvc
            .perform(
                MockMvcRequestBuilders
                    .post(SAML_TOKEN_VALIDATE)
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
                    .param(TOKEN_SUBJECT, DATAPOWER_SAML_BASE64_ENCODED),
            ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(false))
            .andExpect(
                jsonPath("$.message").isString,
            )
    }

    @Test
    fun `Validate a valid OIDC Token`() {
        val result =
            mvc
                .perform(
                    MockMvcRequestBuilders
                        .get(TOKEN)
                        .param(GRANT_TYPE, "client_credentials")
                        .param(SCOPE, "openid")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password")),
                ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.header().stringValues("Cache-Control", "no-store"))
                .andExpect(MockMvcResultMatchers.header().stringValues("Pragma", "no-cache"))
                .andExpect(jsonPath("$.access_token").isString)
                .andExpect(jsonPath("$.token_type").value(TOKEN_TYPE))
                .andExpect(jsonPath("$.expires_in").value(3600))
                .andReturn()

        val mockedToken = objectMapper.readValue(result.response.contentAsString, MockTokenTest::class.java)

        mvc
            .perform(
                MockMvcRequestBuilders
                    .post(OIDC_TOKEN_VALIDATE)
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
                    .param(TOKEN_SUBJECT, mockedToken.access_token),
            ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(true))
    }

    @Test
    fun `Validate Expired OIDC Token`() {
        mvc
            .perform(
                MockMvcRequestBuilders
                    .post(OIDC_TOKEN_VALIDATE)
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
                    .param(TOKEN_SUBJECT, getOpenAmOIDC()),
            ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(false))
            .andExpect(jsonPath("$.message").value("Validation failed: token has expired"))
    }
}
