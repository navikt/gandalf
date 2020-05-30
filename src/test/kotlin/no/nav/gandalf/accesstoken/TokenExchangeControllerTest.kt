package no.nav.gandalf.accesstoken

import no.nav.gandalf.api.INVALID_CLIENT
import no.nav.gandalf.api.INVALID_REQUEST
import no.nav.gandalf.utils.ControllerUtil
import no.nav.gandalf.utils.EXCHANGE
import no.nav.gandalf.utils.EXCHANGE_DIFI
import no.nav.gandalf.utils.GRANT_TYPE
import no.nav.gandalf.ldap.InMemoryLdap
import no.nav.gandalf.utils.REQUESTED_TOKEN_TYPE
import no.nav.gandalf.utils.SUBJECT_TOKEN
import no.nav.gandalf.utils.SUBJECT_TOKEN_TYPE
import no.nav.gandalf.utils.TOKEN_SUBJECT
import no.nav.gandalf.utils.getDatapowerSAMLBase64Encoded
import no.nav.gandalf.utils.getDifiOidcToken
import no.nav.gandalf.utils.getOpenAmAndDPSamlExchangePair
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import javax.annotation.PostConstruct

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = no.nav.gandalf.utils.PORT)
@TestPropertySource(locations = ["classpath:application-test.properties"])
@DirtiesContext
class TokenExchangeControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var issuer: AccessTokenIssuer

    private val inMemoryLdap = InMemoryLdap()

    private val controllerUtil = ControllerUtil()

    @PostConstruct
    fun setup() {
        controllerUtil.runLdap(inMemoryLdap)
        controllerUtil.setupKnownIssuers()
        controllerUtil.setupOverride()
    }

    @After
    fun clear() {
        controllerUtil.stopLdap(inMemoryLdap)
    }

    // Path: /token/exchange
    // This test is a 400, token is outdated, this is only to test the api.
    @Test
    fun `Get Token Exchange SAML to OIDC`() {
        mvc.perform(
            MockMvcRequestBuilders.post(EXCHANGE)
                .param(GRANT_TYPE, "urn:ietf:params:oauth:grant-type:token-exchange")
                .param(REQUESTED_TOKEN_TYPE, "urn:ietf:params:oauth:token-type:access_token")
                .param(SUBJECT_TOKEN, getDatapowerSAMLBase64Encoded)
                .param(SUBJECT_TOKEN_TYPE, "urn:ietf:params:oauth:token-type:saml2")
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
                .param(REQUESTED_TOKEN_TYPE, "urn:ietf:params:oauth:token-type:access_token")
                .param(SUBJECT_TOKEN, getDatapowerSAMLBase64Encoded)
                .param(SUBJECT_TOKEN_TYPE, "urn:ietf:params:oauth:token-type:saml2")
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
                .param(GRANT_TYPE, "urn:ietf:params:oauth:grant-type:token-exchange")
                .param(REQUESTED_TOKEN_TYPE, "urn:ietf:params:oauth:token-type:access_token")
                .param(SUBJECT_TOKEN_TYPE, "urn:ietf:params:oauth:token-type:saml2")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value(INVALID_REQUEST))
            .andExpect(jsonPath("$.error_description").value("Missing subject_token in request"))
    }

    @Test
    fun `OIDC - Token Exchange - OIDC to SAML`() {
        mvc.perform(
            MockMvcRequestBuilders.post(EXCHANGE)
                .param(GRANT_TYPE, "urn:ietf:params:oauth:grant-type:token-exchange")
                .param(SUBJECT_TOKEN_TYPE, "urn:ietf:params:oauth:token-type:access_token")
                .param(SUBJECT_TOKEN, getOpenAmAndDPSamlExchangePair()[0])
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
            .andExpect(jsonPath("$.error_description").value("Client is unauthorized for this endpoint"))
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
            .andExpect(jsonPath("$.error_description").value("Failed to exchange difi oidc token to oidc token: Validation failed: token has expired"))
    }
}
