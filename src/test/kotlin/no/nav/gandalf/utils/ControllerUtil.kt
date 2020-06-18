package no.nav.gandalf.utils

import no.nav.gandalf.ldap.InMemoryLdap
import org.apache.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

internal const val JWKS = "/jwks"
internal const val WELL_KNOWN = "/.well-known/openid-configuration"
internal const val BASE = "/rest/v1/sts"
internal const val TOKEN = "$BASE/token"
internal const val TOKEN2 = "$BASE/token2"
internal const val SAML_TOKEN = "$BASE/samltoken"
internal const val EXCHANGE = "$BASE/token/exchange"
internal const val EXCHANGE_DIFI = "$BASE/token/exchangedifi"
internal const val SAML_TOKEN_VALIDATE = "$BASE/samltoken/validate"
internal const val OIDC_TOKEN_VALIDATE = "$BASE/token/validate"
internal const val WS_SAMLTOKEN = "$BASE/ws/samltoken"

internal const val GRANT_TYPE = "grant_type"
internal const val SCOPE = "scope"
internal const val TOKEN_TYPE = "Bearer"
internal const val TOKEN_SUBJECT = "token"
internal const val REQUESTED_TOKEN_TYPE = "requested_token_type"
internal const val SUBJECT_TOKEN = "subject_token"
internal const val SUBJECT_TOKEN_TYPE = "subject_token_type"

open class ControllerUtil {

    private val inMemoryLdap = InMemoryLdap()

    fun setupKnownIssuers() {
        endpointStub(HttpStatus.SC_OK, difiOIDCConfigurationUrl, difiOIDCConfigurationResponseFileName)
        endpointStub(HttpStatus.SC_OK, azureADJwksUrl, azureADResponseFileName)
        endpointStub(HttpStatus.SC_OK, openAMJwksUrl, openAMResponseFileName)
        endpointStub(HttpStatus.SC_OK, difiMASKINPORTENCJwksUrl, difiMASKINPORTENJWKSResponseFileName)
        endpointStub(HttpStatus.SC_OK, difiOIDCJwksUrl, difiOIDCResponseFileName)
    }

    fun setupOverride() {
        // Default value has changed in Spring5, need to allow overriding of beans in tests
        System.setProperty("spring.main.allow-bean-definition-overriding", "true")
    }

    fun addUserContext(authenticationManager: AuthenticationManager, username: String, password: String) {
        val token: Authentication =
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken(username, password))
        SecurityContextHolder.getContext().authentication = token
    }

    fun runLdap() {
        inMemoryLdap.start()
    }

    fun stopLdap() {
        inMemoryLdap.stop()
    }
}
