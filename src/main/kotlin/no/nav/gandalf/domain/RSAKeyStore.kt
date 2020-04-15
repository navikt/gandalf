package no.nav.gandalf.domain

import com.nimbusds.jose.jwk.RSAKey
import org.json.JSONObject
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table

@Entity
@Table(name = "KEYSTORE")
class RSAKeyStore {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @SequenceGenerator(name = "keyStoreIdGenerator", sequenceName = "KEYSTORE_ID_SEQ", allocationSize = 1, initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "keyStoreIdGenerator")
    private val id: Long? = null

    @Column(name = "RSAKEY", nullable = false, length = 2000)
    private var rsaKey: String? = null

    @Column(name = "EXPIRES", nullable = false)
    @Convert(converter = TimestampConverter::class)
    var expires: LocalDateTime? = null

    constructor() {
        // brukes av JPA
    }

    constructor(rsaKey: RSAKey, keyRotationTime: Long) {
        this.rsaKey = rsaKey.toJSONString()
        expires = LocalDateTime.now().plusSeconds(keyRotationTime)
    }

    // old format
    val rSAKey: RSAKey
        get() = try {
            if (rsaKey!!.contains("SIGNATURE")) { // old format
                RSAKey.parse(getNewFormat(rsaKey))
            } else {
                RSAKey.parse(rsaKey)
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse key in db")
        }

    fun hasExpired(): Boolean {
        val now = LocalDateTime.now()
        return now.isAfter(expires)
    }

    companion object {
        fun getNewFormat(rsaKey: String?): String? {
            if (rsaKey!!.contains("SIGNATURE")) { // old format
                val oldJson = JSONObject(rsaKey)
                var newKeyStr = "{"
                for (k in oldJson.keySet()) {
                    if (oldJson.get(k).javaClass == JSONObject::class.java) {
                        val newJson: JSONObject = oldJson.get(k) as JSONObject
                        newKeyStr += "\"" + k + "\":\"" + (if (newJson.has("value")) newJson.get("value") else newJson.get("name")) + "\","
                    } else if (oldJson.get(k).javaClass == String::class.java) {
                        newKeyStr += "\"" + k + "\":\"" + (if (oldJson.get(k) == "SIGNATURE") "sig" else oldJson.get(k)) + "\","
                    }
                }
                return newKeyStr.substring(0, newKeyStr.length - 1) + "}"
            }
            return rsaKey
        }
    }
}