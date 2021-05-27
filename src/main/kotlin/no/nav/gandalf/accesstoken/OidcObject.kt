package no.nav.gandalf.accesstoken

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.ParseException
import java.time.ZonedDateTime
import java.util.Date
import java.util.UUID

class OidcObject {
    var issuer: String? = null
    var version: String? = null
    var id: String? = null
    var subject: String? = null
    private var audience: MutableList<String>? = null
    private var orgno: String? = null
    var azp: String? = null
    var authLevel: String? = null
    var consumerId: String? = null
    var resourceType: String? = null
    var notBeforeTime: Date?
    var issueTime: Date
    var expirationTime: Date
    private var authTime: Long? = null
    var auditTrackingId: String? = null
    var navIdent: String? = null
    private var signedJWT: SignedJWT? = null
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    constructor(issueTime: ZonedDateTime, duration: Long) {
        id = UUID.randomUUID().toString()
        this.issueTime = toDate(issueTime)
        authTime = this.issueTime.toInstant().epochSecond
        notBeforeTime = this.issueTime
        expirationTime = toDate(issueTime.plusSeconds(duration))
    }

    constructor(issueTime: ZonedDateTime?, expirationTime: ZonedDateTime) {
        id = UUID.randomUUID().toString()
        this.issueTime = toDate(issueTime)
        authTime = this.issueTime.toInstant().epochSecond
        notBeforeTime = this.issueTime
        this.expirationTime = toDate(expirationTime)
    }

    constructor(oidcToken: String) {
        // parse token
        signedJWT = oidcToken.parse()
        val claimSet: JWTClaimsSet = signedJWT!!.jwtClaimsSet

        // get claims
        issuer = claimSet.issuer
        version = claimSet.getStringClaim(VERSION_CLAIM)
        id = claimSet.jwtid
        subject = claimSet.subject
        navIdent = claimSet.getStringClaim(NAV_IDENT_CLAIM)
        audience = claimSet.audience
        azp = claimSet.getStringClaim(AZP_CLAIM)
        authLevel = claimSet.getStringClaim(AUTHLEVEL_CLAIM)
        consumerId = claimSet.getStringClaim(CONSUMERID_CLAIM)
        resourceType = claimSet.getStringClaim(RESOURCETYPE_CLAIM)
        notBeforeTime = claimSet.notBeforeTime
        issueTime = claimSet.issueTime
        expirationTime = claimSet.expirationTime
        if (claimSet.getDateClaim(AUTHTIME_CLAIM) != null) {
            authTime = claimSet.getDateClaim(AUTHTIME_CLAIM).toInstant().epochSecond
        }
        auditTrackingId = claimSet.getStringClaim(TRACKING_CLAIM)
    }

    @Throws(ParseException::class)
    fun String.parse(): SignedJWT = try {
        SignedJWT.parse(this)
    } catch (p: ParseException) {
        log.error("Could not parse token")
        throw p
    }

    @Throws(JOSEException::class)
    fun validate(issuer: OidcIssuer, rsaJwk: RSAKey) {
        validate(issuer.issuer, toDate(ZonedDateTime.now()), rsaJwk)
    }

    @Throws(JOSEException::class)
    fun validate(issuer: OidcIssuer, now: Date) {
        when (val rsaJwk = issuer.getKeyByKeyId(signedJWT!!.header.keyID)) {
            null -> {
                throw IllegalArgumentException("Validation failed: failed to find key " + signedJWT!!.header.keyID + " in keys provided by issuer " + issuer.issuer)
            }
            else -> {
                validate(issuer.issuer, now, rsaJwk)
            }
        }
    }

    @Throws(JOSEException::class)
    fun validate(issuer: String?, now: Date, rsaJwk: RSAKey) {
        when {
            issuer == null || issuer != this.issuer -> {
                throw IllegalArgumentException("Validation failed: 'issuer' is null or unknown")
            }
            // check time
            notBeforeTime != null && now.before(notBeforeTime) -> {
                throw IllegalArgumentException("Validation failed: notBeforeTime validation failed")
            }
            now.after(expirationTime) -> {
                throw IllegalArgumentException("Validation failed: token has expired")
            }
            else -> {
                val verifier = RSASSAVerifier(rsaJwk.toRSAPublicKey())
                if (!verifier.verify(signedJWT!!.header, signedJWT!!.signingInput, signedJWT!!.signature)) {
                    throw IllegalArgumentException("Validation failed: Signature validation failed")
                }
            }
        }
    }

    fun getSignedToken(key: RSAKey, alg: JWSAlgorithm) = getSignedJWT(jWTClaimsSet, key, alg)

    fun getSignedTokenSpec2(key: RSAKey, alg: JWSAlgorithm) = getSignedJWT(jWTClaimsSetSpec2, key, alg)

    private val jWTClaimsSet: JWTClaimsSet
        get() {
            val clBuilder: JWTClaimsSet.Builder = JWTClaimsSet.Builder()
                .issuer(issuer)
                .claim(VERSION_CLAIM, version)
                .jwtID(id)
                .subject(subject)
                .audience(audience)
                .claim(AUTHTIME_CLAIM, authTime)
                .notBeforeTime(notBeforeTime)
                .issueTime(issueTime)
                .expirationTime(expirationTime)
                .claim(AZP_CLAIM, azp)
                .claim(RESOURCETYPE_CLAIM, resourceType)

            if (orgno != null) {
                clBuilder.claim(CLIENT_ORGNO_CLAIM, orgno)
            }
            if (navIdent != null) {
                clBuilder.claim(NAV_IDENT_CLAIM, navIdent)
            }
            return clBuilder.build()
        }

    // kravene for saml til oidc, DISSE MÅ ENES
    private val jWTClaimsSetSpec2: JWTClaimsSet
        get() { // kravene for saml til oidc, DISSE MÅ ENES
            val clBuilder: JWTClaimsSet.Builder = JWTClaimsSet.Builder()
                .issuer(issuer)
                .claim(VERSION_CLAIM, version)
                .jwtID(id)
                .subject(subject) // 				.audience(audience.get(0))	// spec2 spesifikk
                .audience(audience)
                .claim(AUTHTIME_CLAIM, issueTime)
                .notBeforeTime(issueTime)
                .issueTime(issueTime)
                .expirationTime(expirationTime)
                .claim(AZP_CLAIM, azp)
                .claim(RESOURCETYPE_CLAIM, resourceType)
                .claim(CONSUMERID_CLAIM, consumerId) // spec2 spesifikk
                .claim(UTY_CLAIM, resourceType) // spec2 spesifikk
            if (authLevel != null) {
                clBuilder.claim(AUTHLEVEL_CLAIM, authLevel)
            }
            if (navIdent != null) {
                clBuilder.claim(NAV_IDENT_CLAIM, navIdent)
            }
            return clBuilder.build()
        }

    private fun getSignedJWT(claimsSet: JWTClaimsSet, key: RSAKey, alg: JWSAlgorithm): SignedJWT {
        log.info("Sign the jwt with claimSet for issuer: ${claimsSet.issuer}")
        try {
            val header: JWSHeader.Builder = JWSHeader.Builder(alg)
                .keyID(key.keyID)
                .type(JOSEObjectType.JWT)
            val signedJWT = SignedJWT(header.build(), claimsSet)
            val signer: JWSSigner = RSASSASigner(key.toPrivateKey())
            signedJWT.sign(signer)
            return signedJWT
        } catch (e: JOSEException) {
            log.error("Could not sign the jwt with claimSet: $claimsSet")
            throw RuntimeException(e)
        }
    }

    @Throws(ParseException::class)
    fun getClaim(claimName: String?): Any? {
        return (if (signedJWT != null) signedJWT!!.jwtClaimsSet.claims[claimName] else null)
    }

    @Throws(ParseException::class)
    fun getSignedTokenCopyAndAddClaimsFrom(
        copyOidc: OidcObject,
        copyClaimsList: List<String?>,
        key: RSAKey,
        alg: JWSAlgorithm
    ): SignedJWT {
        // copy claims in copyClaimsList from copyOidc to this and add extra claims from copyOidc to this
        val copyClaims: Map<String?, Any> = copyOidc.signedJWT!!.jwtClaimsSet.claims
        val newClaims: Map<String?, Any> = jWTClaimsSet.claims
        val cBuilder: JWTClaimsSet.Builder = JWTClaimsSet.Builder()
        for (cName: String? in newClaims.keys) {
            when {
                copyClaimsList.contains(cName) -> {
                    cBuilder.claim(cName, copyClaims[cName])
                }
                else -> {
                    cBuilder.claim(cName, newClaims[cName])
                }
            }
        }
        cBuilder.claim(TRACKING_CLAIM, auditTrackingId)
        for (cName: String? in copyClaims.keys) {
            if (!newClaims.containsKey(cName)) {
                cBuilder.claim(cName, copyClaims[cName])
            }
        }
        // generate signedJWT
        return getSignedJWT(cBuilder.build(), key, alg)
    }

    val keyId: String?
        get() {
            return when {
                signedJWT != null -> signedJWT!!.header.keyID
                else -> null
            }
        }

    fun setAudience(aud1: String, aud2: String) {
        audience = ArrayList()
        (audience as ArrayList<String>).add(aud1)
        (audience as ArrayList<String>).add(aud2)
    }

    fun setOrgno(orgno: String) {
        this.orgno = orgno
    }

    companion object {
        var VERSION_CLAIM: String = "ver"
        var CONSUMERID_CLAIM: String = "cid"
        var AUTHLEVEL_CLAIM: String = "acr"
        var RESOURCETYPE_CLAIM: String = "identType"
        var AUTHTIME_CLAIM: String = "auth_time"
        var AZP_CLAIM: String = "azp"
        var UTY_CLAIM: String = "uty"
        var TRACKING_CLAIM: String = "auditTrackingId"
        var CLIENT_ORGNO_CLAIM = "client_orgno"
        var NAV_IDENT_CLAIM = "NAVident"
        fun toDate(d: ZonedDateTime?): Date {
            return Date.from(d!!.toInstant())
        }
    }
}
