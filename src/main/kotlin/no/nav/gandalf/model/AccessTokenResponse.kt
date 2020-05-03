package no.nav.gandalf.model

data class AccessTokenResponse(
    var access_token: String = "",
    var token_type: String = "",
    var expires_in: Long = 0
)
