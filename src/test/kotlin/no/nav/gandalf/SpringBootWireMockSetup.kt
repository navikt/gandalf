package no.nav.gandalf

import com.github.tomakehurst.wiremock.WireMockServer
import no.nav.gandalf.SpringBootWireMockSetup.Companion.PROP_EXTERNAL_PREFIX
import no.nav.gandalf.SpringBootWireMockSetup.Companion.PROP_JWKS_ENDPOINT_PREFIX
import no.nav.gandalf.SpringBootWireMockSetup.Companion.TOKENX_WELLKNOWN_PATH
import no.nav.gandalf.SpringBootWireMockSetup.Companion.WIREMOCK_URL
import no.nav.gandalf.utils.azureADJwksUrl
import no.nav.gandalf.utils.azureADResponseFileName
import no.nav.gandalf.utils.difiMASKINPORTENCConfigurationUrl
import no.nav.gandalf.utils.difiMASKINPORTENCJwksUrl
import no.nav.gandalf.utils.difiMASKINPORTENConfigurationResponseFileName
import no.nav.gandalf.utils.difiMASKINPORTENJWKSResponseFileName
import no.nav.gandalf.utils.difiOIDCConfigurationResponseFileName
import no.nav.gandalf.utils.difiOIDCConfigurationUrl
import no.nav.gandalf.utils.difiOIDCJwksUrl
import no.nav.gandalf.utils.difiOIDCResponseFileName
import no.nav.gandalf.utils.endpointStub
import no.nav.gandalf.utils.openAMJwksUrl
import no.nav.gandalf.utils.openAMResponseFileName
import no.nav.gandalf.utils.wellKnownStub
import org.apache.http.HttpStatus
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(
    properties = [
        "$PROP_EXTERNAL_PREFIX.difi.oidc=$WIREMOCK_URL$difiOIDCConfigurationUrl",
        "$PROP_EXTERNAL_PREFIX.difi.maskinporten=$WIREMOCK_URL$difiMASKINPORTENCConfigurationUrl",
        "$PROP_EXTERNAL_PREFIX.tokenx=$WIREMOCK_URL$TOKENX_WELLKNOWN_PATH",
        "$PROP_JWKS_ENDPOINT_PREFIX.azuread=$WIREMOCK_URL$azureADJwksUrl",
        "$PROP_JWKS_ENDPOINT_PREFIX.azureb2c=$WIREMOCK_URL$azureADJwksUrl",
        "$PROP_JWKS_ENDPOINT_PREFIX.openam=$WIREMOCK_URL$openAMJwksUrl",
    ],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWireMock(port = 0)
abstract class SpringBootWireMockSetup {

    @Autowired
    private lateinit var server: WireMockServer

    @Before
    fun setupKnownIssuers() {
        wellKnownStub(difiOIDCConfigurationUrl, server.url(difiOIDCJwksUrl), difiOIDCConfigurationResponseFileName)
        wellKnownStub(difiMASKINPORTENCConfigurationUrl, server.url(difiMASKINPORTENCJwksUrl), difiMASKINPORTENConfigurationResponseFileName)
        wellKnownStub(TOKENX_WELLKNOWN_PATH, server.url(TOKENX_JWKS_PATH), "tokenx-configuration.json")
        endpointStub(HttpStatus.SC_OK, TOKENX_JWKS_PATH, "tokenx-jwks.json")
        endpointStub(HttpStatus.SC_OK, azureADJwksUrl, azureADResponseFileName)
        endpointStub(HttpStatus.SC_OK, openAMJwksUrl, openAMResponseFileName)
        endpointStub(HttpStatus.SC_OK, difiMASKINPORTENCJwksUrl, difiMASKINPORTENJWKSResponseFileName)
        endpointStub(HttpStatus.SC_OK, difiOIDCJwksUrl, difiOIDCResponseFileName)
    }

    companion object {
        const val PROP_EXTERNAL_PREFIX = "application.external.configuration"
        const val PROP_JWKS_ENDPOINT_PREFIX = "application.jwks.endpoint"
        const val WIREMOCK_URL = "http://localhost:\${wiremock.server.port}"
        const val TOKENX_WELLKNOWN_PATH = "/tokenx/.well-known/oauth-authorization-server"
        const val TOKENX_JWKS_PATH = "/tokenx/jwks"
    }
}
