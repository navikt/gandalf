package no.nav.gandalf.accesstoken

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey

class OidcIssuerImpl(
        override val issuer: String,
        private val jwksUrl: String,
        private val httpClient: HttpClient
) : OidcIssuer {
    private lateinit var jwkSet: JWKSet

    override fun getKeyByKeyId(keyId: String?): RSAKey {
        when {
            jwkSet.getKeyByKeyId(keyId) == null -> {
                // bruk og setup dette riktig
                val jwks: String = httpClient.get(jwksUrl)
                jwkSet = try {
                    JWKSet.parse(jwks)
                } catch (e: Exception) {
                    throw IllegalArgumentException("Failed to get keys from by issuer : $issuer")
                }
            }
        }
        return jwkSet.getKeyByKeyId(keyId) as RSAKey
    }
}