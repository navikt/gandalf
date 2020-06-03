package no.nav.gandalf.api

import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.ldap.InMemoryLdap
import no.nav.gandalf.utils.ControllerUtil
import no.nav.gandalf.utils.OIDC_TOKEN_VALIDATE
import no.nav.gandalf.utils.SAML_TOKEN_VALIDATE
import no.nav.gandalf.utils.TOKEN_SUBJECT
import no.nav.gandalf.utils.getDatapowerSAMLBase64Encoded
import no.nav.gandalf.utils.getOpenAmOIDC
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
class ValidateControllerTest {

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
    fun `Validate Expired SAML Token`() {
        mvc.perform(
            MockMvcRequestBuilders.post(SAML_TOKEN_VALIDATE)
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
                .param(TOKEN_SUBJECT, getDatapowerSAMLBase64Encoded)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(false))
            .andExpect(jsonPath("$.message").value("Validation failed: Invalid SAML token: condition NotOnOrAfter is 2018-10-24T09:58:39Z"))
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
