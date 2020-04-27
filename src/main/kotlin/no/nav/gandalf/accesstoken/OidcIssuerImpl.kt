package no.nav.gandalf.accesstoken

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import mu.KotlinLogging
import java.net.URL
import java.text.ParseException

private val log = KotlinLogging.logger { }

class OidcIssuerImpl(
        override val issuer: String,
        private val jwksUrl: String
) : OidcIssuer {

    private var jwkSet: JWKSet? = null

    override fun getKeyByKeyId(keyId: String?): RSAKey? {
        when {
            jwkSet == null || jwkSet!!.getKeyByKeyId(keyId) == null -> {
                jwkSet = try {
                    JWKSet.load(URL(jwksUrl))
                } catch (e: ParseException) {
                    log.error(e) { "Failed to get keys from: $jwksUrl, by issuer : $issuer" }
                    throw IllegalStateException()
                }
            }
        }
        return jwkSet!!.getKeyByKeyId(keyId).toRSAKey()
    }
}