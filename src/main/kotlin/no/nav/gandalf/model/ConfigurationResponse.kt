package no.nav.gandalf.model

data class ConfigurationResponse(
    val issuer: String,
    val token_endpoint: String,
    val exchange_token_endpoint: String,
    val jwks_uri: String,
    val subject_types_supported: List<String>
) {
    companion object Wellknown {
        const val BASE_PATH = "/rest/v1/sts"
        const val TOKEN_PATH = "$BASE_PATH/token"
        const val EXCHANGE_PATH = "$BASE_PATH/token/exchange"
        const val JWKS_PATH = "$BASE_PATH/jwks"
    }
}

internal fun toTokenPath(issuer: String) = issuer + ConfigurationResponse.TOKEN_PATH
internal fun toExchangePath(issuer: String) = issuer + ConfigurationResponse.EXCHANGE_PATH
internal fun toJwksPath(issuer: String) = issuer + ConfigurationResponse.JWKS_PATH
