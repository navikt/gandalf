package no.nav.gandalf.accesstoken

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import java.net.URL
import java.text.ParseException
import mu.KotlinLogging
import java.lang.RuntimeException

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
                    JWKSet.load(URL(jwksUrl)).also { log.info { "Load jwks from: $jwksUrl" } }
                } catch (e: ParseException) {
                    log.error(e) { "Failed to get keys from: $jwksUrl." }
                    throw IllegalStateException()
                }
            }
        }
        return jwkSet?.getKeyByKeyId(keyId)?.toRSAKey() ?: throw RuntimeException("Could not find any keys matching keyid: $keyId")
    }
}
