package no.nav.gandalf.keystore

import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import no.nav.gandalf.domain.RSAKeyStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
class RSAKeyStoreRepository {
    var currRSAKeyStore: RSAKeyStore? = null // satt public for testing
    var currPublicJWKSet: JWKSet? = null // satt public for testing

    @Autowired
    private val repository: RSAKeyStoreRepositoryInternal? = null

    // currPublicJWKSet er utdatert, settes til null for å trigge lesing fra DB ved neste kall til getPublicJWKSet
    @get:Throws(Exception::class)
    val currentRSAKey: RSAKey
        get() {
            if (currRSAKeyStore == null || currRSAKeyStore!!.hasExpired()) {
                currRSAKeyStore = repository!!.currentDBKeyUpdateIfNeeded
                currPublicJWKSet = null // currPublicJWKSet er utdatert, settes til null for å trigge lesing fra DB ved neste kall til getPublicJWKSet
            }
            return currRSAKeyStore!!.rSAKey
        }

    // les fra DB
    val publicJWKSet: JWKSet?
        get() {
            if (currPublicJWKSet == null || currRSAKeyStore == null || currRSAKeyStore!!.hasExpired()) {
                currPublicJWKSet = getPublicJWKSet(repository!!.findAllOrdered()) // les fra DB
            }
            return currPublicJWKSet
        }

    fun getPublicJWKSet(keyList: List<RSAKeyStore>? = null): JWKSet {
        val jwkList: MutableList<JWK> = ArrayList()
        if (keyList != null) {
            for (key in keyList) {
                jwkList.add(key.rSAKey)
            }
        }
        return JWKSet(jwkList).toPublicJWKSet()
    }

    fun resetCache() {
        currRSAKeyStore = null
        currPublicJWKSet = null
    }

    fun selfTest(): Boolean {
        publicJWKSet
        return true
    }
}