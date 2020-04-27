package no.nav.gandalf.model

import com.nimbusds.jwt.SignedJWT
import org.apache.commons.codec.binary.Base64

class ExchangeTokenResponse {
    var access_token: String? = null
    var issued_token_type: String? = null
    var token_type: String? = null
    var expires_in: Long? = null

    constructor(oidcToken: SignedJWT) {
        access_token = oidcToken.serialize()
        token_type = "Bearer"
        expires_in = (oidcToken.jwtClaimsSet.expirationTime.time - oidcToken.jwtClaimsSet.issueTime.time) / 1000
        issued_token_type = "urn:ietf:params:oauth:token-type:access_token"
    }

    constructor(accessToken: String, tokenType: String?, issuedTokenType: String?, expiresIn: Long?, UrlEncoding: Boolean) {
        access_token = when {
            UrlEncoding -> {
                Base64.encodeBase64URLSafeString(accessToken.toByteArray())
            }
            else -> {
                Base64.encodeBase64String(accessToken.toByteArray())
            }
        }
        token_type = tokenType
        expires_in = expiresIn
        issued_token_type = issuedTokenType
    }

}