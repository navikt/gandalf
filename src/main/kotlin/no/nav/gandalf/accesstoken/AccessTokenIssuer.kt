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
import no.nav.gandalf.config.ExternalIssuerConfig
import no.nav.gandalf.config.LocalIssuerConfig
import no.nav.gandalf.keystore.KeyStoreReader
import no.nav.gandalf.model.Consumer
import no.nav.gandalf.model.IdentType
import no.nav.gandalf.service.RSAKeyStoreService
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
    @Autowired private val keyStore: RSAKeyStoreService,
    @Autowired private val keySelector: KeySelector,
    @Autowired private val keyStoreReader: KeyStoreReader,
    @Autowired private val difiConfiguration: DIFIConfiguration,
    @Autowired private val externalIssuersConfig: ExternalIssuerConfig,
    @Autowired private val localIssuerConfig: LocalIssuerConfig
) : OidcIssuer {

    final override val issuer = localIssuerConfig.issuer
    private val srvUser = localIssuerConfig.issuerUsername

    private val domain = getDomainFromIssuerURL(this.issuer)
    private lateinit var knownIssuers: MutableList<OidcIssuer>

    @PostConstruct
    @Throws(ParseException::class)
    fun setKnownIssuers() {
        knownIssuers = mutableListOf(
            this,
            OidcIssuerImpl(
                externalIssuersConfig.issuerOpenAm,
                externalIssuersConfig.jwksEndpointOpenAm
            ),
            OidcIssuerImpl(
                externalIssuersConfig.issuerAzureAd,
                externalIssuersConfig.jwksEndpointAzuread
            ),
            OidcIssuerImplDifi(
                externalIssuersConfig.configurationDIFIOIDCUrl,
                difiConfiguration
            ),
            OidcIssuerImplDifi(
                externalIssuersConfig.configurationDIFIMaskinportenUrl,
                difiConfiguration
            )
        )
    }

    @Throws(Exception::class)
    fun issueToken(username: String?): SignedJWT? {
        log.info("issueToken for $username")
        require(!(username == null || username.isEmpty())) { "Failed to issue oidc token, username is null" }
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
        require(!(oidcToken == null || oidcToken.isEmpty())) { "Validation failed: OidcToken is null or empty" }
        val oidcObj = OidcObject(oidcToken)
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
        issueTime: ZonedDateTime = ZonedDateTime.now()
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
    fun exchangeSamlToOidcToken(samlToken: String, now: ZonedDateTime? = ZonedDateTime.now()): SignedJWT? {
        log.info("Issuing OIDC token from SAML: exchangeSamlToOidcToken - SAML:$samlToken")

        // read Saml token
        val samlObj = SamlObject(now!!)
        samlObj.read(samlToken)

        // validate token
        samlObj.validate(keySelector)

        // issue new oidc token based on saml token
        val oidcObj = OidcObject(now, samlObj.notOnOrAfter!!.plusSeconds(EXCHANGE_TOKEN_EXTENDED_TIME))
        oidcObj.subject = samlObj.nameID
        oidcObj.issuer = this.issuer
        oidcObj.version = OIDC_VERSION
        // oidcObj.setAudience(getIssuerSrvUser());
        oidcObj.setAudience(samlObj.nameID!!, domain)
        oidcObj.azp = samlObj.nameID
        oidcObj.resourceType = samlObj.identType
        oidcObj.consumerId = samlObj.consumerId
        oidcObj.authLevel = samlObj.authenticationLevel
        return oidcObj.getSignedTokenSpec2(keyStore.currentRSAKey, OIDC_SIGNINGALG)
    }

    @JvmOverloads
    @Throws(Exception::class)
    fun exchangeOidcToSamlToken(
        oidcToken: String,
        username: String?,
        now: Date = OidcObject.toDate(ZonedDateTime.now())
    ): String {
        log.info("Issuing SAML: exchangeOidcToSamlToken from OIDC:$oidcToken")
        // validate oidc token
        val oidcObj: OidcObject = validateOidcToken(oidcToken, now)

        // issue new saml token based on oidc token
        val samlObj = SamlObject(toZonedDateTime(now))
        samlObj.issuer = SAML_ISSUER
        samlObj.setDuration((oidcObj.expirationTime.time - now.time) / 1000 + EXCHANGE_TOKEN_EXTENDED_TIME)
        samlObj.nameID = oidcObj.subject
        when {
            oidcObj.authLevel != null -> {
                samlObj.authenticationLevel = oidcObj.authLevel
            }
            else -> {
                samlObj.authenticationLevel = DEFAULT_SAML_AUTHLEVEL
            }
        }
        samlObj.consumerId = username
        samlObj.identType = getIdentType(samlObj.nameID!!)
        samlObj.auditTrackingId = (when {
            oidcObj.auditTrackingId != null -> oidcObj.auditTrackingId
            else -> oidcObj.id
        })
        return samlObj.getSignedSaml(keyStoreReader)
    }

    @JvmOverloads
    @Throws(Exception::class)
    fun exchangeDifiTokenToOidc(difiToken: String?, now: Date = OidcObject.toDate(ZonedDateTime.now())): SignedJWT {
        log.info("Issuing Internal token from DIFI-token: exchangeDifiTokenToOidc")
        require(!(difiToken == null || difiToken.isEmpty())) { "Validation failed: OidcToken is null or empty" }
        val difiOidcObj = OidcObject(difiToken)
        println(knownIssuers.map { it.issuer })
        println(difiOidcObj.issuer)
        val knownIssuer: OidcIssuer = knownIssuers.map { it }.singleOrNull { it.issuer == difiOidcObj.issuer }
            ?: throw IllegalArgumentException("Validation failed: the oidcToken is issued by unknown issuer: " + difiOidcObj.issuer)
        log.info("DIFI-token issuer: " + knownIssuer.issuer)
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
            keyStore.publicJWKSet == null || keyStore.publicJWKSet != null && keyStore.publicJWKSet!!.keys.isEmpty() -> {
                throw IllegalArgumentException("Failed to get keys from by issuer: $issuer")
            }
            else -> {
                val keyIdResult: JWK? = keyStore.publicJWKSet!!.getKeyByKeyId(keyId)
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
        // seconds
        var OIDC_DURATION_TIME = 60 * 60.toLong()
        var OIDC_VERSION = "1.0"
        var OIDC_SIGNINGALG: JWSAlgorithm = JWSAlgorithm.RS256
        var SAML_ISSUER = "IS02"
        var SAML_DURATION_TIME = 60 * 60.toLong()
        var EXCHANGE_TOKEN_EXTENDED_TIME: Long = 30 // seconds
        var DEFAULT_SAML_AUTHLEVEL = "0"

        fun getDomainFromIssuerURL(issuer: String?): String {
            val domainPrefix = "nais."
            require(!(issuer == null || issuer.length < domainPrefix.length)) { "Failed to find domain from issuerUrl: $issuer" }
            return issuer.substring(issuer.indexOf(domainPrefix) + domainPrefix.length)
        }

        fun getIdentType(subject: String): String {
            if (subject.toLowerCase().startsWith("srv")) {
                return IdentType.SYSTEMRESSURS.value
            }
            return when {
                subject.length == 9 && subject.matches("[0-9]+".toRegex()) -> {
                    IdentType.SAMHANDLER.value
                }
                else -> IdentType.INTERNBRUKER.value
            }
        }

        fun toZonedDateTime(d: Date): ZonedDateTime {
            return ZonedDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault())
        }
    }
}
