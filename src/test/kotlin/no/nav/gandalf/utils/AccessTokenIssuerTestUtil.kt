package no.nav.gandalf.utils

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import org.apache.http.HttpStatus

internal const val openAMResponseFileName = "openam-jwks.json"
internal const val openAMJwksUrl = "/isso/oauth2/connect/jwk_uri"

internal const val difiOIDCJwksUrl = "/idporten-oidc-provider/jwk"
internal const val difiOIDCResponseFileName = "difi-oidc-jwks.json"

internal const val difiOIDCConfigurationUrl = "/idporten-oidc-provider/.well-known/openid-configuration"
internal const val difiOIDCConfigurationResponseFileName = "difi-oidc-configuration.json"

internal const val difiMASKINPORTENCConfigurationUrl = "/.well-known/oauth-authorization-server"
internal const val difiMASKINPORTENCJwksUrl = "/jwk"
internal const val difiMASKINPORTENJWKSResponseFileName = "difi-maskinporten-jwks.json"
internal const val difiMASKINPORTENConfigurationResponseFileName = "difi-maskinporten-configuration.json"

internal const val azureADResponseFileName = "azuread-jwks.json"
internal const val azureADJwksUrl = "/navtestb2c.onmicrosoft.com/discovery/v2.0/keys?p=b2c_1a_idporten_ver1"

internal fun endpointStub(status: Int = HttpStatus.SC_OK, path: String, bodyFile: String) =
    stubFor(
        WireMock.get(WireMock.urlEqualTo(path))
            .willReturn(
                aResponse()
                    .withStatus(status)
                    .withHeader("Content-Type", "application/json; charset=UTF-8")
                    .withBodyFile(bodyFile)
            )
    )

// Original REST-STS did not have token.
internal fun getAzureAdOIDC() = ""

internal fun getMaskinportenToken() =
    "eyJraWQiOiJBTUhVZWpoMnJWLUpCMU4teWJUVjhLNmQ5QlRFUEhEaTBYR29xUGxuNXprIiwiYWxnIjoiUlMyNTYifQ.eyJhdWQiOiJ1bnNwZWNpZmllZCIsInNjb3BlIjoibmF2OnBlbnNqb25cL3YzXC9hbGRlcnNwZW5zam9uIiwiaXNzIjoiaHR0cHM6XC9cL3ZlcjIubWFza2lucG9ydGVuLm5vXC8iLCJjbGllbnRfYW1yIjoidmlya3NvbWhldHNzZXJ0aWZpa2F0IiwidG9rZW5fdHlwZSI6IkJlYXJlciIsImV4cCI6MTU4MTYwMjc5NSwiaWF0IjoxNTgxNjAyNjc1LCJjbGllbnRfaWQiOiI0NDEyMjZmOS0zOTgwLTRjY2MtOGIzOC1kZGQzN2U4MTZkMmEiLCJjbGllbnRfb3Jnbm8iOiI5ODI1ODM0NjIiLCJqdGkiOiJScndoTWFHelU0NmJ3WGpjZjlLX1V2OVMyQUZKMVNCWXpHd2hfUlZETmRBIiwiY29uc3VtZXIiOnsiYXV0aG9yaXR5IjoiaXNvNjUyMy1hY3RvcmlkLXVwaXMiLCJJRCI6IjAxOTI6OTgyNTgzNDYyIn19.w3Y62AGAyXpR7g-jQaKR7R7BaT1zUvfSV-HOlejKaeI2m6WQaN65pr9fXrtbFvIJp7Kbp6ezgpJ4Y6tUiDN1gYkYgMY5IOlDK4cvRqW3UuGkJ8Jt071xrCN-yco1iWm8TZ1lIpOCjuGNPYpJ6Fxp4c_nsIaXhQ1KD2HWAIUqUFUoDOp7Hd8fpxxcdq3O_mOxdXeEi4sS6BKpdMzxdQIRnEI_PEb-dqP4odKO9yAOvPNRhCbxYoBAYYUY2d2BbwLsy032OXvrTR2Y1zkthyeeH0n1eM0aOvxjFK9vuy0gODIpnnCGFE4HpnjwCZX6-sLPFCRuLZ244kpmN9cyU5jc4g"

internal fun getOpenAmOIDC() =
    "eyAidHlwIjogIkpXVCIsICJraWQiOiAiU0gxSWVSU2sxT1VGSDNzd1orRXVVcTE5VHZRPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICJZNXgxcVNLclVVRlE3eVpEVVBIUXZBIiwgInN1YiI6ICJhZ2VudGFkbWluIiwgImF1ZGl0VHJhY2tpbmdJZCI6ICI0NWVkYmYwZS05NmIxLTRkODUtYWFlOC0xMzNmZDVlNmYzOGMtMzA4MDQ1IiwgImlzcyI6ICJodHRwczovL2lzc28tdC5hZGVvLm5vOjQ0My9pc3NvL29hdXRoMiIsICJ0b2tlbk5hbWUiOiAiaWRfdG9rZW4iLCAiYXVkIjogImZyZWctdG9rZW4tcHJvdmlkZXItdDAiLCAiY19oYXNoIjogInY3YW1vYl9lbjc2Tl9VRm5lZDFJaXciLCAib3JnLmZvcmdlcm9jay5vcGVuaWRjb25uZWN0Lm9wcyI6ICI2YjI3OWI0MC1iYjdhLTRjMmItYTQzNC01Y2YyYmNmMzcyMjUiLCAiYXpwIjogImZyZWctdG9rZW4tcHJvdmlkZXItdDAiLCAiYXV0aF90aW1lIjogMTUzOTY4MDMwMCwgInJlYWxtIjogIi8iLCAiZXhwIjogMTUzOTY4MzkwMCwgInRva2VuVHlwZSI6ICJKV1RUb2tlbiIsICJpYXQiOiAxNTM5NjgwMzAwIH0.CEPqay8UMpqVDUB9VMYMWtGgL5NoKY5fqy4-0216OxZ6zSQgUwJ9tqqMXD12yFNIACSOTTRdwahA0qgBDoRZH-u67mnEYXF_pFwhYpOWbwZy3W67B3re8dti8pMoCbFv0ydSifVkyDD44LEEUbnIBV-GvWgcY8E5pASwjycdRJAskCP3m2Bo9GY4XV10lDv59mM9G5wr3kpOn9iiPK0NFxdSizJZjh2qv9KtId9fuC7E-8SZvvUlKjChcIwP-gYiFkqce7KBhYkbwr9eYZ5wSwIY7107xAYVgKoRrjAbEm1okity6yEow0RwoEU-JlUe5Tbvd8mmjrJSHBEgEPiZyw"
