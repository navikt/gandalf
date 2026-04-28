package no.nav.gandalf.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class LocalIssuer(
    @param:Value("\${application.oidc.issuer}")
    val issuer: String,
    @param:Value("\${application.service.username}")
    val issuerUsername: String,
    @param:Value("\${application.clock.skew.saml}")
    val clockSkewSaml: Long,
    @param:Value("\${application.clock.skew.oidc}")
    val clockSkewOidc: Long,
)
