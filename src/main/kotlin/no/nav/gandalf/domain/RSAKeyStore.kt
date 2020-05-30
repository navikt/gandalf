package no.nav.gandalf.domain

import com.nimbusds.jose.jwk.RSAKey
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import no.nav.gandalf.util.TimestampConverter
import org.jetbrains.annotations.NotNull
import org.json.JSONObject

@Entity
@Table(name = "KEYSTORE")
data class RSAKeyStore(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "ID") var id: Long? = 0,
    @get: NotNull @Column(name = "RSAKEY", length = 2000) var rsaKey: String = "",
    @get: NotNull @Column(name = "EXPIRES") @Convert(converter = TimestampConverter::class) var expires: LocalDateTime = LocalDateTime.now()
) {

    constructor(rsaKey: RSAKey, keyRotationTime: Long) : this() {
        this.rsaKey = rsaKey.toJSONString()
        expires = LocalDateTime.now().plusSeconds(keyRotationTime)
    }

    // old format
    val rSAKey: RSAKey
        get() = try {
            when {
                rsaKey.contains("SIGNATURE") -> { // old format
                    RSAKey.parse(getNewFormat(rsaKey))
                }
                else -> {
                    RSAKey.parse(rsaKey)
                }
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
