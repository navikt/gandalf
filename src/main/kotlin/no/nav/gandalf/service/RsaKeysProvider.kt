package no.nav.gandalf.service

import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import no.nav.gandalf.domain.RsaKeys
import no.nav.gandalf.repository.RsaKeysRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class RsaKeysProvider {
    private var rsaKeys: RsaKeys? = null

    @Autowired
    private val repository: RsaKeysRepository? = null

    fun setKeyRotationTimeSeconds(keyRotationTimeSeconds: Long) {
        repository!!.setKeyRotationTimeSeconds(keyRotationTimeSeconds)
    }

    val currentRSAKey: RSAKey
        get() = keys.getCurrentKey()

    val publicJWKSet: JWKSet
        get() {
            val keys = keys
            val jwkList: MutableList<JWK> = ArrayList()
            jwkList.add(keys.getCurrentKey())
            jwkList.add(keys.getPreviousKey())
            return JWKSet(jwkList).toPublicJWKSet()
        }

    private val keys: RsaKeys
        get() {
            if (rsaKeys == null || rsaKeys!!.expired(LocalDateTime.now())) {
                rsaKeys = repository!!.keys
            }
            return rsaKeys!!
        }

    fun selfTest(): Boolean {
        publicJWKSet
        return true
    }
}
