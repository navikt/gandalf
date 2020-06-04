package no.nav.gandalf.accesstoken

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import java.net.URL
import java.text.ParseException

private val log = KotlinLogging.logger { }

class OidcIssuerImplDifi(
    private val configurationUrl: String,
    @Autowired val difiConfiguration: DIFIConfiguration
) : OidcIssuer {

    override lateinit var issuer: String
    private var jwkSet: JWKSet? = null
    private var jwksUrl: String = getJwksFromIssuer()

    private fun getJwksFromIssuer() = try {
        val config = difiConfiguration.getAuthServerMetadata(configurationUrl)
        this.issuer = config.issuer.value
        config.jwkSetURI.toString()
    } catch (e: Exception) {
        log.error(e) { "Failed to read jwks endpoint for issuer: $issuer" }
        throw RuntimeException()
    }

    override fun getKeyByKeyId(keyId: String?): RSAKey? {
        when {
            jwkSet?.getKeyByKeyId(keyId) == null -> {
                jwkSet = try {
                    println(jwksUrl)
                    JWKSet.load(URL(jwksUrl))
                } catch (e: ParseException) {
                    log.error(e) { "Failed to get keys from: $jwksUrl, by issuer : $issuer" }
                    throw IllegalStateException()
                }
            }
        }
        return jwkSet?.getKeyByKeyId(keyId)?.toRSAKey()
            ?: throw RuntimeException("Could not find matching keys in configuration for: $keyId")
    }
}
