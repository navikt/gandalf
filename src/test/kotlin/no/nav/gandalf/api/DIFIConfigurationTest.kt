package no.nav.gandalf.api

import no.nav.gandalf.accesstoken.DIFIConfiguration
import no.nav.gandalf.config.ExternalIssuerConfig
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
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import javax.annotation.PostConstruct

@RunWith(SpringRunner::class)
@SpringBootTest(
    properties = [
        "application.external.issuer.difi.maskinporten=http://localhost:\${wiremock.server.port}/", "application.external.issuer.difi.oidc=http://localhost:\${wiremock.server.port}/idporten-oidc-provider"
    ], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@EnableConfigurationProperties
@AutoConfigureWireMock(port = 0)
@TestPropertySource(locations = ["classpath:application-test.properties"])
class DIFIConfigurationTest {

    @Autowired
    private lateinit var externalIssuersConfig: ExternalIssuerConfig

    @Value("\${wiremock.server.port}")
    private var port: Int = 0

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

    @Test
    fun `DIFI Config - AuthServerMetadata`() {
        val difiConfiguration = DIFIConfiguration()
        val content = difiConfiguration.getAuthServerMetadata(externalIssuersConfig.issuerDifiMaskinporten)
        assert(content.jwkSetURI.toString() == "http://localhost:8888/jwk")
        assert(content.issuer.toString() == "https://ver2.maskinporten.no/")
        assert(content.tokenEndpointURI.toString() == "https://ver2.maskinporten.no/token")
        assert(content.grantTypes[0].toString() == "urn:ietf:params:oauth:grant-type:jwt-bearer")
        assert(content.tokenEndpointAuthMethods[0].toString() == "private_key_jwt")
    }

    @Test
    fun `DIFI Config - OIDCProviderMetadata`() {
        val difiConfiguration = DIFIConfiguration()
        val content = difiConfiguration.getOIDCProviderMetadata(externalIssuersConfig.issuerDifiOIDC)
        assert(content.jwkSetURI.toString() == "http://localhost:8888/idporten-oidc-provider/jwk")
        assert(content.issuer.toString() == "https://oidc-ver2.difi.no/idporten-oidc-provider/")
        assert(content.tokenEndpointURI.toString() == "https://oidc-ver2.difi.no/idporten-oidc-provider/token")
    }
}
