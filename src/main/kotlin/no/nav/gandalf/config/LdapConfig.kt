package no.nav.gandalf.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
data class LdapConfig(
    @Value("\${spring.ldap.url}")
    val url: String,
    @Value("\${spring.ldap.base}")
    val base: String,
    @Value("\${spring.ldap.ou}")
    val ou: String,
    @Value("\${spring.profiles.active}")
    val remote: String,
    @Value("\${spring.ldap.port}")
    val port: Int
)
