package no.nav.gandalf.model

import com.nimbusds.oauth2.sdk.GrantType

data class ConfigurationResponse(
    val issuer: String,
    val token_endpoint: String,
    val exchange_token_endpoint: String,
    val jwks_uri: String,
    val subject_types_supported: List<String>,
    val grant_types_supported: List<String>,
    val scopes_supported: List<String>,
    val token_endpoint_auth_methods_supported: List<String>,
    val response_types_supported: List<String>,
    val response_modes_supported: List<String>,
    val id_token_signing_alg_values_supported: List<String>,
) {
    companion object Wellknown {
        const val BASE_PATH = "/rest/v1/sts"
        const val TOKEN_PATH = "$BASE_PATH/token"
        const val EXCHANGE_PATH = "$BASE_PATH/token/exchange"
        const val JWKS_PATH = "/jwks"
        val GRANT_TYPES = listOf("urn:ietf:params:oauth:grant-type:token-exchange", GrantType.CLIENT_CREDENTIALS.value)
        val TOKEN_ENDPOINT_AUTH = listOf("client_secret_basic")
        val SCOPES_SUPPORTED = listOf("openid")
        val RESPONSE_TYPES_SUPPORTED = listOf("id_token token")
        val RESPONSE_MODE_SUPPORTED = listOf("form_post")
        val ID_TOKEN_SIGNING_ALG = listOf("RS256")
        val SUBJECT_TYPES_SUPPORTED = listOf("public")
    }
}

internal fun toTokenPath(issuer: String) = issuer + ConfigurationResponse.TOKEN_PATH
internal fun toExchangePath(issuer: String) = issuer + ConfigurationResponse.EXCHANGE_PATH
internal fun toJwksPath(issuer: String) = issuer + ConfigurationResponse.JWKS_PATH
