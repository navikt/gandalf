package no.nav.gandalf.ldap

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
    @Autowired val ldapConnectionSetup: LDAPConnectionSetup
) : AuthenticationProvider {
    @Throws(AuthenticationException::class)
    override fun authenticate(authentication: Authentication): Authentication? {
        val name: String = authentication.name
        val password: String = authentication.getPw()
        return try {
            if (authenticate(ldapConnectionSetup, User(name, password))) {
                UsernamePasswordAuthenticationToken(name, password, ArrayList())
            } else {
                null
            }
        } catch (t: Throwable) {
            throw BadCredentialsException("Authentication failed, ${t.message}")
        }
    }

    fun Authentication.getPw() = this.credentials.toString()

    override fun supports(authentication: Class<*>): Boolean {
        return authentication == UsernamePasswordAuthenticationToken::class.java
    }

    fun externalAuth(
        username: String,
        password: String
    ) = try {
        authenticate(ldapConnectionSetup, User(username, password))
        true
    } catch (t: Throwable) {
        throw BadCredentialsException("Authentication failed, ${t.message}")
    }
}
