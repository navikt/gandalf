package no.nav.gandalf.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
data class LocalIssuer(
    @Value("\${application.oidc.issuer}")
    val issuer: String,
    @Value("\${application.service.username}")
    val issuerUsername: String
)
