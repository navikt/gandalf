package no.nav.gandalf.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
data class ExternalIssuerConfig(
    @Value("\${application.external.issuer.openam}")
    val issuerOpenAm: String,
    @Value("\${application.jwks.endpoint.openam}")
    val jwksEndpointOpenAm: String,
    @Value("\${application.external.issuer.azuread}")
    val issuerAzureAd: String,
    @Value("\${application.jwks.endpoint.azuread}")
    val jwksEndpointAzuread: String,
    @Value("\${application.external.issuer.difi.oidc}")
    val issuerDifiOIDC: String,
    @Value("\${application.external.issuer.difi.maskinporten}")
    val issuerDifiMaskinporten: String
)
