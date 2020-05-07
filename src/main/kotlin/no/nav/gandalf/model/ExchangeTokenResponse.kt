package no.nav.gandalf.model

class ExchangeTokenResponse(
        var access_token: String,
        var issued_token_type: String,
        var token_type: String,
        var expires_in: Long
)
