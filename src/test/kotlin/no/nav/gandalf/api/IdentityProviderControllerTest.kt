package no.nav.gandalf.api

import com.nimbusds.jose.jwk.JWKSet
import no.nav.gandalf.SpringBootWireMockSetup
import no.nav.gandalf.utils.EXCHANGE
import no.nav.gandalf.utils.JWKS
import no.nav.gandalf.utils.TOKEN
import no.nav.gandalf.utils.WELL_KNOWN
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
class IdentityProviderControllerTest : SpringBootWireMockSetup() {
    @Autowired
    private lateinit var mvc: MockMvc

    // Path: /jwks
    @Test
    fun `Get JWKS Should only return Public keys`() {
        val response =
            mvc.perform(
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

    // Path: /.well-known/openid-configuration
    @Test
    fun `Get WELL_KNOWN Deprecated`() {
        val stsEndpoint = "https://security-token-service.nais.preprod.local"
        mvc.perform(
            MockMvcRequestBuilders.get("/rest/v1/sts/.well-known/openid-configuration")
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
