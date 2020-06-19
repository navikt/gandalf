package no.nav.gandalf.api

import no.nav.gandalf.utils.ControllerUtil
import no.nav.gandalf.utils.OIDC_TOKEN_VALIDATE
import no.nav.gandalf.utils.SAML_TOKEN_VALIDATE
import no.nav.gandalf.utils.TOKEN_SUBJECT
import no.nav.gandalf.utils.getDatapowerSAMLBase64Encoded
import no.nav.gandalf.utils.getOpenAmOIDC
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
    fun `Validate Expired SAML Token`() {
        mvc.perform(
            MockMvcRequestBuilders.post(SAML_TOKEN_VALIDATE)
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
                .param(TOKEN_SUBJECT, getDatapowerSAMLBase64Encoded)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(false))
            .andExpect(jsonPath("$.message").value("Invalid SAML token: condition NotOnOrAfter is 2018-10-24T09:58:39Z"))
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
            .andExpect(jsonPath("$.message").value("Validation failed: Validation failed: token has expired"))
    }
}
