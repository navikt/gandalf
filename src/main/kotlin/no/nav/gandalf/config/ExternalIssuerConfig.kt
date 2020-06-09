package no.nav.gandalf.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
data class ExternalIssuerConfig(
    @Value("\${application.external.issuer.openam}")
    val issuerOpenAm: String,
    @Value("\${application.jwks.endpoint.openam}")
    val jwksEndpointOpenAm: String,
    @Value("\${application.external.issuer.azuread}")
    val issuerAzureAd: String,
    @Value("\${application.jwks.endpoint.azuread}")
    val jwksEndpointAzuread: String,
    @Value("\${application.external.configuration.difi.oidc}")
    val configurationDIFIOIDCUrl: String,
    @Value("\${application.external.configuration.difi.maskinporten}")
    val configurationDIFIMaskinportenUrl: String
)
