package no.nav.gandalf.api

import io.prometheus.client.CollectorRegistry
import no.nav.gandalf.accesstoken.DIFIConfiguration
import no.nav.gandalf.config.ExternalIssuer
import no.nav.gandalf.utils.difiMASKINPORTENCConfigurationUrl
import no.nav.gandalf.utils.difiMASKINPORTENCJwksUrl
import no.nav.gandalf.utils.difiMASKINPORTENConfigurationResponseFileName
import no.nav.gandalf.utils.difiMASKINPORTENJWKSResponseFileName
import no.nav.gandalf.utils.difiOIDCConfigurationResponseFileName
import no.nav.gandalf.utils.difiOIDCConfigurationUrl
import no.nav.gandalf.utils.difiOIDCJwksUrl
import no.nav.gandalf.utils.difiOIDCResponseFileName
import no.nav.gandalf.utils.endpointStub
import org.apache.http.HttpStatus
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import javax.annotation.PostConstruct

@RunWith(SpringRunner::class)
@SpringBootTest(
    properties = [
        "application.external.configuration.difi.maskinporten=http://localhost:\${wiremock.server.port}$difiMASKINPORTENCConfigurationUrl",
        "application.external.configuration.difi.oidc=http://localhost:\${wiremock.server.port}$difiOIDCConfigurationUrl"
    ],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@EnableConfigurationProperties
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
@DirtiesContext
class DIFIConfigurationTest {

    @Autowired
    private lateinit var externalIssuersConfig: ExternalIssuer

    @PostConstruct
    fun setupKnownIssuers() {
        endpointStub(
            HttpStatus.SC_OK,
            difiMASKINPORTENCConfigurationUrl,
            difiMASKINPORTENConfigurationResponseFileName
        )
        endpointStub(
            HttpStatus.SC_OK,
            difiMASKINPORTENCJwksUrl,
            difiMASKINPORTENJWKSResponseFileName
        )
        endpointStub(
            HttpStatus.SC_OK,
            difiOIDCConfigurationUrl,
            difiOIDCConfigurationResponseFileName
        )
        endpointStub(
            HttpStatus.SC_OK,
            difiOIDCJwksUrl,
            difiOIDCResponseFileName
        )
    }

    @After
    fun tearDown() {
        CollectorRegistry.defaultRegistry.clear()
    }

    @Test
    fun `DIFI Config - AuthServerMetadata`() {
        val difiConfiguration = DIFIConfiguration()
        val content = difiConfiguration.getAuthServerMetadata(externalIssuersConfig.configurationDIFIMaskinportenUrl)
        assert(content.jwkSetURI.toString() == "http://localhost:8888/jwk")
        assert(content.issuer.toString() == "https://ver2.maskinporten.no/")
        assert(content.tokenEndpointURI.toString() == "https://ver2.maskinporten.no/token")
        assert(content.grantTypes[0].toString() == "urn:ietf:params:oauth:grant-type:jwt-bearer")
        assert(content.tokenEndpointAuthMethods[0].toString() == "private_key_jwt")
    }
}
