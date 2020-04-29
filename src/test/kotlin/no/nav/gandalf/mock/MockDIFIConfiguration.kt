package no.nav.gandalf.mock

import com.nimbusds.oauth2.sdk.`as`.AuthorizationServerMetadata
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata
import no.nav.gandalf.accesstoken.DIFIConfiguration
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
class MockDIFIConfiguration : DIFIConfiguration(){

    override fun getAuthServerMetadata(issuer: String) =
            AuthorizationServerMetadata.parse(MockDIFIConfiguration::class.java.getResource("/__files/difi-maskinporten-configuration.json").readText())!!

    override fun getOIDCProviderMetadata(issuer: String) =
        OIDCProviderMetadata.parse(MockDIFIConfiguration::class.java.getResource("/__files/difi-oidc-configuration.json").readText())!!
}