package no.nav.gandalf.model

import com.nimbusds.jwt.SignedJWT

data class AccessTokenResponse(
    var access_token: String = "",
    var token_type: String = "",
    var expires_in: Long = 0,
) {
    constructor(oidcToken: SignedJWT) : this() {
        this.access_token = oidcToken.serialize()
        this.token_type = "Bearer"
        this.expires_in = (oidcToken.jwtClaimsSet.expirationTime.time - oidcToken.jwtClaimsSet.issueTime.time) / 1000
    }
}
