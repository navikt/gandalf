package no.nav.gandalf.service

import com.nimbusds.jwt.SignedJWT
import no.nav.gandalf.model.ExchangeTokenResponse
import org.springframework.stereotype.Service
import java.util.Base64

@Service
class ExchangeTokenService {
    fun getResponseFrom(oidcToken: SignedJWT) =
        ExchangeTokenResponse(
            access_token = oidcToken.serialize(),
            token_type = "Bearer",
            expires_in = (oidcToken.jwtClaimsSet.expirationTime.time - oidcToken.jwtClaimsSet.issueTime.time) / 1000,
            issued_token_type = "urn:ietf:params:oauth:token-type:access_token",
        )

    fun constructResponse(
        accessToken: String,
        tokenType: String,
        issuedTokenType: String,
        expiresIn: Long,
        UrlEncoding: Boolean,
    ) = ExchangeTokenResponse(
        access_token =
            when {
                UrlEncoding -> {
                    Base64.getUrlEncoder().encodeToString(accessToken.toByteArray())
                }
                else -> {
                    Base64.getEncoder().encodeToString(accessToken.toByteArray())
                }
            },
        token_type = tokenType,
        expires_in = expiresIn,
        issued_token_type = issuedTokenType,
    )
}
