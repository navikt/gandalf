package no.nav.gandalf.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
data class LdapConfig(
    @Value("\${spring.ldap.urls}")
    val urls: String,
    @Value("\${spring.ldap.base}")
    val base: String,
    @Value("\${spring.profiles.active}")
    val remote: String
)
