package no.nav.gandalf.accesstoken

import javax.annotation.PostConstruct
import no.nav.gandalf.service.RSAKeyStoreService
import no.nav.gandalf.utils.azureADJwksUrl
import no.nav.gandalf.utils.azureADResponseFileName
import no.nav.gandalf.utils.difiMASKINPORTENCJwksUrl
import no.nav.gandalf.utils.difiMASKINPORTENConfigurationResponseFileName
import no.nav.gandalf.utils.difiOIDCConfigurationResponseFileName
import no.nav.gandalf.utils.difiOIDCConfigurationUrl
import no.nav.gandalf.utils.difiOIDCJwksUrl
import no.nav.gandalf.utils.difiOIDCResponseFileName
import no.nav.gandalf.utils.jwksEndpointStub
import no.nav.gandalf.utils.openAMJwksUrl
import no.nav.gandalf.utils.openAMResponseFileName
import org.apache.http.HttpStatus
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

private const val BASE = "/v1/sts"
private const val TOKEN = "$BASE/token"
private const val PORT = 8888
private const val GRANT_TYPE = "grant_type"
private const val SCOPE = "scope"

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = PORT)
@TestPropertySource(locations = ["classpath:application-test.properties"])
@DirtiesContext
class AccessTokenControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var issuer: AccessTokenIssuer

    @PostConstruct
    fun setupKnownIssuers() {
        jwksEndpointStub(HttpStatus.SC_OK, difiOIDCConfigurationUrl, difiOIDCConfigurationResponseFileName)
        jwksEndpointStub(HttpStatus.SC_OK, azureADJwksUrl, azureADResponseFileName)
        jwksEndpointStub(HttpStatus.SC_OK, openAMJwksUrl, openAMResponseFileName)
        jwksEndpointStub(HttpStatus.SC_OK, difiMASKINPORTENCJwksUrl, difiMASKINPORTENConfigurationResponseFileName)
        jwksEndpointStub(HttpStatus.SC_OK, difiOIDCJwksUrl, difiOIDCResponseFileName)
    }

    @Test
    fun `Get OIDC Token`() {
        mvc.perform(MockMvcRequestBuilders.get(TOKEN)
                .param(GRANT_TYPE, "client_credentials")
                .param(SCOPE, "openid")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password")))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.header().stringValues("Cache-Control", "no-store"))
                .andExpect(MockMvcResultMatchers.header().stringValues("Pragma", "no-cache"))
                .andExpect(jsonPath("$.access_token").isString)
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.expires_in").value(3600))
    }

    @Test
    fun `Get OIDC Token Missing Params`() {
        mvc.perform(MockMvcRequestBuilders.get(TOKEN)
                .param(GRANT_TYPE, "client_credentials")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password")))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError)

        mvc.perform(MockMvcRequestBuilders.get(TOKEN)
                .param(SCOPE, "openid")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password")))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError)
    }
}
