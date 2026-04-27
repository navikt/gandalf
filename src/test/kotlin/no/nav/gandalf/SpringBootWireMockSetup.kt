package no.nav.gandalf

import com.github.tomakehurst.wiremock.WireMockServer
import no.nav.gandalf.SpringBootWireMockSetup.Companion.PROP_EXTERNAL_PREFIX
import no.nav.gandalf.SpringBootWireMockSetup.Companion.PROP_JWKS_ENDPOINT_PREFIX
import no.nav.gandalf.SpringBootWireMockSetup.Companion.TOKENX_WELLKNOWN_PATH
import no.nav.gandalf.SpringBootWireMockSetup.Companion.WIREMOCK_URL
import no.nav.gandalf.utils.AZUREAD_JWKS_FILENAME
import no.nav.gandalf.utils.AZUREAD_JWKS_URL
import no.nav.gandalf.utils.DIFI_CONFIG_FILENAME
import no.nav.gandalf.utils.DIFI_CONFIG_URL
import no.nav.gandalf.utils.DIFI_JWKS_URL
import no.nav.gandalf.utils.DIFI_MASKINPORTEN_CONFIG_FILENAME
import no.nav.gandalf.utils.DIFI_MASKINPORTEN_CONFIG_URL
import no.nav.gandalf.utils.DIFI_MASKINPORTEN_JWKS_FILENAME
import no.nav.gandalf.utils.DIFI_MASKINPORTEN_JWKS_URL
import no.nav.gandalf.utils.DIFI_RESPONSE_FILENAME
import no.nav.gandalf.utils.OPENAM_JWKS_URL
import no.nav.gandalf.utils.OPENAM_RESPONSE_FILENAME
import no.nav.gandalf.utils.endpointStub
import no.nav.gandalf.utils.wellKnownStub
import org.apache.hc.core5.http.HttpStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    properties = [
        "$PROP_EXTERNAL_PREFIX.difi.oidc=$WIREMOCK_URL$DIFI_CONFIG_URL",
        "$PROP_EXTERNAL_PREFIX.difi.maskinporten=$WIREMOCK_URL$DIFI_MASKINPORTEN_CONFIG_URL",
        "token.x.well.known.url=$WIREMOCK_URL$TOKENX_WELLKNOWN_PATH",
        "$PROP_JWKS_ENDPOINT_PREFIX.azuread=$WIREMOCK_URL$AZUREAD_JWKS_URL",
        "$PROP_JWKS_ENDPOINT_PREFIX.azureb2c=$WIREMOCK_URL$AZUREAD_JWKS_URL",
        "$PROP_JWKS_ENDPOINT_PREFIX.openam=$WIREMOCK_URL$OPENAM_JWKS_URL",
    ],
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
)
@ContextConfiguration(initializers = [WireMockInitializer::class])
abstract class SpringBootWireMockSetup {
    @Autowired
    private lateinit var server: WireMockServer

    @Autowired
    protected lateinit var wac: WebApplicationContext

    protected lateinit var mvc: MockMvc

    @BeforeEach
    fun setupKnownIssuers() {
        mvc =
            MockMvcBuilders
                .webAppContextSetup(wac)
                .apply<org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
                .build()
        server.resetAll()
        server.resetAll()
        wellKnownStub(DIFI_CONFIG_URL, server.url(DIFI_JWKS_URL), DIFI_CONFIG_FILENAME)
        wellKnownStub(
            DIFI_MASKINPORTEN_CONFIG_URL,
            server.url(DIFI_MASKINPORTEN_JWKS_URL),
            DIFI_MASKINPORTEN_CONFIG_FILENAME,
        )
        wellKnownStub(TOKENX_WELLKNOWN_PATH, server.url(TOKENX_JWKS_PATH), "tokenx-configuration.json")
        endpointStub(HttpStatus.SC_OK, TOKENX_JWKS_PATH, "tokenx-jwks.json")
        endpointStub(HttpStatus.SC_OK, AZUREAD_JWKS_URL, AZUREAD_JWKS_FILENAME)
        endpointStub(HttpStatus.SC_OK, OPENAM_JWKS_URL, OPENAM_RESPONSE_FILENAME)
        endpointStub(HttpStatus.SC_OK, DIFI_MASKINPORTEN_JWKS_URL, DIFI_MASKINPORTEN_JWKS_FILENAME)
        endpointStub(HttpStatus.SC_OK, DIFI_JWKS_URL, DIFI_RESPONSE_FILENAME)
    }

    companion object {
        const val PROP_EXTERNAL_PREFIX = "application.external.configuration"
        const val PROP_JWKS_ENDPOINT_PREFIX = "application.jwks.endpoint"
        const val WIREMOCK_URL = "http://localhost:\${wiremock.server.port}"
        const val TOKENX_WELLKNOWN_PATH = "/tokenx/.well-known/oauth-authorization-server"
        const val TOKENX_JWKS_PATH = "/tokenx/jwks"
    }
}
