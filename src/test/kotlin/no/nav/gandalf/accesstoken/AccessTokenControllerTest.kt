package no.nav.gandalf.accesstoken

import no.nav.gandalf.utils.ControllerUtil
import no.nav.gandalf.utils.GRANT_TYPE
import no.nav.gandalf.utils.SAML_TOKEN
import no.nav.gandalf.utils.SCOPE
import no.nav.gandalf.utils.TOKEN
import no.nav.gandalf.utils.TOKEN2
import no.nav.gandalf.utils.TOKEN_TYPE
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
import javax.annotation.PostConstruct

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = no.nav.gandalf.utils.PORT)
@TestPropertySource(locations = ["classpath:application-test.properties"])
@DirtiesContext
class AccessTokenControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var issuer: AccessTokenIssuer

    @PostConstruct
    fun setupKnownIssuers() {
        ControllerUtil().setupKnownIssuers()
    }

    // Path: /token
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
                .andExpect(jsonPath("$.token_type").value(TOKEN_TYPE))
                .andExpect(jsonPath("$.expires_in").value(3600))
    }

    // TODO test more errors when LDAP is in place
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

    // Path: /token2
    @Test
    fun `Get OIDC Token2`() {
        mvc.perform(MockMvcRequestBuilders.get(TOKEN2)
                .header("username", "srvPDP")
                .header("password", "password")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password")))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").isString)
                .andExpect(jsonPath("$.tokenType").value(TOKEN_TYPE))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.scope").value("openid"))
                .andExpect(jsonPath("$.idToken").isString)
    }

    @Test
    fun `Get OIDC Token2 Missing headers`() {
        mvc.perform(MockMvcRequestBuilders.get(TOKEN2)
                .header("username", "srvPDP")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password")))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError)
    }

    // Path: /samltoken
    @Test
    fun `Get SAML Token`() {
        mvc.perform(MockMvcRequestBuilders.get(SAML_TOKEN)
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password")))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.access_token").isString)
                .andExpect(jsonPath("$.token_type").value(TOKEN_TYPE))
                .andExpect(jsonPath("$.issued_token_type").value("urn:ietf:params:oauth:token-type:saml2"))
                .andExpect(jsonPath("$.expires_in").isNumber)
    }
}
