package no.nav.gandalf.mock

data class ProviderConfigurationMokk(
    val issuer: String,
    val token_endpoint: String,
    val jwks_uri: String,
    val token_endpoint_auth_methods_supported: List<String>,
    val grant_types_supported: List<String>,
    var subject_types_supported: List<String> = emptyList()
)
