package no.nav.gandalf.service

import com.nimbusds.jwt.SignedJWT
import no.nav.gandalf.model.AccessTokenResponse

class AccessTokenResponseService(oidcToken: SignedJWT){

    val tokenResponse = AccessTokenResponse(
            access_token = oidcToken.serialize(),
            token_type = "Bearer",
            expires_in = (oidcToken.jwtClaimsSet.expirationTime.time - oidcToken.jwtClaimsSet.issueTime.time) / 1000
    )
}