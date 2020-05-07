package no.nav.gandalf.model

class ConfigurationResponse(
    val issuer: String,
    token_path: String,
    exchange_path: String,
    jwks_path: String
) {
    val token_endpoint: String = issuer + token_path
    val exchange_token_endpoint: String = issuer + exchange_path
    val jwks_uri: String = issuer + jwks_path
    val subject_types_supported: Array<String> = arrayOf("public")
}
