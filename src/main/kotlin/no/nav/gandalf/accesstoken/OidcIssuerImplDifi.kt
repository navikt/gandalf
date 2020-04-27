package no.nav.gandalf.accesstoken

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.oauth2.sdk.`as`.AuthorizationServerConfigurationRequest
import com.nimbusds.oauth2.sdk.`as`.AuthorizationServerMetadata
import com.nimbusds.oauth2.sdk.id.Issuer
import com.nimbusds.openid.connect.sdk.op.OIDCProviderConfigurationRequest
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata
import mu.KotlinLogging
import java.net.URL
import java.text.ParseException


private val log = KotlinLogging.logger { }

class OidcIssuerImplDifi(
        override var issuer: String
) : OidcIssuer {

    private var jwkSet: JWKSet? = null
    private var jwksUrl = getJwksFromIssuer()

    private fun getJwksFromIssuer() = try {
                when {
                    issuer.contains("maskinporten") -> {
                        getAuthServerMetadata().jwkSetURI.toString()
                    }
                    else -> {
                        getOIDCProviderMetadata().jwkSetURI.toString()
                    }
                }
            } catch (e: Exception) {
                log.error("Failed to read wellknown endpoint for issuer: $issuer")
                throw RuntimeException("Failed to read wellknown endpoint: " + e.message)
            }

    private fun getAuthServerMetadata(): AuthorizationServerMetadata {
        val issuer = Issuer(issuer)
        val request = AuthorizationServerConfigurationRequest(issuer)
        val httpRequest = request.toHTTPRequest()
        val httpResponse = httpRequest.send()
        return AuthorizationServerMetadata.parse(httpResponse.contentAsJSONObject)
    }

    private fun getOIDCProviderMetadata(): OIDCProviderMetadata {
        val issuer = Issuer(issuer)
        // Will resolve the OpenID provider metadata automatically
        val request = OIDCProviderConfigurationRequest(issuer)
        // Make HTTP request
        val httpRequest = request.toHTTPRequest()
        val httpResponse = httpRequest.send()
        // Parse OpenID provider metadata
        return OIDCProviderMetadata.parse(httpResponse.contentAsJSONObject)
    }

    override fun getKeyByKeyId(keyId: String?): RSAKey? {
        when {
            jwkSet?.getKeyByKeyId(keyId) == null -> {
                jwkSet = try {
                    JWKSet.load(URL(jwksUrl))
                } catch (e: ParseException) {
                    log.error(e) { "Failed to get keys from: $jwksUrl, by issuer : $issuer" }
                    throw IllegalStateException()
                }
            }
        }
        return jwkSet?.getKeyByKeyId(keyId)?.toRSAKey()
    }
}