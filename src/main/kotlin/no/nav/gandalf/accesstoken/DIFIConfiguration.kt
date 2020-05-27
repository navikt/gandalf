package no.nav.gandalf.accesstoken

import com.nimbusds.oauth2.sdk.`as`.AuthorizationServerConfigurationRequest
import com.nimbusds.oauth2.sdk.`as`.AuthorizationServerMetadata
import com.nimbusds.oauth2.sdk.id.Issuer
import com.nimbusds.openid.connect.sdk.op.OIDCProviderConfigurationRequest
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata
import org.springframework.stereotype.Component

@Component
class DIFIConfiguration {

    fun getAuthServerMetadata(issuer: String): AuthorizationServerMetadata {
        val request = AuthorizationServerConfigurationRequest(Issuer(issuer))
        val httpRequest = request.toHTTPRequest()
        val httpResponse = httpRequest.send()
        return AuthorizationServerMetadata.parse(httpResponse.contentAsJSONObject)
    }

    fun getOIDCProviderMetadata(issuer: String): OIDCProviderMetadata {
        val request = OIDCProviderConfigurationRequest(Issuer(issuer))
        val httpRequest = request.toHTTPRequest()
        val httpResponse = httpRequest.send()
        return OIDCProviderMetadata.parse(httpResponse.contentAsJSONObject)
    }
}
