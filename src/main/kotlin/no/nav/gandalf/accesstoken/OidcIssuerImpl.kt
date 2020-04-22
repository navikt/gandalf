package no.nav.gandalf.accesstoken

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import no.nav.gandalf.accesstoken.OidcIssuer

class OidcIssuerImpl(
        override val issuer: String,
        val jwksUrl: String
) : OidcIssuer {
    private lateinit var jwkSet: JWKSet

    override fun getKeyByKeyId(keyId: String?): RSAKey {
        when {
            jwkSet.getKeyByKeyId(keyId) == null -> {
                // bruk og setup dette riktig
                //  val jwks: String = httpClient.send(HttpRequest.newBuilder(URI(jwksUrl)))
                val jwks: String = ""
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