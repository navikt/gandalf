package no.nav.gandalf.domain

import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.oauth2.sdk.ParseException
import no.nav.gandalf.util.TimestampConverter
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

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

    fun rotateKeys(nextKey: RSAKey, expires: LocalDateTime?) {
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
}
