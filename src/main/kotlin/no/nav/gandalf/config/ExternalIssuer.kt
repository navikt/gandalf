package no.nav.gandalf.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class ExternalIssuer(
    @param:Value("\${application.external.issuer.openam}")
    val issuerOpenAm: String,
    @param:Value("\${application.jwks.endpoint.openam}")
    val jwksEndpointOpenAm: String,
    @param:Value("\${application.external.issuer.azureb2c}")
    val issuerAzureB2C: String,
    @param:Value("\${application.jwks.endpoint.azureb2c}")
    val jwksEndpointAzureB2C: String,
    @param:Value("\${application.external.issuer.azuread}")
    val issuerAzureAd: String,
    @param:Value("\${application.jwks.endpoint.azuread}")
    val jwksEndpointAzuread: String,
    @param:Value("\${application.external.configuration.difi.oidc}")
    val configurationDIFIOIDCUrl: String,
    @param:Value("\${application.external.configuration.difi.maskinporten}")
    val configurationDIFIMaskinportenUrl: String,
    @param:Value("\${token.x.well.known.url}")
    val configurationTokenX: String,
)
