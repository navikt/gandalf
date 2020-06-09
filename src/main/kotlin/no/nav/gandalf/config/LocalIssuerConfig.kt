package no.nav.gandalf.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
data class LocalIssuerConfig(
    @Value("\${application.oidc.issuer}")
    val issuer: String,
    @Value("\${application.service.username}")
    val issuerUsername: String
)
