package no.nav.gandalf.utils

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jwt.JWTClaimsSet
import org.junit.Assert
import java.text.ParseException

internal fun compare(jwtOriginal: JWTClaimsSet, jwt: JWTClaimsSet) {
    val diffKeys: MutableSet<String> = HashSet(jwt.claims.keys)
    diffKeys.removeAll(jwtOriginal.claims.keys)
    Assert.assertTrue("har forskjellige antall claims", diffKeys.isEmpty())
    for (key in jwt.claims.keys) {
        println("jwt: " + jwt.getClaim(key) + " and org: " + jwtOriginal.getClaim(key))
        Assert.assertTrue(jwt.getClaim(key) == jwtOriginal.getClaim(key))
    }
    Assert.assertTrue("issued oidc token matcher ikke tidligere utstedt token", jwt.toJSONObject() == jwtOriginal.toJSONObject())
}

internal fun getOriginalToken() = "eyJraWQiOiIxMGU5ZWQxNi1lYjg3LTQ5NGEtYTRmZi0zNTE2NTFkNGI5OGUiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJzcnZzZWN1cml0eS10b2tlbi0iLCJhdWQiOlsic3J2c2VjdXJpdHktdG9rZW4tIiwicHJlcHJvZC5sb2NhbCJdLCJ2ZXIiOiIxLjAiLCJuYmYiOjE1Mzk2OTE3NDUsImF6cCI6InNydnNlY3VyaXR5LXRva2VuLSIsImlkZW50VHlwZSI6IlN5c3RlbXJlc3N1cnMiLCJhdXRoX3RpbWUiOjE1Mzk2OTE3NDUsImlzcyI6Imh0dHBzOlwvXC9zZWN1cml0eS10b2tlbi1zZXJ2aWNlLm5haXMucHJlcHJvZC5sb2NhbCIsImV4cCI6MTUzOTY5NTM0NSwiaWF0IjoxNTM5NjkxNzQ1LCJqdGkiOiIxNTE4YTc4MS1iZjQyLTQ1NzYtOThlYS0zZWYzZjU0NTkyYzkifQ.f9fs2DLedwY-deAg7kq8yxYbd-26C55Keyg4CsxI4k98DWic7-VUnKrIKRc907cXgds_vYiEjFzJ0S240isWIInzCs9-j4XTFnK14O_KW7UNCbBuapmUEDxHGx9qVlEkYKuR5tDM29Uer9ESD1cR0xtreVfeOzcFOCVhFBfUAuWADMl-ACssozanTo4OuOjm6osB_lBR70SsQdHFpcBRUhCKVCyCmjhWmsJo7Ne-i_0z7ndQrg4E7EgGvralgJJiWYnX3Icp801oP3l9v0wjZEjk2ddofJnEOV2OJzovoK2jJkFviPRTWGjfB03JP3U-8WcNZXj6V8iUlesVBzbRsg"

internal fun getAlteredOriginalToken() = "eyJraWQiOiIxMGU5ZWQxNi1lYjg3LTQ5NGEtYTRmZi0zNTE2NTFkNGI5OGUiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJzcnZzZWN1cml0eS10b2tlbi0iLCJhdWQiOlsic3J2c2VjdXJpdHktdG9rZW4tIiwicHJlcHJvZC5sb2NhbCJdLCJ2ZXIiOiIxLjAiLCJuYmYiOjE1Mzk2OTE3NDUsImF6cCI6InNydnNlY3VyaXR5LXRva2VuLSIsImlkZW50VHlwZSI6IlN5c3RlbXJlc3N1cnMiLCJhdXRoX3RpbWUiOjE1Mzk2OTE3NDUsImlzcyI6Imh0dHBzOlwvXC9zZWN1cml0eS10b2tlbi1zZXJ2aWNlLm5haXMucHJlcHJvZC5sb2NhbCIsImV4cCI6MTUzOTY5NTM0NSwiaWF0IjoxNTM5NjkxNzQ1LCJqdGkiOiIxNTE4YTc4MS1iZjQyLTQ1NzYtOThlYS0zZWYzZjU0NTkyYzkifQ.f9fs2DLedwY-deAg7kq8yxYbd-26C55Keyg4CsxI4k98DWic7-VUnKrIKRc907cXgds_vYiEjFzJ0S240isWIInzCs9-j4XTFnK14O_KW7UNCbBuapmUEDxHGx9qVlEkYKuR5tDM29Uer9ESD1cR0xtreVfeOzcFOCVhFBfUAuWADMl-ACssozanTo4OuOjm6osB_lBR70SsQdHFpcBRUhCKVCyCmjhWmsJo7Ne-i_0z7ndQrg4E7EgGvralgJJiWYnX3Icp801oP3l9v0wjZEjk2ddofJnEOV2OJzovoK2jJkFviPRTWGjfB03JP3U-8WcNZXj6V8iUlesVBzbRsK"

@Throws(ParseException::class)
internal fun getOriginalJwkSet(): JWKSet {
    val jwks = """{
  "keys": [
    {
      "kty": "RSA",
      "e": "AQAB",
      "use": "sig",
      "kid": "10e9ed16-eb87-494a-a4ff-351651d4b98e",
      "alg": "RS256",
      "n": "qQCLsc-CqGosvj3nSjRsvKEt5gk6MNXVRsKBtjysjKozR4dZw-uWpxMfGC1M6PRJuI-67d6wmZp3W7ydKT1ONESQ_mT4RnCMw7x9FSCI6JVIpg5YqVaCQeVFc16rcjL9sT55CGSWLkYYNZ-nZDPe54sYQ9pp8bQak_kE7gG31q07z95I6q--u774AGgv-aRMHAgK3x1zmd9fA49FnviUxiZ8bGMNJ16jGaFlOLFxOEUAfQhgfT2uuaWJz232DKd6Hx0MgKyD4VOhOfz0JUuATA_zD9ujQUIAb7FJLJ9UjZX37Cjazb82uX4xZ3MyYRlL0hXyDI4Dlf_TzjFu4vL7TQ"
    },
    {
      "kty": "RSA",
      "e": "AQAB",
      "use": "sig",
      "kid": "211146e0-17a1-4acd-b9c9-2535dc14a88d",
      "alg": "RS256",
      "n": "kWGpFHfV0eEh7OxYOs4xYzFqD0QuxUqoG2y9jHU4u3fA76ryCNg6SE1y-O5F7ebo3pEYXRxrLbqPJdeM3XOb8_S4UvWRfnhFaybDo4OCKcuyA8wH-crhoqObUWOitcx9GnSyW-JuZASm_8_FhO0qu8laMQPxfAYhXQwRlyfCtCssUdc0yFgoPci87PFXhvmhmzWp_7aqrfXZiNrQSsfrjCeIqldopDkIN4JF47Yb_wtrp3e6y0y-RhcV1qXXa5N7SpXS7yjzhp-RMjyCmYyH7zOpKaE58KxuMDlql8BNGB2dlfirnU1R4fQkktNmvR8fPMb4DKCeNJkMo9n_Uil-Jw"
    }
  ]
}"""
    return JWKSet.parse(jwks)
}

internal fun getDifiOidcToken() = "eyJraWQiOiJtcVQ1QTNMT1NJSGJwS3JzY2IzRUhHcnItV0lGUmZMZGFxWl81SjlHUjlzIiwiYWxnIjoiUlMyNTYifQ.eyJhdWQiOiJvaWRjX25hdl9wb3J0YWxfdGVzdGtvbnN1bWVudCIsInNjb3BlIjoibmF2OnRlc3RhcGkiLCJpc3MiOiJodHRwczpcL1wvb2lkYy12ZXIyLmRpZmkubm9cL2lkcG9ydGVuLW9pZGMtcHJvdmlkZXJcLyIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJleHAiOjE1NDQwODc0NjYsImlhdCI6MTU0NDA4NzM0NiwiY2xpZW50X29yZ25vIjoiODg5NjQwNzgyIiwianRpIjoibHFSQkRsYTNhbDFHUWRHcHpLaHE0MDNRbFVHMkdnYjB2RzFIMnFFS1ZDST0ifQ.NKFNsdO1zQTwDw_dmFBTKfNpqC7BiqFpQp4e6mTlTTP7r4efo7qPjlkgTFWl_v5RiSD8esBkCKgeeZrDQ1PAAM8VIja9H4vyNXBYwOvbzdxLdCgzwKF1kmkx6l0Cgw3GC8HFHX1UPJ7-rpyLst8V857m6QtC6FqjkIDcv4F249PwhLWhfmGUqwGpRZ3frAJ0SYljehCN-A3qWXU_xaibTmzLtRK56VlzxB2sY9kdKmWIW8yppSZKeroNNeFEsU7WW90jxR9d9EFvPgweSZrKWk3iAS2HUA-pxkctSb64nUAOVyB3ywCMftq0e_TvGboxYB4DcY0cP5ReZlE0bzZftg"