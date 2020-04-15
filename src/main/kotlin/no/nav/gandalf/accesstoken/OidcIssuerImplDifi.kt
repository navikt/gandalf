package no.nav.gandalf.accesstoken

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.oauth2.sdk.`as`.AuthorizationServerMetadata
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata
import mu.KotlinLogging
import java.net.http.HttpClient

private val log = KotlinLogging.logger { }

class OidcIssuerImplDifi(
        wellknownUrl: String,
        wellknownApiKey: String,
        private val jwksUrl: String,
        private var jwkSet: JWKSet,
        private val jwksApiGwKey: String,
        httpClient: HttpClient
) : OidcIssuer {

    override var issuer = getIssuerFromDifi(wellknownUrl, wellknownApiKey, httpClient)

    private fun getIssuerFromDifi(wellknownUrl: String, wellknownApiKey: String, httpClient: HttpClient): String {
        // get issuer from wellknown/configuration endpoint
        val issuer: String
        return try {
            val providerInfo = ""
            // val providerInfo: String = httpClient.makeGetRequestWithApiGWKey(wellknownUrl, wellknownApiKey)
            // maskinoidc er relatert til gatewayen sin url
            issuer = if (wellknownUrl.contains("maskinoidc")) {
                AuthorizationServerMetadata.parse(providerInfo).getIssuer().getValue()
            } else {
                OIDCProviderMetadata.parse(providerInfo).issuer.value
            }
            issuer
        } catch (e: Exception) {
            log.error("Failed to read wellknown endpoint: $wellknownUrl")
            throw RuntimeException("Failed to read wellknown endpoint: " + e.message)
        }
    }

    override fun getKeyByKeyId(keyId: String?) =
            when {
                jwkSet.getKeyByKeyId(keyId) == null -> {
                    val jwks = ""
                    //val jwks: String = httpClient.makeGetRequestWithApiGWKey(jwksUrl, jwksApiGwKey)
                    try {
                        JWKSet.parse(jwks)
                        jwkSet.getKeyByKeyId(keyId) as RSAKey
                    } catch (e: Exception) {
                        throw IllegalArgumentException("Failed to get keys from by issuer : $issuer")
                    }
                }
                else -> {
                    jwkSet.getKeyByKeyId(keyId) as RSAKey
                }
            }
}