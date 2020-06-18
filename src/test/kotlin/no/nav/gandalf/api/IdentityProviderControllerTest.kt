package no.nav.gandalf.api

import com.nimbusds.jose.jwk.JWKSet
import io.prometheus.client.CollectorRegistry
import no.nav.gandalf.utils.ControllerUtil
import no.nav.gandalf.utils.EXCHANGE
import no.nav.gandalf.utils.JWKS
import no.nav.gandalf.utils.TOKEN
import no.nav.gandalf.utils.WELL_KNOWN
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
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
class IdentityProviderControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @PostConstruct
    @BeforeAll
    fun setupKnownIssuers() {
        ControllerUtil().setupKnownIssuers()
    }

    @AfterAll
    @After
    fun tearDown() {
        CollectorRegistry.defaultRegistry.clear()
    }

    // Path: /jwks
    @Test
    fun `Get JWKS Should only return Public keys`() {
        val response = mvc.perform(
            MockMvcRequestBuilders.get(JWKS)
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$..keys").isNotEmpty)
            .andExpect(jsonPath("$..keys").isArray)
            .andReturn()

        val jwkSet: JWKSet = JWKSet.parse(response.response.contentAsString)
        jwkSet.keys.forEach {
            assertThat(it.isPrivate).isEqualTo(false)
        }
    }

    // Path: /.well-known/openid-configuration
    @Test
    fun `Get WELL_KNOWN`() {
        val stsEndpoint = "https://security-token-service.nais.preprod.local"
        mvc.perform(
            MockMvcRequestBuilders.get(WELL_KNOWN)
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.issuer").value(stsEndpoint))
            .andExpect(jsonPath("$.token_endpoint").value("$stsEndpoint$TOKEN"))
            .andExpect(jsonPath("$.exchange_token_endpoint").value("$stsEndpoint$EXCHANGE"))
            .andExpect(jsonPath("$.jwks_uri").value("$stsEndpoint$JWKS"))
            .andExpect(jsonPath("$.subject_types_supported").isArray)
    }
}
