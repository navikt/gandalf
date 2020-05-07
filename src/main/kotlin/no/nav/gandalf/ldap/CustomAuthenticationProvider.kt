package no.nav.gandalf.ldap

import javax.naming.AuthenticationException
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationProvider : AuthenticationProvider {
    @Throws(AuthenticationException::class)
    override fun authenticate(authentication: Authentication): Authentication? {
        val name: String = authentication.getName()
        val password: String = authentication.getCredentials().toString()
        // return if (shouldAuthenticateAgainstThirdPartySystem()) {
        //     // use the credentials
        //     // and authenticate against the third-party system
        //     UsernamePasswordAuthenticationToken(
        //             name, password, ArrayList())
        // } else {
        //     null
        // }
        // }
        return null
    }

    override fun supports(authentication: Class<*>): Boolean {
        return authentication == UsernamePasswordAuthenticationToken::class.java
    }
}
