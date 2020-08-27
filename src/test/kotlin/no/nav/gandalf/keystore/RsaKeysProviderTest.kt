package no.nav.gandalf.keystore

import com.nimbusds.jose.jwk.RSAKey
import no.nav.gandalf.service.RsaKeysProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.util.AssertionErrors.assertEquals
import org.springframework.test.util.AssertionErrors.assertTrue
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate
import javax.transaction.Transactional

@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("test")
class RsaKeysProviderTest {

    @Autowired
    private val provider: RsaKeysProvider? = null

    @Autowired
    @Qualifier("transactionManager")
    protected var txManager: PlatformTransactionManager? = null

    @Test
    @Transactional
    @Throws(Exception::class)
    fun normalLogicWithInit() {
        val key: RSAKey? = provider?.currentRSAKey
        println(key?.toJSONString())
        assertTrue("Found current key", key != null)
        val jwks = provider?.publicJWKSet
        assertTrue("Found jwks", jwks != null)
        assertEquals("jwks", 2, jwks?.keys?.size)
        for (jwk in jwks!!.keys) {
            println(jwk.toJSONString())
        }
    }

    @Test
    // @Ignore // Denne bør kjøres manuelt, siden verifikasjonen sees i det som logges (istedenfor å lage uønskede innsyns-/get-metoder for å hente ut innholdet i keys.
    @Throws(Exception::class)
    fun concurrentExpiry() {

        // Sett expiry til 1 sekund, initier databasen med en les
        provider!!.setKeyRotationTimeSeconds(1)
        readKeysInTransaction()
        // vent på key expiry
        sleep(2)

        // start et antall tråder som alle skal finne at key'ene skal roteres (pga expiry)
        // Alle trådene skal rotere nøklene, og logge nøkkel-id'er før og etter rotering.
        // Før rotering skal trådene se samme id'er og expiry
        // Etter rotering skal alle ha "previous" id lik det som var "current" før rotering, og "current" id lik det som var "next"
        // Det er viktig at alle trådene ser samme "current" og "previous", siden disse returneres på jwks-tjenesten.
        // Hver tråd vil ha en egen id i "next", og egen expiry, hvor det er den siste tråden som "vinner" og blir værende i DB.
        // DIsse brukes ikke før neste expiry, så det er ikke farlig hvilken verdi som "vinner".
        startThreadsThatReadInSeparateTransaction(3)
        // vent så trådene får gjort seg ferdig
        sleep(2)
    }

    private fun readKeysInTransaction() {
        val tmpl = TransactionTemplate(txManager!!)
        tmpl.execute(
            object : TransactionCallbackWithoutResult() {
                override fun doInTransactionWithoutResult(status: TransactionStatus) {
                    val key: RSAKey = provider!!.currentRSAKey
                    println(key.toJSONString())
                }
            }
        )
    }

    private fun startThreadsThatReadInSeparateTransaction(numberOfThreads: Int) {
        for (i in 0 until numberOfThreads) {
            val t = Thread(Runnable { readKeysInTransaction() })
            t.start()
        }
    }

    private fun sleep(seconds: Int) {
        try {
            Thread.sleep(seconds * 1000.toLong())
        } catch (e: InterruptedException) {
            // ok
        }
    }
}
