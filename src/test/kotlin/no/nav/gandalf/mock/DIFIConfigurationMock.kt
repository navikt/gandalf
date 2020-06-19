package no.nav.gandalf.mock

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nimbusds.oauth2.sdk.`as`.AuthorizationServerMetadata
import no.nav.gandalf.accesstoken.DIFIConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
class DIFIConfigurationMock : DIFIConfiguration() {

    @Value("\${wiremock.server.port: 0}")
    private var port: Int = 0

    override fun getAuthServerMetadata(wellknownUrl: String): AuthorizationServerMetadata {
        return when {
            wellknownUrl.contains("/oauth-authorization-server") -> {
                AuthorizationServerMetadata.parse(
                    jacksonObjectMapper().writeValueAsString(
                        ProviderConfigurationMock(
                            issuer = "https://ver2.maskinporten.no/",
                            jwks_uri = "http://localhost:$port/jwk",
                            token_endpoint = "http://localhost:$port/token",
                            token_endpoint_auth_methods_supported = listOf("private_key_jwt"),
                            grant_types_supported = listOf("urn:ietf:params:oauth:grant-type:jwt-bearer")
                        )
                    )
                )!!
            }
            else -> {
                AuthorizationServerMetadata.parse(
                    jacksonObjectMapper().writeValueAsString(
                        ProviderConfigurationMock(
                            issuer = "https://oidc-ver2.difi.no/idporten-oidc-provider/",
                            jwks_uri = "http://localhost:$port/idporten-oidc-provider/jwk",
                            token_endpoint = "http://localhost:$port/idporten-oidc-provider/token",
                            token_endpoint_auth_methods_supported = listOf("private_key_jwt"),
                            grant_types_supported = listOf("urn:ietf:params:oauth:grant-type:jwt-bearer"),
                            subject_types_supported = listOf("pairwise")
                        )
                    )
                )!!
            }
        }
    }
}
