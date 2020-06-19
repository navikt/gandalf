package no.nav.gandalf.keystore

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.domain.RSAKeyStore
import no.nav.gandalf.repository.RSAKeyStoreRepositoryImpl
import org.json.JSONException
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.security.NoSuchAlgorithmException

@RunWith(SpringRunner::class)
@SpringBootTest
@DirtiesContext
@ActiveProfiles("test")
class RSAKeyStoreRepositoryImplTest {

    @Autowired
    private lateinit var rsaKeyStoreRepositoryImpl: RSAKeyStoreRepositoryImpl

    @Test
    @Throws(NoSuchAlgorithmException::class, JOSEException::class)
    fun `Add New RSAKey`() {
        val noofKeys: Int = rsaKeyStoreRepositoryImpl.findAllOrdered().size
        val rsaKeyStore: RSAKeyStore = rsaKeyStoreRepositoryImpl.addNewRSAKey()
        val rsaKey: RSAKey = rsaKeyStore.rSAKey
        Assert.assertTrue(rsaKey.algorithm.name == AccessTokenIssuer.OIDC_SIGNINGALG.name)
        Assert.assertTrue(rsaKey.keyUse == KeyUse.SIGNATURE)
        Assert.assertTrue(rsaKeyStoreRepositoryImpl.findAllOrdered().size == noofKeys + 1)
    }

    @Test
    @Throws(JSONException::class, NoSuchAlgorithmException::class)
    fun `Read Old And New Key`() {
        val oldKey = JWKSet((RSAKey.parse(RSAKeyStore.getNewFormat(getOldDBKey()))))
        val newKey = JWKSet((RSAKey.parse(RSAKeyStore.getNewFormat(getNewDBKey()))))
        println(oldKey.toJSONObject())
        println(newKey.toJSONObject())
    }

    private fun getOldDBKey(): String? {
        return "{\"n\":{\"value\":\"w47g4_TTEzd4F3XoAfd_UTA_6Aj2xb01CrY0_vt3UdoEG1ACzPeuJjMtjN1il-8wAwDhWmaFxO_4D5GRURs446G3ho4fAb6v2_G1INnhhiwZC87Ktcct2Kz3i3x5VaxKtBpL9KjXDm5WfDCV-NeX8XB3TP4pS8_TtHiwlpBHyfOZFH0yc468WZ3XTPmo7bMl-d-f5bgoOoXYwW1l3t2Zmka7HeZhOswVhrA0pFJVwYjyhxLu11AgThfZf3WRyeO-Nw5zwzpB8xU6ZM50qS257Mv4umxYu22f0jc9iPEqHoX_Mh1DKKtoh9MLgdAkvLZTI-ujbIVnOhPsL6n4rHmCzQ\"},\"e\":{\"value\":\"AQAB\"},\"d\":{\"value\":\"gIk1ACA3bHYVFTaGXGSU5oRUPOtHaAlJB9VjWAVpE_-8m0cn0Bkr-ifBVMleuIaahB7cAFNKsXsZDQKdBTZjWhs9Sc-4SalQZbylVQZg2ZO6kqxFSN22IKjvvFsAVXN9VovCSPmBARc8-TvmFz19vVAy2gRkmFPtzr1VNGF5Dh9MvAU1YLe_lDGX118Q_20gXvusM04SYfxlCvL_NZoFOik7F9T7hErs7AG9MvOhvCbflAyFowtkocHvorie2HjPdsx2pKZo7wElZaZuGqj255e2-Qk8fha0kxrUaunDueVmyvq9p31IBNEPyDfab5BIHP217BErg5Td4AWSRAgG8Q\"},\"p\":{\"value\":\"55nTH4DKLi6S1U6l-ZfSt9WnngFkRwj0zZAhehmL9pzvzaGLmnQpT2eHGHVmOU8fT8d1BnZGmiKV-R9XlIFNscJP1OJRW-H35apJ-Ut_nGmIsZftj4BrwqcDkde0k59c6o5ZY9l1j97N4DIGOIjNnildUp8f6jQbCj4sBSMn0js\"},\"q\":{\"value\":\"2Cj_BtAsRUKL5iaaYWVvRurJF6Y4yxvw_jeMYK135Icba0fRcymVemoZAmL6WK-xbbg_Ns4k72CxlBZgBL3Ovr910odCcCxkdEf2KSf2CjTlGhsyLwy6Y8E00IdKPbXmifOzd0UpQAgsPydUwj-vCcQDIyxk_dzeRew931IbZpc\"},\"dp\":{\"value\":\"pFq5RGX1FIjgkpdZmaJwWx038h0yuGZWs3pRB4ERnlUIqHi3xUgh-MOTT3wgqxLHOapmFcvhlohsvXnMgQqxxDAMzxo8emQFs-oAzPUS_kC2TLFwWKHd1ykGk3zsHMa-_YU_UmiD0TFgEHXvjDcpE9Bu9a8OyvlnJwGv44VrQPk\"},\"dq\":{\"value\":\"0tcKCec7h0LlmlPE-YMrCw0voDWt588VPaGI9zO8Yde20ul2TtIczw513nIUpjSY5-BzKE3ikOrLyxoYBgfthPjTP8pW73sdcJ5QaMxlnIcDgHdOd73-gBaQIWYU8CRg_eYLAycemSpfnioK7n2Xifr65HAFAelRlPpdT1qTB3k\"},\"qi\":{\"value\":\"0DjHBJds4KNrK9LL89G2e8zgkSSqi0qoc92ZF_nbsVF50TBaWoALWMtxzvY2lGiyOXVFYS9hTJ3p9dLt2iwc5Ha_UT03M2S8xgaVtBwKEoNqXRnT1DkObvKNMHiPsThB0Y9OIaL-bCniVMuHkAetzLo2CN3JKdhugZEf919hETc\"},\"oth\":[],\"kty\":{\"value\":\"RSA\",\"requirement\":\"REQUIRED\"},\"use\":\"SIGNATURE\",\"alg\":{\"name\":\"RS256\",\"requirement\":\"RECOMMENDED\"},\"kid\":\"7ac9ed8b-7daf-4af8-b663-04e549d576ca\"}"
    }

    private fun getNewDBKey(): String? {
        return "{\"p\":\"zetpYh5JPqje7_ySwxPaiw5wgO8htn1Ul8lKQxsbs_zOdaQ5sjGcnCxXJZyGKNn0HvsyXV4hoCPg_V9pGuedpgiuJCdOR1lD1Q4vv1u1YZ9v-uPvUbP6g7ZpwP8Eq4hxmmEX4ggrcWN8ywHpjGQOeYZiAe0OT0bt2SqDOELCAFc\",\"kty\":\"RSA\",\"q\":\"sVv9wFSu99nyTvDc6LLGdykflHYoWQiVFxNoANvK81xDX9TPFEcaDGkvz-Ql7RcR7hqElyoauwAEnG1eo9E8xjK20y1SZTUKISBMFUtLCzvH1LOZJ9DjFy5BfS7ZJDlice3RNRwE8Lwnar7cdpkejoCLUeXIRC0OT1NBWFCOt_c\",\"d\":\"R3y5ldnjx0MP3rQ7zjt_4xyyfx85_Km7L_wUKDM9LdgFJLl-Hxij4YPAzzIY8gXqebFJU1edSylGbuy_HKKFwzi375J_KK_pCBPEQb4CHOQ7BjAqpGxhVfbTomVZ9_cdlGJs2NcCEm-pDUndvnqyeBCePKXVxahuvihdB0x1bbC3nt8_ZbY2-dG0c7AyShHzndogonFjGOplRKngNL5nXeSzXJ6-E0-QquCS-kZsvEAZCszSNbFirLTO01BZ13uNpts6wouDS-ijG4rJawEMGOQodcUdQhZY0mfv7_MfxWEGUwnC8ibRjveaZArTodkgh57Oz3TODFgstXwUkpKOIQ\",\"e\":\"AQAB\",\"use\":\"sig\",\"kid\":\"7932ad8e-c20d-405c-a343-efa14b0cbbe6\",\"qi\":\"M0bEH-EOqiyTHenwLIVPazkC8D2fGiedr5g_MZFMACV5ywxMWC6XWjGyIBKOzCFr9_ctU6el-U8knIzf9DoDFoRWsZu-ROI8465A6yvcLPWAPoES61f3iKnR62LtfHdAV01k_26zaUw37pPyE5KmLQHLmz2HGmJj6fRG04sX_T8\",\"dp\":\"FydWNSMkpzgDwUZMFkVAOIyKF-VLjCJxhZOrlvoQVl-hs1ag6ilr65-MuPztlOBU87rZAeiYrVH7dWHqBo1ui4HEWupUge9GkklDEygzr-NmivZgLXaoP3EeWQYTt1njNDGgpti2UcyW9pijcxa5De8PTBoklsNp8RY3pPbUq4c\",\"alg\":\"RS256\",\"dq\":\"W0dt7EtpFVRM-cXK29QOgotenmjFUTUyjgLF1xNHe0IzxqFexh_lzxdfrQ3NWx0xdCqfrLDVuwJCXUOInqT0M0ksgLjlnALVKMFXhuoOQETPDrA0FaLTDT4YJFY1GQiTmvD7Z-r4u_EKFE0QZ9VcPZj4mvi4viW2JKtDiTFEknc\",\"n\":\"jqnCp6ikZdRmyba-Oe4GWaa9bRYVy4T06asYCwiV7dSeYGcLupSSYfgpDs18shmKoTzT0huxsZk8_hdu5GLNOA1uwkeVMnHDcglzWiHUriq3zXLRE5MIz7t5vfcduemXsawRrJahGEXPtQ_9ElaRM9kCiRsFo9BDpyA0Qe3jpEFl9NNSIhDx-W_iPFySeWTvYcPVQoGrUuFf5zqg_87c84MYRhmiYR81uwRarXWyjkdC_TB6MhQCkeawQGUQin9Jw5ocqyyJkNRoln0d2WkAPDHFsPVSWU1R3z_PTAL6SuiIMVnjqPk4kpXgiABhgVwyQKuFs0-Ka11z3q-gd66E8Q\"}"
    }
}
