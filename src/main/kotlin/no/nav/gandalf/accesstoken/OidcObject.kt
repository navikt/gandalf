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
import java.util.*

class OidcObject {
    var issuer: String? = null
    var version: String? = null
    var id: String
    var subject: String? = null
    private var audience: MutableList<String>? = null
    var azp: String? = null
    var authLevel: String? = null
    var consumerId: String? = null
    var resourceType: String? = null
    var notBeforeTime: Date?
    var issueTime: Date
    var expirationTime: Date
    private var authTime: Date
    var auditTrackingId: String? = null
    private var signedJWT: SignedJWT? = null
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    constructor(issueTime: ZonedDateTime, duration: Long) {
        id = UUID.randomUUID().toString()
        this.issueTime = toDate(issueTime)
        authTime = this.issueTime
        notBeforeTime = this.issueTime
        expirationTime = toDate(issueTime.plusSeconds(duration))
    }

    constructor(issueTime: ZonedDateTime, expirationTime: ZonedDateTime) {
        id = UUID.randomUUID().toString()
        this.issueTime = toDate(issueTime)
        authTime = this.issueTime
        notBeforeTime = this.issueTime
        this.expirationTime = toDate(expirationTime)
    }

    constructor(oidcToken: String?) {
        // parse token
        signedJWT = SignedJWT.parse(oidcToken)
        val claimSet: JWTClaimsSet = signedJWT!!.getJWTClaimsSet()

        // get claims
        issuer = claimSet.issuer
        version = claimSet.getStringClaim(VERSION_CLAIM)
        id = claimSet.jwtid
        subject = claimSet.subject
        audience = claimSet.audience
        azp = claimSet.getStringClaim(AZP_CLAIM)
        authLevel = claimSet.getStringClaim(AUTHLEVEL_CLAIM)
        consumerId = claimSet.getStringClaim(CONSUMERID_CLAIM)
        resourceType = claimSet.getStringClaim(RESOURCETYPE_CLAIM)
        notBeforeTime = claimSet.notBeforeTime
        issueTime = claimSet.issueTime
        expirationTime = claimSet.expirationTime
        authTime = claimSet.getDateClaim(AUTHTIME_CLAIM)
        auditTrackingId = claimSet.getStringClaim(TRACKING_CLAIM)
    }

    @Throws(JOSEException::class)
    fun validate(issuer: OidcIssuer) {
        validate(issuer, toDate(ZonedDateTime.now()))
    }

    @Throws(JOSEException::class)
    fun validate(issuer: OidcIssuer, now: Date) {
        val rsaJwk: RSAKey = issuer.getKeyByKeyId(signedJWT!!.header.keyID)
                ?: throw IllegalArgumentException("Validation failed: failed to find key " + signedJWT!!.header.keyID + " in keys provided by issuer " + issuer.issuer)
        validate(issuer.issuer, now, rsaJwk)
    }

    @Throws(JOSEException::class)
    fun validate(issuer: String?, now: Date, rsaJwk: RSAKey) {
        if (issuer == null || !(issuer == this.issuer)) {
            throw IllegalArgumentException("Validation failed: 'issuer' is null or unknown")
        }

        // check time
        if (notBeforeTime != null && now.before(notBeforeTime)) {
            throw IllegalArgumentException("Validation failed: notBeforeTime validation failed")
        }
        if (now.after(expirationTime)) {
            throw IllegalArgumentException("Validation failed: token has expired")
        }
        val verifier: RSASSAVerifier = RSASSAVerifier(rsaJwk.toRSAPublicKey())
        if (!verifier.verify(signedJWT!!.header, signedJWT!!.signingInput, signedJWT!!.signature)) {
            throw IllegalArgumentException("Validation failed: Signature validation failed")
        }
    }

    fun getSignedToken(key: RSAKey, alg: JWSAlgorithm): SignedJWT? {
        signedJWT = getSignedJWT(jWTClaimsSet, key, alg)
        return signedJWT
    }

    fun getSignedTokenSpec2(key: RSAKey, alg: JWSAlgorithm): SignedJWT? {
        signedJWT = getSignedJWT(jWTClaimsSetSpec2, key, alg)
        return signedJWT
    }

    private val jWTClaimsSet: JWTClaimsSet
        get() = JWTClaimsSet.Builder()
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
                .build()//				.audience(audience.get(0))	// spec2 spesifikk
    // spec2 spesifikk
    // spec2 spesifikk

    // kravene for saml til oidc, DISSE MÅ ENES
    private val jWTClaimsSetSpec2: JWTClaimsSet
        get() { // kravene for saml til oidc, DISSE MÅ ENES
            val clBuilder: JWTClaimsSet.Builder = JWTClaimsSet.Builder()
                    .issuer(issuer)
                    .claim(VERSION_CLAIM, version)
                    .jwtID(id)
                    .subject(subject) //				.audience(audience.get(0))	// spec2 spesifikk
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
            return clBuilder.build()
        }

    private fun getSignedJWT(claimsSet: JWTClaimsSet, key: RSAKey, alg: JWSAlgorithm): SignedJWT {
        log.info("getSignedJWT: Sign the jwt with claimSet")
        try {
            val header: JWSHeader.Builder = JWSHeader.Builder(alg)
                    .keyID(key.keyID)
                    .type(JOSEObjectType.JWT)
            val signedJWT: SignedJWT = SignedJWT(header.build(), claimsSet)
            val signer: JWSSigner = RSASSASigner(key.toPrivateKey())
            signedJWT.sign(signer)
            return signedJWT
        } catch (e: JOSEException) {
            log.warn("Could not sign the jwt with claimSet: $claimsSet")
            throw RuntimeException(e)
        }
    }

    @Throws(ParseException::class)
    fun getClaim(claimName: String?): Any? {
        return (if (signedJWT != null) signedJWT!!.jwtClaimsSet.claims.get(claimName) else null)
    }

    @Throws(ParseException::class)
    fun getSignedTokenCopyAndAddClaimsFrom(copyOidc: OidcObject, copyClaimsList: List<String?>, key: RSAKey, alg: JWSAlgorithm): SignedJWT {
        // copy claims in copyClaimsList from copyOidc to this and add extra claims from copyOidc to this
        val copyClaims: Map<String?, Any> = copyOidc.signedJWT!!.jwtClaimsSet.claims
        val newClaims: Map<String?, Any> = jWTClaimsSet.claims
        val cBuilder: JWTClaimsSet.Builder = JWTClaimsSet.Builder()
        for (cName: String? in newClaims.keys) {
            if (copyClaimsList.contains(cName)) {
                cBuilder.claim(cName, copyClaims.get(cName))
            } else {
                cBuilder.claim(cName, newClaims.get(cName))
            }
        }
        cBuilder.claim(TRACKING_CLAIM, auditTrackingId)
        for (cName: String? in copyClaims.keys) {
            if (!newClaims.containsKey(cName)) {
                cBuilder.claim(cName, copyClaims.get(cName))
            }
        }
        // generate signedJWT
        return getSignedJWT(cBuilder.build(), key, alg)
    }

    fun getAudience(): List<String>? {
        return audience
    }

    val keyId: String?
        get() {
            return (if (signedJWT != null) signedJWT!!.header.keyID else null)
        }

    fun setAudience(audience: MutableList<String>?) {
        this.audience = audience
    }

    fun setAudience(audience: String) {
        this.audience = ArrayList()
        (this.audience as ArrayList<String>).add(audience)
    }

    fun setAudience(aud1: String, aud2: String) {
        audience = ArrayList()
        (audience as ArrayList<String>).add(aud1)
        (audience as ArrayList<String>).add(aud2)
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
        fun toDate(d: ZonedDateTime): Date {
            return Date.from(d.toInstant())
        }
    }
}