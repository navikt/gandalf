package no.nav.gandalf.model

import com.nimbusds.jwt.SignedJWT

class AccessToken2Response(
    oidcToken: SignedJWT
) {
    var accessToken: String? = null
    var expiresIn: Long? = null
    var idToken: String? = null
    var scope: String? = null
    var tokenType: String? = null

    init {
        accessToken = oidcToken.serialize()
        expiresIn = (oidcToken.jwtClaimsSet.expirationTime.time - oidcToken.jwtClaimsSet.issueTime.time) / 1000
        idToken = accessToken
        scope = "openid"
        tokenType = "Bearer"
    }
}
