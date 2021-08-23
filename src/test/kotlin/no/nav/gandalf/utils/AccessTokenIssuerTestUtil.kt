package no.nav.gandalf.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import org.apache.http.HttpStatus
import java.nio.file.Files
import java.nio.file.Path

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
internal const val azureADJwksUrl = "/discovery/v2.0/keys"

private val objectMapper: ObjectMapper = jacksonObjectMapper()

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

internal fun wellKnownStub(path: String, jwksUrl: String, bodyFile: String) {
    val content = Files.readString(Path.of("src/test/resources/__files/$bodyFile"))
    val jsonNode: JsonNode = objectMapper.readValue<JsonNode>(content).apply {
        (this as ObjectNode).put("jwks_uri", jwksUrl)
    }
    endpointStubWithBody(path = path, body = jsonNode)
}

internal fun endpointStubWithBody(status: Int = HttpStatus.SC_OK, path: String, body: Any) =
    stubFor(
        WireMock.get(WireMock.urlEqualTo(path))
            .willReturn(
                aResponse()
                    .withStatus(status)
                    .withHeader("Content-Type", "application/json; charset=UTF-8")
                    .withBody(objectMapper.writeValueAsString(body))
            )
    )

// Original REST-STS did not have token.
internal fun getAzureAdOIDC() =
    "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IkN0VHVoTUptRDVNN0RMZHpEMnYyeDNRS1NSWSJ9.eyJhdWQiOiIxNWYwMWZlZS1iZDZkLTQ0MjctYmI3MC1mYzllNzVjYWExM2EiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vOTY2YWM1NzItZjViNy00YmJlLWFhODgtYzc2NDE5YzBmODUxL3YyLjAiLCJpYXQiOjE1OTEwOTQ2NjMsIm5iZiI6MTU5MTA5NDY2MywiZXhwIjoxNTkxMDk4NTYzLCJncm91cHMiOlsiMjhlNjZlNzctZDg3OS00N2YxLTg4MzUtZmVhNTA1Y2ZjZTlmIiwiNTA0NDUzNWQtMjQ1ZC00OWVmLTlhNzgtMmVmZWE2N2JhMGJjIiwiZDI5ODcxMDQtNjNiMi00MTEwLTgzYWMtMjBmZjZhZmUyNGEyIiwiZmVjNjYyOTItM2RkYi00OWJhLWEwY2YtY2FlZjQ2M2I3ZDYzIiwiYTlmNWVmODEtNGU4MS00MmU4LWIzNjgtMDI3MzA3MWI2NGI5IiwiMmZmZTMyNjItNmMwZS00NGYzLTk0ZjAtMzRkZmE1NjU5YTA0IiwiZjgxMTM4MTYtYjI0NC00MWU0LWFmMjktMzA4YjIzMTdhYTMzIiwiYTVjMjM3MGUtNmIzZC00YzJjLTlhNWUtMjM4MDA4NTI2NTc0IiwiZGU0NDA1MmQtYjA2Mi00NDk3LTg5YTItMGM4NWI5MzViODA4IiwiNjkwZmNjMzMtMmM0Yi00YWEzLTk4ZWUtN2MxNmRjYTI3NmZiIiwiOTI4NjM2ZjQtZmQwZC00MTQ5LTk3OGUtYTZmYjY4YmIxOWRlIiwiMTRlZDI2MmEtOGRjMC00ZWIxLTg1NDEtMTg1MzE1ZDBhYmJhIiwiYTNmOTE0OTMtMWFiOC00YjY0LWE1NDQtMGE3N2NiYmE5MjQwIiwiNDJmMzEzYjgtZDg4OC00MzQ0LTg2ZDYtYWRjYWVhNGVkMzgxIiwiMmRlYjMzNDEtMDUyYi00OGJhLTk1ZWEtMmIyMzUyYjQwZjYxIiwiYjFlMDQ0NjgtYTUzYS00OGZiLWEzZjctOTk2YzA2YzhjMTYzIiwiYzU3Yzg2ODgtYzY4MC00NWZiLWJlMjQtZTdkZGM0YzY5YjJkIiwiOTBmYzQ1MzUtYWI3NC00NDRlLTg5NjktMTkzNWI5NTYzZTAwIiwiOGJiOWI4ZDEtZjQ2YS00YWRlLThlZTgtNTg5NWVjY2RmOGNmIiwiN2M5MzhmMTgtMGY5OC00ZDM5LWFjNmYtNjBmNmZhNjlmMzNkIiwiOTI0YmFkY2QtYjkzNi00NGY0LWI3YmYtOTdjMDNkZTA4OTNhIiwiMTc0YmVjMjctZTk1NC00NTNiLTg0ODYtNGE4MGQ5ZmM3NjM2Il0sIm5hbWUiOiJGX1o5OTA1NDMgRV9aOTkwNTQzIiwibm9uY2UiOiJkY09KcW13bHZIUDVHWFNWZ2wzemJPcHF6ZmMxYzZjODl6Qm5SNXFSRnlZIiwib2lkIjoiODVjNTk1OWItZGU3OS00NDBkLTgwOTctNDE3YzczZTIxOTYyIiwicHJlZmVycmVkX3VzZXJuYW1lIjoiejk5MDU0M0B0cnlnZGVldGF0ZW4ubm8iLCJzdWIiOiI2REpDcXFmV1phT2JpRllhXzlNOHhULU9VUkhqVk9LNHhrWnA4M1FwTjJrIiwidGlkIjoiOTY2YWM1NzItZjViNy00YmJlLWFhODgtYzc2NDE5YzBmODUxIiwidXRpIjoiaGI1VDE2MUh2VW1ncmZHV0lSNG1BQSIsInZlciI6IjIuMCJ9.phQbYVXP5zuv7iaBUXVQ1-89498NWqS9UMTM7ZFH1Qafl3-eTU8AW1fw-0hhA5JOarnj50xiE0nxjcohbKoVMO7zUQUfeGbl8AcRtDjhTScfv_PMd31fgyz8-NV6b83sedHfuDNlLIpahnqDWiFAO1fWLZ_IFxMgO11okeS98fKXW1b3FiyERbY7RsGkrokZN2srDAthY0t3ZPgMu3fcSfWmSW4SGkQVM4KloNocdgX8-6AV4qXMyZO9uBX8N_EJZscXgmVhg999M-tOY0bhv7WAKFvcK-anOUEzTgSTpF-ovnu6YBpl5695sASgqLHYHYc1fdDuNHXEP_Oxz69gvw"

internal fun getMaskinportenToken() =
    "eyJraWQiOiJBTUhVZWpoMnJWLUpCMU4teWJUVjhLNmQ5QlRFUEhEaTBYR29xUGxuNXprIiwiYWxnIjoiUlMyNTYifQ.eyJhdWQiOiJ1bnNwZWNpZmllZCIsInNjb3BlIjoibmF2OnBlbnNqb25cL3YzXC9hbGRlcnNwZW5zam9uIiwiaXNzIjoiaHR0cHM6XC9cL3ZlcjIubWFza2lucG9ydGVuLm5vXC8iLCJjbGllbnRfYW1yIjoidmlya3NvbWhldHNzZXJ0aWZpa2F0IiwidG9rZW5fdHlwZSI6IkJlYXJlciIsImV4cCI6MTU4MTYwMjc5NSwiaWF0IjoxNTgxNjAyNjc1LCJjbGllbnRfaWQiOiI0NDEyMjZmOS0zOTgwLTRjY2MtOGIzOC1kZGQzN2U4MTZkMmEiLCJjbGllbnRfb3Jnbm8iOiI5ODI1ODM0NjIiLCJqdGkiOiJScndoTWFHelU0NmJ3WGpjZjlLX1V2OVMyQUZKMVNCWXpHd2hfUlZETmRBIiwiY29uc3VtZXIiOnsiYXV0aG9yaXR5IjoiaXNvNjUyMy1hY3RvcmlkLXVwaXMiLCJJRCI6IjAxOTI6OTgyNTgzNDYyIn19.w3Y62AGAyXpR7g-jQaKR7R7BaT1zUvfSV-HOlejKaeI2m6WQaN65pr9fXrtbFvIJp7Kbp6ezgpJ4Y6tUiDN1gYkYgMY5IOlDK4cvRqW3UuGkJ8Jt071xrCN-yco1iWm8TZ1lIpOCjuGNPYpJ6Fxp4c_nsIaXhQ1KD2HWAIUqUFUoDOp7Hd8fpxxcdq3O_mOxdXeEi4sS6BKpdMzxdQIRnEI_PEb-dqP4odKO9yAOvPNRhCbxYoBAYYUY2d2BbwLsy032OXvrTR2Y1zkthyeeH0n1eM0aOvxjFK9vuy0gODIpnnCGFE4HpnjwCZX6-sLPFCRuLZ244kpmN9cyU5jc4g"

internal fun getOpenAmOIDC() =
    "eyAidHlwIjogIkpXVCIsICJraWQiOiAiU0gxSWVSU2sxT1VGSDNzd1orRXVVcTE5VHZRPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICJZNXgxcVNLclVVRlE3eVpEVVBIUXZBIiwgInN1YiI6ICJhZ2VudGFkbWluIiwgImF1ZGl0VHJhY2tpbmdJZCI6ICI0NWVkYmYwZS05NmIxLTRkODUtYWFlOC0xMzNmZDVlNmYzOGMtMzA4MDQ1IiwgImlzcyI6ICJodHRwczovL2lzc28tdC5hZGVvLm5vOjQ0My9pc3NvL29hdXRoMiIsICJ0b2tlbk5hbWUiOiAiaWRfdG9rZW4iLCAiYXVkIjogImZyZWctdG9rZW4tcHJvdmlkZXItdDAiLCAiY19oYXNoIjogInY3YW1vYl9lbjc2Tl9VRm5lZDFJaXciLCAib3JnLmZvcmdlcm9jay5vcGVuaWRjb25uZWN0Lm9wcyI6ICI2YjI3OWI0MC1iYjdhLTRjMmItYTQzNC01Y2YyYmNmMzcyMjUiLCAiYXpwIjogImZyZWctdG9rZW4tcHJvdmlkZXItdDAiLCAiYXV0aF90aW1lIjogMTUzOTY4MDMwMCwgInJlYWxtIjogIi8iLCAiZXhwIjogMTUzOTY4MzkwMCwgInRva2VuVHlwZSI6ICJKV1RUb2tlbiIsICJpYXQiOiAxNTM5NjgwMzAwIH0.CEPqay8UMpqVDUB9VMYMWtGgL5NoKY5fqy4-0216OxZ6zSQgUwJ9tqqMXD12yFNIACSOTTRdwahA0qgBDoRZH-u67mnEYXF_pFwhYpOWbwZy3W67B3re8dti8pMoCbFv0ydSifVkyDD44LEEUbnIBV-GvWgcY8E5pASwjycdRJAskCP3m2Bo9GY4XV10lDv59mM9G5wr3kpOn9iiPK0NFxdSizJZjh2qv9KtId9fuC7E-8SZvvUlKjChcIwP-gYiFkqce7KBhYkbwr9eYZ5wSwIY7107xAYVgKoRrjAbEm1okity6yEow0RwoEU-JlUe5Tbvd8mmjrJSHBEgEPiZyw"

internal fun getTokenDingsIdportenToken() =
    "eyJraWQiOiJjWmswME1rbTVIQzRnN3Z0NmNwUDVGSFpMS0pzdzhmQkFJdUZiUzRSVEQ0IiwiYWxnIjoiUlMyNTYifQ.eyJhdF9oYXNoIjoiVHdKb0NqblNralhPdW1Vd2c1YkFvQSIsInN1YiI6InRxZmxCeEQ4VGc4X2J0OXQtaDJKMDRZYjNrejhwN2NoS2FzcHRGMVNQSDQ9IiwiYW1yIjpbIkJhbmtJRCJdLCJpc3MiOiJodHRwczpcL1wvb2lkYy12ZXIyLmRpZmkubm9cL2lkcG9ydGVuLW9pZGMtcHJvdmlkZXJcLyIsInBpZCI6IjA4MDg5NDA3OTY3IiwibG9jYWxlIjoibmIiLCJzaWQiOiJ3SGlwZlNtZFRON3JSV1p5M1VhRmx6dGJVVllDSjVhTEpzREtMUnZTS3QwIiwiYXVkIjoiZWUxMmY0NzQtYmE0My00MWRjLWJjYjUtYzZkMzJmM2JhODcyIiwiYWNyIjoiTGV2ZWw0IiwiYXV0aF90aW1lIjoxNjI5MTk3ODM4LCJleHAiOjE2MjkxOTc5NTgsImlhdCI6MTYyOTE5NzgzOCwianRpIjoiQklDaUFmUTk5Y3p5UXZ1WUFPeG1HdmpEOVlRdWZqMEg3am5PbVY5cFYwYyJ9.ludFlrX5eAiJOmi8sBF4yhjzbehIURREj5pXIxpiuGeNuXIrqivV-jWAgAP7Czm8FZu9I1XLpia18EbZXe5kq1yUXNQHwgIC6vJp79MF58Cne7Tkr3GI5U-1O09U33FMhDzIHn7ecHgng2LYHRAHCWQHjkMiItUrAIUUNrgBNDMZpyw-xO-HcE-nQ2aSZ6cCy7xxn2RrpF0YSNemcsQ3Dr67QaWNQ7fezhYIq0znYe6bS9sWefzF_GnC_MFJ2j6ozYlLhm0_YbRpwfLTRujLwJPYYIm8HkJjG6QFL0BupikksF-4BSVO8AmcKFf2X2_43iqL5jF-Wc39xc97fcUVeQ"

internal fun getTokenDingsAzureADB2CToken() =
    "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6ImZ5akpfczQwN1ZqdnRzT0NZcEItRy1IUTZpYzJUeDNmXy1JT3ZqVEFqLXcifQ.eyJleHAiOjE2MjkyMDg4NjAsIm5iZiI6MTYyOTIwNTI2MCwidmVyIjoiMS4wIiwiaXNzIjoiaHR0cHM6Ly9uYXZ0ZXN0YjJjLmIyY2xvZ2luLmNvbS9kMzhmMjVhYS1lYWI4LTRjNTAtOWYyOC1lYmY5MmMxMjU2ZjIvdjIuMC8iLCJzdWIiOiIwODA4OTQwNzk2NyIsImF1ZCI6IjAwOTBiNmUxLWZmY2MtNGMzNy1iYzIxLTA0OWY3ZDFmMGZlNSIsImFjciI6IkxldmVsNCIsIm5vbmNlIjoiNnpvUWU3TkE2Uk9lUHd2aGxYcWEySHFVZVdkT3Zld2ZybjBxYlRoMkt6ayIsImlhdCI6MTYyOTIwNTI2MCwiYXV0aF90aW1lIjoxNjI5MjA1MjU5LCJqdGkiOiJsOEFYdWtrbjl6LUJ6M1VSNW9JQkYtN2kxcXpxa0dpVmVVN2hPeG5VMGZrIiwiYXRfaGFzaCI6IkY5YlVlX3pObXhyM00ybWlPNk9mbFEifQ.SEFNMEYG7-B1iCwc_UnwTqtvJ6t42KPExV_YiovQjb1fQfLGqx8KLWczJL_J9vtC01PLbNBMXZ5LKnsVvDFbdBzwa_odOj8WD4hJSMym4mJJ2GrcJLO1UYT640j5N0vQ37I7M-HGxVVIu8bBDlgpD50T64OW_BugCoQRXvetIG-mo4OsXX94ffPfkje5N_tFw0YBI4rdqw2xO1n9-iwYNVfxrLGT61jI5o3kmxqPn0OgF8X6nVZVJB75Uq5N1Fneiox40v2Pt7Vtv7kBs5Yk1pAfZAJl7HQTsYb4VTcLSF5plUjLxSlApFxUR76e2gZTPMn_jsobLt9_retaotporA"
