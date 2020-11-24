package no.nav.gandalf.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.gandalf.utils.ControllerUtil
import no.nav.gandalf.utils.EXCHANGE
import no.nav.gandalf.utils.EXCHANGE_DIFI
import no.nav.gandalf.utils.GRANT_TYPE
import no.nav.gandalf.utils.REQUESTED_TOKEN_TYPE
import no.nav.gandalf.utils.SAML_TOKEN
import no.nav.gandalf.utils.SCOPE
import no.nav.gandalf.utils.SUBJECT_TOKEN
import no.nav.gandalf.utils.SUBJECT_TOKEN_TYPE
import no.nav.gandalf.utils.TOKEN
import no.nav.gandalf.utils.TOKEN_SUBJECT
import no.nav.gandalf.utils.TOKEN_TYPE
import no.nav.gandalf.utils.getDatapowerSAMLBase64Encoded
import no.nav.gandalf.utils.getDifiOidcToken
import no.nav.gandalf.utils.getOpenAmAndDPSamlExchangePair
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import wiremock.org.apache.http.client.entity.UrlEncodedFormEntity
import wiremock.org.apache.http.message.BasicNameValuePair
import wiremock.org.apache.http.util.EntityUtils
import javax.annotation.PostConstruct

@RunWith(SpringRunner::class)
@SpringBootTest(
    properties = [
        "application.jwks.endpoint.azuread=http://localhost:\${wiremock.server.port}/jwk",
        "application.jwks.endpoint.openam=http://localhost:\${wiremock.server.port}/isso/oauth2/connect/jwk_uri"
    ],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
@DirtiesContext
class TokenExchangeControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @PostConstruct
    fun setup() {
        val controllerUtil = ControllerUtil()
        controllerUtil.setupKnownIssuers()
        controllerUtil.setupOverride()
    }

    // Path: /token/exchange
    @Test
    fun `Get Valid SAML and Exchange to OIDC`() {
        val result = mvc.perform(
            MockMvcRequestBuilders.get(SAML_TOKEN)
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.access_token").isString).andReturn()

        val mockedToken = objectMapper.readValue<MockTokenTest>(result.response.contentAsString)

        mvc.perform(
            MockMvcRequestBuilders.post(EXCHANGE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formPostBody(
                    listOf(
                        BasicNameValuePair(GRANT_TYPE, "urn:ietf:params:oauth:grant-type:token-exchange"),
                        BasicNameValuePair(
                            REQUESTED_TOKEN_TYPE,
                            "urn:ietf:params:oauth:token-type:access_token"
                        ),
                        BasicNameValuePair(SUBJECT_TOKEN, mockedToken.access_token),
                        BasicNameValuePair(SUBJECT_TOKEN_TYPE, "urn:ietf:params:oauth:token-type:saml2")
                    )
                )
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.access_token").isString)
            .andExpect(jsonPath("$.token_type").value("Bearer"))
            .andExpect(jsonPath("$.expires_in").isNumber)
            .andExpect(jsonPath("$.issued_token_type").value("urn:ietf:params:oauth:token-type:access_token"))
    }

    @Test
    fun `Get Valid SAML and Exchange to OIDC with Querry parmams should return 415`() {
        val result = mvc.perform(
            MockMvcRequestBuilders.get(SAML_TOKEN)
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.access_token").isString).andReturn()

        val mockedToken = objectMapper.readValue<MockTokenTest>(result.response.contentAsString)

        mvc.perform(
            MockMvcRequestBuilders.post(EXCHANGE)
                .param(GRANT_TYPE, "urn:ietf:params:oauth:grant-type:token-exchange")
                .param(REQUESTED_TOKEN_TYPE, "urn:ietf:params:oauth:token-type:access_token")
                .param(SUBJECT_TOKEN, mockedToken.access_token)
                .param(SUBJECT_TOKEN_TYPE, "urn:ietf:params:oauth:token-type:saml2")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
        )
            .andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType)
    }

    @Test
    fun `Get Token Exchange SAML to OIDC fail with bad-request with expired token`() {
        mvc.perform(
            MockMvcRequestBuilders.post(EXCHANGE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formPostBody(
                    listOf(
                        BasicNameValuePair(GRANT_TYPE, "urn:ietf:params:oauth:grant-type:token-exchange"),
                        BasicNameValuePair(
                            REQUESTED_TOKEN_TYPE,
                            "urn:ietf:params:oauth:token-type:access_token"
                        ),
                        BasicNameValuePair(SUBJECT_TOKEN, getDatapowerSAMLBase64Encoded),
                        BasicNameValuePair(SUBJECT_TOKEN_TYPE, "urn:ietf:params:oauth:token-type:saml2")
                    )
                )
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value(INVALID_REQUEST))
            .andExpect(jsonPath("$.error_description").value("Invalid SAML token: condition NotOnOrAfter is 2018-10-24T09:58:39Z"))
    }

    @Test
    fun `Missing or Unknown Grant type Token Exchange`() {
        mvc.perform(
            MockMvcRequestBuilders.post(EXCHANGE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formPostBody(
                    listOf(
                        BasicNameValuePair(
                            REQUESTED_TOKEN_TYPE,
                            "urn:ietf:params:oauth:token-type:access_token"
                        ),
                        BasicNameValuePair(SUBJECT_TOKEN, getDatapowerSAMLBase64Encoded),
                        BasicNameValuePair(SUBJECT_TOKEN_TYPE, "urn:ietf:params:oauth:token-type:saml2"),
                    )
                )
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value(INVALID_REQUEST))
            .andExpect(jsonPath("$.error_description").value("Unknown grant_type"))
    }

    @Test
    fun `Missing Subject Token - Token Exchange`() {
        mvc.perform(
            MockMvcRequestBuilders.post(EXCHANGE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formPostBody(
                    listOf(
                        BasicNameValuePair(GRANT_TYPE, "urn:ietf:params:oauth:grant-type:token-exchange"),
                        BasicNameValuePair(
                            REQUESTED_TOKEN_TYPE,
                            "urn:ietf:params:oauth:token-type:access_token"
                        ),
                        BasicNameValuePair(SUBJECT_TOKEN_TYPE, "urn:ietf:params:oauth:token-type:saml2"),
                    )
                )
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value(INVALID_REQUEST))
            .andExpect(jsonPath("$.error_description").value("Missing subject_token in request"))
    }

    @Test
    fun `OIDC to SAML Exchange Successfully`() {
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
            MockMvcRequestBuilders.post(EXCHANGE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formPostBody(
                    listOf(
                        BasicNameValuePair(GRANT_TYPE, "urn:ietf:params:oauth:grant-type:token-exchange"),
                        BasicNameValuePair(SUBJECT_TOKEN_TYPE, "urn:ietf:params:oauth:token-type:access_token"),
                        BasicNameValuePair(SUBJECT_TOKEN, mockedToken.access_token),
                    )
                )
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.access_token").isString)
            .andExpect(jsonPath("$.token_type").value("Bearer"))
            .andExpect(jsonPath("$.expires_in").isNumber)
            .andExpect(jsonPath("$.issued_token_type").value("urn:ietf:params:oauth:token-type:saml2"))
    }

    @Test
    fun `OIDC to SAML Exchange - Validation Faild with exipred token`() {
        mvc.perform(
            MockMvcRequestBuilders.post(EXCHANGE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formPostBody(
                    listOf(
                        BasicNameValuePair(GRANT_TYPE, "urn:ietf:params:oauth:grant-type:token-exchange"),
                        BasicNameValuePair(SUBJECT_TOKEN_TYPE, "urn:ietf:params:oauth:token-type:access_token"),
                        BasicNameValuePair(SUBJECT_TOKEN, getOpenAmAndDPSamlExchangePair()[0]),
                    )
                )
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value(INVALID_REQUEST))
            .andExpect(jsonPath("$.error_description").value("Validation failed: token has expired"))
    }

    @Test
    fun `OIDC - Token Exchange - DIFI - oidc-difi-no - Unauthorized Client`() {
        mvc.perform(
            MockMvcRequestBuilders.post(EXCHANGE_DIFI)
                .header(TOKEN_SUBJECT, getDifiOidcToken())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value(INVALID_CLIENT))
            .andExpect(jsonPath("$.error_description").value("Client is Unauthorized for this endpoint"))
    }

    @Test
    fun `OIDC - Token Exchange - DIFI - oidc-difi-no`() {
        mvc.perform(
            MockMvcRequestBuilders.post(EXCHANGE_DIFI)
                .header(TOKEN_SUBJECT, getDifiOidcToken())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvDatapower", "password"))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value(INVALID_REQUEST))
            .andExpect(jsonPath("$.error_description").value("Failed to exchange difi token to oidc token: Validation failed: token has expired"))
    }
}

internal fun MockHttpServletRequestBuilder.formPostBody(formBody: List<BasicNameValuePair>) =
    this.content(
        EntityUtils.toString(
            UrlEncodedFormEntity(formBody)
        )
    )

@JsonIgnoreProperties(ignoreUnknown = true)
data class MockTokenTest(
    val access_token: String
)
