package no.nav.gandalf.accesstoken

import com.google.gson.Gson
import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.ParseException
import mu.KotlinLogging
import no.nav.gandalf.accesstoken.oauth.OidcObject
import no.nav.gandalf.accesstoken.saml.SamlObject
import no.nav.gandalf.config.ExternalIssuer
import no.nav.gandalf.config.LocalIssuer
import no.nav.gandalf.keystore.KeyStoreReader
import no.nav.gandalf.model.Consumer
import no.nav.gandalf.model.IdentType
import no.nav.gandalf.service.RsaKeysProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.xml.sax.SAXException
import java.io.IOException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import javax.annotation.PostConstruct
import javax.xml.crypto.KeySelector
import javax.xml.crypto.MarshalException
import javax.xml.crypto.dsig.XMLSignatureException
import javax.xml.parsers.ParserConfigurationException

private val log = KotlinLogging.logger { }

@Component
class AccessTokenIssuer(
    @Autowired private val keyStore: RsaKeysProvider,
    @Autowired private val keySelector: KeySelector,
    @Autowired private val keyStoreReader: KeyStoreReader,
    @Autowired private val externalIssuersConfig: ExternalIssuer,
    @Autowired private val localIssuerConfig: LocalIssuer
) : IssuerConfig {

    final override val issuer = localIssuerConfig.issuer
    private val domain = getDomainFromIssuerURL(this.issuer)
    private lateinit var knownIssuers: MutableList<IssuerConfig>

    @PostConstruct
    @Throws(ParseException::class)
    fun setKnownIssuers() {
        knownIssuers = mutableListOf(
            this,
            IssuerConfig.from(
                externalIssuersConfig.issuerOpenAm,
                externalIssuersConfig.jwksEndpointOpenAm
            ),
            IssuerConfig.from(
                externalIssuersConfig.issuerAzureB2C,
                externalIssuersConfig.jwksEndpointAzureB2C
            ),
            IssuerConfig.from(
                externalIssuersConfig.issuerAzureAd,
                externalIssuersConfig.jwksEndpointAzuread
            ),
            IssuerConfig.from(
                externalIssuersConfig.configurationDIFIOIDCUrl
            ),
            IssuerConfig.from(
                externalIssuersConfig.configurationDIFIMaskinportenUrl
            ),
            IssuerConfig.from(
                externalIssuersConfig.configurationTokenX
            )
        )
    }

    @Throws(Exception::class)
    fun issueToken(username: String?): SignedJWT {
        log.info("issueToken for $username")
        require(!username.isNullOrEmpty()) { "Failed to issue oidc token, username is null" }
        val oidcObj = OidcObject(ZonedDateTime.now(), OIDC_DURATION_TIME)
        oidcObj.subject = username
        oidcObj.issuer = issuer
        oidcObj.version = OIDC_VERSION
        oidcObj.setAudience(username, domain)
        oidcObj.azp = username
        oidcObj.resourceType = getIdentType(username)
        return oidcObj.getSignedToken(keyStore.currentRSAKey, OIDC_SIGNINGALG)
    }

    @Throws(java.text.ParseException::class, JOSEException::class)
    fun validateOidcToken(oidcToken: String?) = validateOidcToken(oidcToken, OidcObject.toDate(ZonedDateTime.now()))

    @Throws(java.text.ParseException::class, JOSEException::class)
    fun validateOidcToken(oidcToken: String?, now: Date): OidcObject {
        require(!oidcToken.isNullOrEmpty()) { "Validation failed: OidcToken is null or empty" }
        val oidcObj = OidcObject(oidcToken)
        oidcObj.setMaxClockSkew(localIssuerConfig.clockSkewOidc)
        val knownIssuer = knownIssuers.map { it }.singleOrNull { it.issuer == oidcObj.issuer }
            ?: throw IllegalArgumentException("Validation failed: the oidcToken is issued by unknown issuer: " + oidcObj.issuer)
        oidcObj.validate(knownIssuer, now)
        return oidcObj
    }

    @JvmOverloads
    @Throws(Exception::class)
    fun issueSamlToken(
        username: String,
        consumerId: String?,
        authLevel: String?,
        issueTime: ZonedDateTime = ZonedDateTime.now().minusSeconds(SAML_ISSUE_SKEW_SECONDS)
    ): String {
        val samlObj = SamlObject(issueTime)
        samlObj.issuer = SAML_ISSUER
        samlObj.setDuration(SAML_DURATION_TIME)
        samlObj.nameID = username
        samlObj.authenticationLevel = authLevel
        samlObj.consumerId = consumerId
        samlObj.identType = getIdentType(username)
        return samlObj.getSignedSaml(keyStoreReader)
    }

    @Throws(
        ParserConfigurationException::class,
        SAXException::class,
        IOException::class,
        MarshalException::class,
        XMLSignatureException::class
    )
    fun validateSamlToken(samlToken: String?): SamlObject {
        // read Saml token
        val samlObj = SamlObject()
        samlObj.read(samlToken)

        // validate token
        samlObj.validate(keySelector)
        return samlObj
    }

    @Throws(
        ParserConfigurationException::class,
        SAXException::class,
        IOException::class,
        MarshalException::class,
        XMLSignatureException::class
    )
    fun validateSamlToken(samlToken: String?, keySelector: KeySelector) {
        // read Saml token
        val samlObj = SamlObject()
        samlObj.read(samlToken)

        // validate token
        samlObj.validate(keySelector)
    }

    @JvmOverloads
    @Throws(Exception::class)
    fun exchangeSamlToOidcToken(samlToken: String, now: ZonedDateTime = ZonedDateTime.now()): SignedJWT {
        log.info("Issuing OIDC token from SAML: exchangeSamlToOidcToken")

        // read Saml token
        val samlObj = SamlObject(now)
        samlObj.setMaxClockSkew(localIssuerConfig.clockSkewSaml)
        samlObj.read(samlToken)

        // validate token
        samlObj.validate(keySelector)

        // issue new oidc token based on saml token
        val oidcObj = OidcObject(now, samlObj.notOnOrAfter!!.plusSeconds(EXCHANGE_TOKEN_EXTENDED_TIME))
        oidcObj.subject = samlObj.nameID
        oidcObj.issuer = this.issuer
        oidcObj.version = OIDC_VERSION
        oidcObj.setAudience(samlObj.consumerId!!, domain)
        oidcObj.azp = samlObj.consumerId
        oidcObj.resourceType = samlObj.identType
        oidcObj.consumerId = samlObj.consumerId
        oidcObj.authLevel = samlObj.authenticationLevel
        return oidcObj.getSignedTokenSpec2(keyStore.currentRSAKey, OIDC_SIGNINGALG)
    }

    @JvmOverloads
    @Throws(Exception::class)
    fun exchangeOidcToSamlToken(
        token: String,
        username: String?,
        now: Date = OidcObject.toDate(ZonedDateTime.now())
    ): String {
        log.info("Issuing SAML from JWT: exchangeOidcToSamlToken")
        val oidcObj = validateOidcToken(token, now)

        return samlObject(toZonedDateTime(now)) {
            issuer = SAML_ISSUER
            setDuration((oidcObj.expirationTime.time - now.time) / 1000 + EXCHANGE_TOKEN_EXTENDED_TIME)
            nameID = oidcObj.navIdent ?: oidcObj.getClaim("pid") as? String ?: oidcObj.subject
            val level = getAuthenticationLevel(oidcObj)
            val type = getIdentType(nameID!!, getAuthenticationLevel(oidcObj))
            authenticationLevel = defaultLevelIfInternBruker(level, type)
            consumerId = username
            identType = type
            auditTrackingId = oidcObj.auditTrackingId ?: oidcObj.id
        }.getSignedSaml(keyStoreReader)
    }

    private fun defaultLevelIfInternBruker(authLevel: String, identType: String) =
        if (authLevel == DEFAULT_SAML_AUTHLEVEL && identType == IdentType.INTERNBRUKER.value) {
            DEFAULT_INTERN_SAML_AUTHLEVEL
        } else {
            authLevel
        }

    fun samlObject(now: ZonedDateTime, configure: SamlObject.() -> Unit): SamlObject =
        SamlObject(now).apply(configure)

    fun filterIssoInternIssuer() = knownIssuers.singleOrNull { it.issuer.contains(ISSO_OIDC_ISSUER) }

    fun getAuthenticationLevel(oidcObj: OidcObject): String {
        return when {
            oidcObj.authLevel.equals("Level3") -> "3"
            oidcObj.authLevel.equals("Level4") -> "4"
            else -> DEFAULT_SAML_AUTHLEVEL
        }
    }

    @JvmOverloads
    @Throws(Exception::class)
    fun exchangeDifiTokenToOidc(difiToken: String?, now: Date = OidcObject.toDate(ZonedDateTime.now())): SignedJWT {
        log.info("Issuing a Exchange token for DIFI-Accesstoken")
        require(!difiToken.isNullOrEmpty()) { "Validation failed: OidcToken is null or empty" }
        val difiOidcObj = OidcObject(difiToken)
        val knownIssuer: IssuerConfig = knownIssuers.map { it }.singleOrNull { it.issuer == difiOidcObj.issuer }
            ?: throw IllegalArgumentException("Validation failed: the oidcToken is issued by unknown issuer: " + difiOidcObj.issuer)
        log.info("DIFI accessToken from issuer: " + knownIssuer.issuer)
        difiOidcObj.validate(knownIssuer, now)

        // issue new oidc token
        val oidcObj = OidcObject(toZonedDateTime(now), OIDC_DURATION_TIME)
        val subject = getSubjectFromDifiToken(difiOidcObj.getClaim("consumer"))
        // val subject = difiOidcObj.getClaim("client_orgno") as String
        oidcObj.subject = subject
        oidcObj.setOrgno(subject)
        oidcObj.issuer = this.issuer
        oidcObj.version = OIDC_VERSION
        oidcObj.azp = subject
        oidcObj.resourceType = getIdentType(subject)
        oidcObj.auditTrackingId = difiOidcObj.id
        val copyClaims = listOf("aud")
        return oidcObj.getSignedTokenCopyAndAddClaimsFrom(
            difiOidcObj,
            copyClaims,
            keyStore.currentRSAKey,
            OIDC_SIGNINGALG
        )
    }

    fun getSubjectFromDifiToken(consumer: Any?) =
        when {
            consumer != null -> {
                Gson().fromJson(consumer.toString(), Consumer::class.java).ID!!.split(":")[1]
            }
            else -> throw Exception("Could not Deserialize DIFI - Consumer object")
        }

    override fun getKeyByKeyId(keyId: String?): RSAKey? {
        return when {
            keyStore.publicJWKSet.keys.isEmpty() -> {
                throw IllegalArgumentException("Failed to get keys from by issuer: $issuer")
            }
            else -> {
                val keyIdResult: JWK? = keyStore.publicJWKSet.getKeyByKeyId(keyId)
                when {
                    keyIdResult != null -> keyIdResult as RSAKey
                    else -> keyIdResult
                }
            }
        }
    }

    fun getAuthenticationLevel(samlObj: SamlObject): String? {
        return when {
            samlObj.identType.equals(IdentType.EKSTERNBRUKER.name, ignoreCase = true) -> {
                "Level" + samlObj.authenticationLevel
            }
            else -> null
        }
    }

    fun getKeySelector() = keySelector

    fun getPublicJWKSet(): JWKSet? {
        return keyStore.publicJWKSet
    }

    companion object {

        const val SAML_ISSUE_SKEW_SECONDS: Long = 3
        // seconds
        var OIDC_DURATION_TIME = 60 * 60.toLong()
        var OIDC_VERSION = "1.0"
        var OIDC_SIGNINGALG: JWSAlgorithm = JWSAlgorithm.RS256
        var SAML_ISSUER = "IS02"
        var SAML_DURATION_TIME = 60 * 60.toLong()
        var EXCHANGE_TOKEN_EXTENDED_TIME: Long = 30 // seconds
        var DEFAULT_SAML_AUTHLEVEL = "0"
        var DEFAULT_INTERN_SAML_AUTHLEVEL = "4"
        var ISSO_OIDC_ISSUER = "isso"

        fun getDomainFromIssuerURL(issuer: String?): String {
            val domainPrefix = "nais."
            require(!(issuer == null || issuer.length < domainPrefix.length)) { "Failed to find domain from issuerUrl: $issuer" }
            return issuer.substring(issuer.indexOf(domainPrefix) + domainPrefix.length)
        }

        @Throws(java.lang.RuntimeException::class)
        fun getIdentType(subject: String, acrLevel: String? = null): String {
            return when {
                subject.lowercase().startsWith("srv") -> {
                    IdentType.SYSTEMRESSURS.value
                }
                subject.isSamHandler() -> {
                    IdentType.SAMHANDLER.value
                }
                acrLevel.isLeveledForExternal(subject) -> {
                    IdentType.EKSTERNBRUKER.value
                }
                else -> IdentType.INTERNBRUKER.value
            }
        }

        private fun String.isSamHandler() = this.length == 9 && this.isOnlyNumbers()

        private fun String?.isLeveledForExternal(subject: String) = this?.let {
            when {
                !isFnr(subject) -> {
                    false
                }
                isFnr(subject) && this in listOf("3", "4") -> {
                    true
                }
                else -> {
                    throw RuntimeException("Identype: $subject does not have the acrLevel 3 or 4")
                }
            }
        } ?: false

        private fun isFnr(subject: String) = subject.length == 11 && subject.isOnlyNumbers()

        private fun String.isOnlyNumbers() = this.matches("[0-9]+".toRegex())

        fun toZonedDateTime(d: Date): ZonedDateTime {
            return ZonedDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault())
        }
    }
}
