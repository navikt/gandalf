package no.nav.gandalf.accesstoken

import com.nimbusds.jose.jwk.JWKMatcher
import com.nimbusds.jose.jwk.JWKSelector
import com.nimbusds.jose.jwk.KeyType
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.jwk.source.JWKSourceBuilder
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.oauth2.sdk.OAuth2Error
import com.nimbusds.oauth2.sdk.`as`.AuthorizationServerMetadata
import mu.KotlinLogging
import no.nav.gandalf.http.ProxyAwareResourceRetriever
import org.springframework.util.ResourceUtils
import java.net.URL
import kotlin.time.Duration.Companion.minutes

private val log = KotlinLogging.logger { }

interface IssuerConfig {
    val issuer: String

    fun getKeyByKeyId(keyId: String?): RSAKey?

    companion object {
        fun from(wellKnownUrl: String) =
            issuerConfig {
                log.info { "retrieve metadata from wellknown: $wellKnownUrl" }
                AuthorizationServerMetadata.parse(
                    ProxyAwareResourceRetriever().retrieveResource(wellKnownUrl.toUrl()).content,
                ).let {
                    WellKnown(
                        it.issuer.toString(),
                        it.jwkSetURI.toString(),
                    )
                }
            }

        fun from(
            issuer: String,
            jwksUrl: String,
        ) = issuerConfig {
            WellKnown(issuer, jwksUrl)
        }

        private fun issuerConfig(wellKnownFunction: () -> WellKnown): IssuerConfig =
            object : IssuerConfig {
                val wellKnown: WellKnown by lazy { wellKnownFunction.invoke() }
                val remoteJWKSet: JWKSource<SecurityContext> by lazy {
                    val ttl = 180.minutes
                    val refreshTimeout = 60.minutes
                    val outageTTL = 60.minutes
                    JWKSourceBuilder.create<SecurityContext>(wellKnown.jwksUrl.toUrl(), ProxyAwareResourceRetriever())
                        .cache(true)
                        .rateLimited(false)
                        .outageTolerant(outageTTL.inWholeMilliseconds)
                        .cache(ttl.inWholeMilliseconds, refreshTimeout.inWholeMilliseconds)
                        .build()
                }
                override val issuer: String by lazy { wellKnownFunction.invoke().issuer }

                override fun getKeyByKeyId(keyId: String?): RSAKey = remoteJWKSet.getKeyByKeyId(keyId)
            }

        private fun JWKSource<SecurityContext>.getKeyByKeyId(keyId: String?): RSAKey =
            get(keyId?.toJWKSelector(), null)?.firstOrNull()?.toRSAKey() ?: throw OAuthException(
                OAuth2Error.INVALID_REQUEST.setDescription(
                    "Could not find matching keys in configuration for kid=$keyId",
                ),
            )

        private fun String.toJWKSelector(): JWKSelector =
            JWKSelector(
                JWKMatcher.Builder().keyType(KeyType.RSA).keyID(this).build(),
            )

        private fun String.toUrl(): URL = ResourceUtils.toURL(this)
    }

    private data class WellKnown(
        val issuer: String,
        val jwksUrl: String,
    )
}
