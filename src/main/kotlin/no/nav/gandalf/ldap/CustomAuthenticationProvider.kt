package no.nav.gandalf.ldap

import no.nav.gandalf.api.initAD
import no.nav.gandalf.config.LdapConfig
import no.nav.gandalf.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import javax.naming.AuthenticationException

@Component
class CustomAuthenticationProvider(
    @Autowired val ldapConfig: LdapConfig
) : AuthenticationProvider {

    @Throws(AuthenticationException::class)
    override fun authenticate(authentication: Authentication): Authentication? {
        val name: String = authentication.name
        val password: String = authentication.credentials.toString()
        return try {
            LDAPAuthentication(initAD(ldapConfig)).result(User(name, password))
            UsernamePasswordAuthenticationToken(name, password, ArrayList())
        } catch (t: Throwable) {
            throw BadCredentialsException("Authentication failed, ${t.message}")
        }
    }

    override fun supports(authentication: Class<*>): Boolean {
        return authentication == UsernamePasswordAuthenticationToken::class.java
    }
}
