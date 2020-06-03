package no.nav.gandalf.keystore

import no.nav.gandalf.domain.RSAKeyStore
import no.nav.gandalf.repository.RSAKeyStoreRepositoryImpl
import no.nav.gandalf.service.RSAKeyStoreService
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDateTime

@RunWith(SpringRunner::class)
@SpringBootTest
@TestPropertySource(locations = ["classpath:application-test.properties"])
class RSAKeyStoreServiceTest {

    @Autowired
    private lateinit var rsaKeyStoreService: RSAKeyStoreService

    @Before
    fun init() {
        rsaKeyStoreService.resetRepository()
        rsaKeyStoreService.resetCache()
    }

    @Test
    @Throws(Exception::class)
    fun `Current DBKey With Empty DB`() {
        // DB with no keys
        println(rsaKeyStoreService.findAllOrdered())
        Assert.assertTrue(rsaKeyStoreService.findAllOrdered().isEmpty())
        val rsaKeyStore: RSAKeyStore? = rsaKeyStoreService.currentDBKeyUpdateIfNeeded // should add one new key to DB
        Assert.assertTrue(rsaKeyStore != null)
        Assert.assertTrue(!rsaKeyStore!!.hasExpired())
        Assert.assertTrue(rsaKeyStoreService.findAllOrdered().size == 1)
        val rsaKeyStore2: RSAKeyStore? = rsaKeyStoreService.currentDBKeyUpdateIfNeeded // should not alter DB
        println(rsaKeyStore)
        println(rsaKeyStore2)
        Assert.assertTrue(rsaKeyStoreService.findAllOrdered().size == 1)
        Assert.assertTrue(rsaKeyStore2 != null && rsaKeyStore2.rSAKey == rsaKeyStore.rSAKey)
    }

    @Test
    @Throws(Exception::class)
    fun `Get Current DBKey With Expired Key`() {
        // db with one expired key, 0 outdated keys
        rsaKeyStoreService.addRSAKey(
            LocalDateTime.now().minusSeconds(RSAKeyStoreRepositoryImpl.keyRotationTime + 10)
        ) // expired, but not outdated
        Assert.assertTrue(rsaKeyStoreService.findAllOrdered().size == 1)
        val rsaKeyStore: RSAKeyStore? =
            rsaKeyStoreService.currentDBKeyUpdateIfNeeded // should add one new key to DB and the old key
        Assert.assertTrue(rsaKeyStore != null)
        Assert.assertTrue(!rsaKeyStore!!.hasExpired())
        Assert.assertTrue(rsaKeyStoreService.findAllOrdered().size == 2)
        println(rsaKeyStoreService.findAllOrdered())
        val rsaKeyStore2: RSAKeyStore? = rsaKeyStoreService.currentDBKeyUpdateIfNeeded // should not alter DB
        println(rsaKeyStore2)
        println(rsaKeyStore)
        println(rsaKeyStoreService.findAllOrdered().size)
        Assert.assertTrue(rsaKeyStoreService.findAllOrdered().size == 2)
        Assert.assertTrue(rsaKeyStore2 != null && rsaKeyStore2.rSAKey == rsaKeyStore.rSAKey)
    }

    @Test
    @Throws(Exception::class)
    fun `Get Current DBKey WithExpired Key And Outdated Keys`() {
        // db with 5 expired key, 2 outdated
        rsaKeyStoreService.addRSAKey(
            LocalDateTime.now().minusSeconds(2 * RSAKeyStoreRepositoryImpl.keyRotationTime + 10)
        ) // expired and outdated (more than 2*keyRotationTime)
        rsaKeyStoreService.addRSAKey(
            LocalDateTime.now().minusSeconds(2 * RSAKeyStoreRepositoryImpl.keyRotationTime)
        ) // expired and outdated
        rsaKeyStoreService.addRSAKey(
            LocalDateTime.now().minusSeconds(2 * RSAKeyStoreRepositoryImpl.keyRotationTime - 10)
        ) // expired, but not outdated
        rsaKeyStoreService.addRSAKey(
            LocalDateTime.now().minusSeconds(2 * RSAKeyStoreRepositoryImpl.keyRotationTime + 10)
        ) // expired, but not outdated
        rsaKeyStoreService.addRSAKey(
            LocalDateTime.now().minusSeconds(RSAKeyStoreRepositoryImpl.keyRotationTime + 10)
        ) // expired, but not outdated
        println(rsaKeyStoreService.findAllOrdered().size)
        Assert.assertTrue(rsaKeyStoreService.findAllOrdered().size == 5)
        val rsaKeyStore: RSAKeyStore? =
            rsaKeyStoreService.currentDBKeyUpdateIfNeeded // should add one new key and delete the 2 outdated keys
        Assert.assertTrue(rsaKeyStore != null)
        Assert.assertTrue(!rsaKeyStore!!.hasExpired())
        Assert.assertTrue(rsaKeyStoreService.findAllOrdered().size == 4) // 5+1-2
        val rsaKeyStore2: RSAKeyStore? = rsaKeyStoreService.currentDBKeyUpdateIfNeeded // should not alter DB
        println(rsaKeyStore2)
        println(rsaKeyStore)
        Assert.assertTrue(rsaKeyStoreService.findAllOrdered().size == 4)
        Assert.assertTrue(rsaKeyStore2 != null && rsaKeyStore2.rSAKey == rsaKeyStore.rSAKey)
    }

    @Test
    @Throws(Exception::class)
    fun `Get Current DBKey WithExpired Key And Outdated Keys Check MinNoofKeys`() {
        // db with 3 expired and outdated keys
        rsaKeyStoreService.addRSAKey(
            LocalDateTime.now().minusSeconds(2 * RSAKeyStoreRepositoryImpl.keyRotationTime + 20)
        ) // expired and outdated (more than 2*keyRotationTime)
        rsaKeyStoreService.addRSAKey(
            LocalDateTime.now().minusSeconds(2 * RSAKeyStoreRepositoryImpl.keyRotationTime + 10)
        ) // expired and outdated
        rsaKeyStoreService.addRSAKey(
            LocalDateTime.now().minusSeconds(2 * RSAKeyStoreRepositoryImpl.keyRotationTime + 5)
        ) // expired and outdated
        Assert.assertTrue(rsaKeyStoreService.findAllOrdered().size == 3)
        val rsaKeyStore: RSAKeyStore? =
            rsaKeyStoreService.currentDBKeyUpdateIfNeeded // should add one new key and keep old keys according to minNoofKeys
        Assert.assertTrue(rsaKeyStore != null)
        Assert.assertTrue(!rsaKeyStore!!.hasExpired())
        Assert.assertTrue(rsaKeyStoreService.findAllOrdered().size == RSAKeyStoreRepositoryImpl.minNoofKeys)
        val rsaKeyStore2: RSAKeyStore? = rsaKeyStoreService.currentDBKeyUpdateIfNeeded // should not alter DB
        Assert.assertTrue(rsaKeyStoreService.findAllOrdered().size == RSAKeyStoreRepositoryImpl.minNoofKeys)
        Assert.assertTrue(rsaKeyStore2 != null && rsaKeyStore2.rSAKey == rsaKeyStore.rSAKey)
    }
}
