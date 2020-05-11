package no.nav.gandalf.accesstoken

import javax.annotation.PostConstruct
import no.nav.gandalf.utils.ControllerUtil
import no.nav.gandalf.utils.JWKS
import no.nav.gandalf.utils.WELL_KNOWN
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

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = no.nav.gandalf.utils.PORT)
@TestPropertySource(locations = ["classpath:application-test.properties"])
@DirtiesContext
class IdentityProviderControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var issuer: AccessTokenIssuer

    @PostConstruct
    fun setupKnownIssuers() {
        ControllerUtil().setupKnownIssuers()
    }

    // Path: /jwks
    @Test
    fun `Get JWKS`() {
        mvc.perform(MockMvcRequestBuilders.get(JWKS)
                .with(SecurityMockMvcRequestPostProcessors.anonymous()))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$..keys").isArray)
    }

    // Path: /.well-known/openid-configuration
    @Test
    fun `Get WELL_KNOWN`() {
        val stsEndpoint = "https://security-token-service.nais.preprod.local"
        mvc.perform(MockMvcRequestBuilders.get(WELL_KNOWN)
                .with(SecurityMockMvcRequestPostProcessors.anonymous()))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.issuer").value(stsEndpoint))
                .andExpect(jsonPath("$.token_endpoint").value("$stsEndpoint/rest/v1/sts/token"))
                .andExpect(jsonPath("$.exchange_token_endpoint").value("$stsEndpoint/rest/v1/sts/token/exchange"))
                .andExpect(jsonPath("$.jwks_uri").value("$stsEndpoint/rest/v1/sts/jwks"))
    }
}
