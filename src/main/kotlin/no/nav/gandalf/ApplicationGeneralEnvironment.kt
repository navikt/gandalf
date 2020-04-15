package no.nav.gandalf

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "application")
data class ApplicationGeneralEnvironment(
        var issuer: String = "",
        var issuerSrvUser: String = ""
)