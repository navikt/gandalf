package no.nav.gandalf.accesstoken

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.oauth2.sdk.`as`.AuthorizationServerMetadata
import mu.KotlinLogging
import no.nav.gandalf.http.ProxyAwareResourceRetriever
import java.net.URL

private val log = KotlinLogging.logger { }

interface IssuerConfig {
    val issuer: String
    fun getKeyByKeyId(keyId: String?): RSAKey?

    companion object {
        fun from(wellKnownUrl: String): IssuerConfig =
            object : IssuerConfig {
                private val wellKnown: WellKnown by lazy {
                    wellKnown(wellKnownUrl)
                }
                override val issuer: String
                    get() = wellKnown.issuer
                private var jwkSet: JWKSet? = null

                override fun getKeyByKeyId(keyId: String?): RSAKey? {
                    jwkSet = jwkSet.fetchAndReloadIfNeccessary(keyId, wellKnown.jwksUrl)
                    return jwkSet?.getKeyByKeyId(keyId)?.toRSAKey()
                        ?: throw RuntimeException("Could not find matching keys in configuration for: $keyId")
                }
            }

        fun from(issuer: String, jwksUrl: String) = issuerConfig(
            WellKnown(issuer, jwksUrl)
        )

        private fun wellKnown(url: String?): WellKnown {
            log.info { "retrieve metadata from wellknown: $url" }
            return AuthorizationServerMetadata.parse(
                ProxyAwareResourceRetriever().retrieveResource(URL(url)).content
            ).let {
                WellKnown(
                    it.issuer.toString(),
                    it.jwkSetURI.toString()
                )
            }
        }

        private fun issuerConfig(wellKnown: WellKnown): IssuerConfig = object : IssuerConfig {
            override val issuer: String = wellKnown.issuer
            private var jwkSet: JWKSet? = null

            override fun getKeyByKeyId(keyId: String?): RSAKey? {
                jwkSet = jwkSet.fetchAndReloadIfNeccessary(keyId, wellKnown.jwksUrl)
                return jwkSet?.getKeyByKeyId(keyId)?.toRSAKey()
                    ?: throw RuntimeException("Could not find matching keys in configuration for: $keyId")
            }
        }

        private fun JWKSet?.fetchAndReloadIfNeccessary(keyId: String?, jwksUrl: String): JWKSet? =
            if (this?.getKeyByKeyId(keyId)?.toRSAKey() != null) {
                this
            } else {
                log.info("reloading jwks from endpoint: $jwksUrl for keyid: $keyId")
                JWKSet.load(URL(jwksUrl))
            }
    }

    private data class WellKnown(
        val issuer: String,
        val jwksUrl: String
    )
}
