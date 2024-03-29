package no.nav.gandalf.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
data class LdapConfig(
    @Value("\${spring.ldap.url}")
    val url: String,
    @Value("\${spring.ldap.base}")
    val base: String,
    @Value("\${spring.profiles.active}")
    val remote: String,
    @Value("\${spring.ldap.port}")
    val port: Int,
    @Value("\${spring.ldap.timeout: 5000}")
    val timeout: Int,
    @Value("\${srvtest.password: password}")
    var srvTestPassword: String,
    @Value("\${srvtest.username: srvPDP}")
    var srvTestUsername: String,
) {
    override fun toString() = "Host: $url, Port: $port, Timeout: $timeout, Base: $base"
}
