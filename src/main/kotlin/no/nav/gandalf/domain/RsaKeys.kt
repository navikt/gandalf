package no.nav.gandalf.domain

import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.oauth2.sdk.ParseException
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import no.nav.gandalf.util.TimestampConverter
import java.time.LocalDateTime

@Entity
@Table(name = "RSAKEYS")
class RsaKeys {
    // skal kun finnes 1 record til enhver tid
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    private var id: Long? = null

    @Column(name = "CURRENTKEY", nullable = false, length = 2000)
    private var currentKey: String? = null

    @Column(name = "PREVIOUSKEY", nullable = false, length = 2000)
    private var previousKey: String? = null

    @Column(name = "NEXTKEY", nullable = false, length = 2000)
    private var nextKey: String? = null

    // Brukes bare til test
    @Column(name = "EXPIRES", nullable = false)
    @Convert(converter = TimestampConverter::class)
    var expiry: LocalDateTime? = null

    constructor() {
        // brukes av JPA
    }

    // Trengs bare for initialisering av tom DB, ellers brukes les-rotateKeys-save
    constructor(id: Long?, currentKey: RSAKey, previousKey: RSAKey, nextKey: RSAKey, expires: LocalDateTime?) {
        this.id = id
        this.currentKey = currentKey.toJSONString()
        this.previousKey = previousKey.toJSONString()
        this.nextKey = nextKey.toJSONString()
        expiry = expires
    }

    fun rotateKeys(
        nextKey: RSAKey,
        expires: LocalDateTime?,
    ) {
        previousKey = currentKey
        currentKey = this.nextKey
        this.nextKey = nextKey.toJSONString()
        expiry = expires
    }

    fun getCurrentKey(): RSAKey {
        return getRSAKey(currentKey)
    }

    fun getPreviousKey(): RSAKey {
        return getRSAKey(previousKey)
    }

    // Brukes bare til test
    fun getNextKey(): RSAKey {
        return getRSAKey(nextKey)
    }

    private fun getRSAKey(keyString: String?): RSAKey {
        return try {
            RSAKey.parse(keyString)
        } catch (e: ParseException) {
            throw IllegalArgumentException("Failed to parse key in db")
        }
    }

    fun expired(now: LocalDateTime): Boolean {
        return now.isAfter(expiry)
    }

    // private fun getNewFormat(rsaKey: String?): String? {
    //     if (rsaKey!!.contains("SIGNATURE")) { // old format
    //         val oldJson = JSONObject(rsaKey)
    //         var newKeyStr = "{"
    //         for (key in oldJson.keySet()) {
    //             if (oldJson.get(key)::class.java === JSONObject::class.java) {
    //                 val newJson: JSONObject = oldJson.get(key) as JSONObject
    //                 newKeyStr += "\"$key\":\"" + (if (newJson.has("value")) newJson.get("value") else newJson.get(
    //                     "name"
    //                 )) + "\","
    //             } else if (oldJson.get(key)::class.java === String::class.java) {
    //                 newKeyStr += "\"$key\":\"" + (if (oldJson.get(key)
    //                         .equals("SIGNATURE")
    //                 ) "sig" else oldJson.get(key)) + "\","
    //             }
    //         }
    //         return newKeyStr.substring(0, newKeyStr.length - 1) + "}"
    //     }
    //     return rsaKey
    // }
}
