package no.nav.gandalf.api

import jakarta.annotation.PostConstruct
import no.nav.gandalf.SpringBootWireMockSetup
import no.nav.gandalf.utils.ControllerUtil
import no.nav.gandalf.utils.GRANT_TYPE
import no.nav.gandalf.utils.SAML_TOKEN
import no.nav.gandalf.utils.SCOPE
import no.nav.gandalf.utils.TOKEN
import no.nav.gandalf.utils.TOKEN2
import no.nav.gandalf.utils.TOKEN_TYPE
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
import wiremock.org.apache.hc.core5.http.message.BasicNameValuePair

@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
class AccessTokenControllerTest : SpringBootWireMockSetup() {
    @Autowired
    private lateinit var mvc: MockMvc

    @PostConstruct
    fun setup() {
        val controllerUtil = ControllerUtil()
        controllerUtil.setupOverride()
    }

    // GET Path: /token
    @Test
    fun `Get OIDC Token`() {
        mvc.perform(
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
            .andExpect(jsonPath("$.expires_in").value(3600))
    }

    @Test
    fun `Get OIDC Token - Not Known Params`() {
        val unknownGrantType = "client_credential"
        val scope = "openid"
        mvc.perform(
            MockMvcRequestBuilders.get(TOKEN)
                .param(GRANT_TYPE, unknownGrantType)
                .param(SCOPE, "openid")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(jsonPath("$.error").value(INVALID_REQUEST))
            .andExpect(jsonPath("$.error_description").value("grant_type = $unknownGrantType, scope = $scope"))
    }

    @Test
    fun `Get OIDC Token - Missing Params`() {
        mvc.perform(
            MockMvcRequestBuilders.get(TOKEN)
                .param(GRANT_TYPE, "client_credentials")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)

        mvc.perform(
            MockMvcRequestBuilders.get(TOKEN)
                .param(SCOPE, "openid")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `Get OIDC Token - UnAuthorized`() {
        mvc.perform(
            MockMvcRequestBuilders.get(TOKEN)
                .param(GRANT_TYPE, "client_credentials")
                .param(SCOPE, "openid")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPD", "passwor"))
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(jsonPath("$.error").value(INVALID_CLIENT))
            .andExpect(
                jsonPath(
                    "$.error_description"
                ).value(
                    "Unauthorised: Authentication failed, Unable to bind as user 'cn=srvPD,OU=ServiceAccounts,dc=test,dc=local' because no such entry exists in the server."
                )
            )
    }

    // POST Path: /token
    // Should have the same result as for get
    @Test
    fun `POST OIDC Token`() {
        mvc.perform(
            MockMvcRequestBuilders.post(TOKEN)
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
            .andExpect(jsonPath("$.expires_in").value(3600))
    }

    @Test
    fun `POST OIDC Token with Form-Params`() {
        mvc.perform(
            MockMvcRequestBuilders.post(TOKEN)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formPostBody(
                    listOf(
                        BasicNameValuePair(GRANT_TYPE, "client_credentials"),
                        BasicNameValuePair(SCOPE, "openid")
                    )
                )
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.header().stringValues("Cache-Control", "no-store"))
            .andExpect(MockMvcResultMatchers.header().stringValues("Pragma", "no-cache"))
            .andExpect(jsonPath("$.access_token").isString)
            .andExpect(jsonPath("$.token_type").value(TOKEN_TYPE))
            .andExpect(jsonPath("$.expires_in").value(3600))
    }

    // Path: /token2
    @Test
    fun `Get OIDC Token2`() {
        mvc.perform(
            MockMvcRequestBuilders.get(TOKEN2)
                .header("username", "srvPDP")
                .header("password", "password")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.accessToken").isString)
            .andExpect(jsonPath("$.tokenType").value(TOKEN_TYPE))
            .andExpect(jsonPath("$.expiresIn").value(3600))
            .andExpect(jsonPath("$.scope").value("openid"))
            .andExpect(jsonPath("$.idToken").isString)
    }

    @Test
    fun `UnAuthorized Request OIDC Token2`() {
        mvc.perform(
            MockMvcRequestBuilders.get(TOKEN2)
                .header("username", "srvPD")
                .header("password", "passwor")
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    fun `Get OIDC Token2 Missing headers`() {
        mvc.perform(
            MockMvcRequestBuilders.get(TOKEN2)
                .header("username", "srvPDP")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
        )
            .andExpect(MockMvcResultMatchers.status().is4xxClientError)
    }

    // Path: /samltoken
    @Test
    fun `Get SAML Token`() {
        mvc.perform(
            MockMvcRequestBuilders.get(SAML_TOKEN)
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "password"))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.access_token").isString)
            .andExpect(jsonPath("$.token_type").value(TOKEN_TYPE))
            .andExpect(jsonPath("$.issued_token_type").value("urn:ietf:params:oauth:token-type:saml2"))
            .andExpect(jsonPath("$.expires_in").isNumber)
    }

    @Test
    fun `UnAuthorized SAML Token`() {
        mvc.perform(
            MockMvcRequestBuilders.get(SAML_TOKEN)
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("srvPDP", "pasword"))
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(jsonPath("$.error").value(INVALID_CLIENT))
            .andExpect(
                jsonPath(
                    "$.error_description"
                ).value(
                    "Unauthorised: Authentication failed, Unable to bind as user 'cn=srvPDP,OU=ServiceAccounts,dc=test,dc=local' because the provided password was incorrect."
                )
            )
    }
}
