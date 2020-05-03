package no.nav.gandalf.accesstoken

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import java.net.URL
import java.text.ParseException
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired

private val log = KotlinLogging.logger { }

class OidcIssuerImplDifi(
    override val issuer: String,
    @Autowired val difiConfiguration: DIFIConfiguration
) : OidcIssuer {

    private var jwkSet: JWKSet? = null
    private var jwksUrl: String = getJwksFromIssuer()

    private fun getJwksFromIssuer() = try {
        when {
            issuer.contains("maskinporten") -> {
                difiConfiguration.getAuthServerMetadata(issuer).jwkSetURI.toString()
            }
            else -> {
                difiConfiguration.getOIDCProviderMetadata(issuer).jwkSetURI.toString()
            }
        }
    } catch (e: Exception) {
        log.error(e) { "Failed to read jwks endpoint for issuer: $issuer" }
        throw RuntimeException()
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
