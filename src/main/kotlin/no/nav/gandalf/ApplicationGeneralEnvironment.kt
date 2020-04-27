package no.nav.gandalf

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "application.local")
data class ApplicationGeneralEnvironment(
        var issuer: String = "",
        var issuerSrvUser: String = "",
        var openamIssuerUrl: String = "",
        var openamJwksUrl: String = "",
        var azureadIssuerUrl: String = "",
        var azureadJwksUrl: String = "",
        var baseUrl: String = "",
        var difiOIDCIssuer: String = ""
)