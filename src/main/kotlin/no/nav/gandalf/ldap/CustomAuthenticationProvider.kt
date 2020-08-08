package no.nav.gandalf.ldap

import no.nav.gandalf.model.User
import no.nav.gandalf.util.authenticate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
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
            } else null
        } catch (t: Throwable) {
            throw BadCredentialsException("Authentication failed, ${t.message}")
        }
    }

    fun Authentication.getPw(): String {
        val bytes: ByteArray = this.credentials.toString().toByteArray(StandardCharsets.UTF_8)
        return String(bytes, StandardCharsets.UTF_8)
    }

    override fun supports(authentication: Class<*>): Boolean {
        return authentication == UsernamePasswordAuthenticationToken::class.java
    }
}
