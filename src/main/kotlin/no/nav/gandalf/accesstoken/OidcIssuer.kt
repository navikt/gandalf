package no.nav.gandalf.accesstoken

import com.nimbusds.jose.jwk.RSAKey

interface OidcIssuer {
    val issuer: String
    fun getKeyByKeyId(keyId: String?): RSAKey?
}
