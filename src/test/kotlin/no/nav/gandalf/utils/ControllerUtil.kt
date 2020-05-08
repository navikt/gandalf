package no.nav.gandalf.utils

import org.apache.http.HttpStatus

internal const val JWKS = "/jwks"
internal const val WELL_KNOWN = "/.well-known/openid-configuration"
internal const val BASE = "/v1/sts"
internal const val TOKEN = "$BASE/token"
internal const val TOKEN2 = "$BASE/token2"
internal const val SAML_TOKEN = "$BASE/samltoken"
internal const val PORT = 8888
internal const val GRANT_TYPE = "grant_type"
internal const val SCOPE = "scope"
internal const val TOKEN_TYPE = "Bearer"

class ControllerUtil {

    fun setupKnownIssuers() {
        jwksEndpointStub(HttpStatus.SC_OK, difiOIDCConfigurationUrl, difiOIDCConfigurationResponseFileName)
        jwksEndpointStub(HttpStatus.SC_OK, azureADJwksUrl, azureADResponseFileName)
        jwksEndpointStub(HttpStatus.SC_OK, openAMJwksUrl, openAMResponseFileName)
        jwksEndpointStub(HttpStatus.SC_OK, difiMASKINPORTENCJwksUrl, difiMASKINPORTENConfigurationResponseFileName)
        jwksEndpointStub(HttpStatus.SC_OK, difiOIDCJwksUrl, difiOIDCResponseFileName)
    }
}