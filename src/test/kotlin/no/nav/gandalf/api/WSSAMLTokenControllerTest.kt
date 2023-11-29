package no.nav.gandalf.api

import com.nimbusds.jwt.SignedJWT
import io.mockk.every
import io.mockk.mockkStatic
import no.nav.gandalf.SpringBootWireMockSetup
import no.nav.gandalf.accesstoken.AccessTokenIssuer
import no.nav.gandalf.utils.WS_SAMLTOKEN
import no.nav.gandalf.utils.getOidcToSamlRequest
import no.nav.gandalf.utils.getSamlRequest
import no.nav.gandalf.utils.getValidateSamlRequest
import org.apache.http.entity.ContentType
import org.junit.Ignore
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date

@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
class WSSAMLTokenControllerTest : SpringBootWireMockSetup() {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var issuer: AccessTokenIssuer

    @Test
    fun `SAML - WS - User Not In Ldap`() {
        val xmlReq = setupValidateRequest("srvPD", "password", issuer)
        mvc.perform(
            MockMvcRequestBuilders.post(WS_SAMLTOKEN)
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
                .contentType(ContentType.TEXT_XML.mimeType)
                .content(xmlReq)
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(MockMvcResultMatchers.content().contentType("text/xml;charset=UTF-8"))
    }

    @Test
    fun `SAML - WS - isIssueSamlFromUNT`() {
        val xmlReq: String = getSamlRequest("srvPDP", "password")
        mvc.perform(
            MockMvcRequestBuilders.post(WS_SAMLTOKEN)
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
                .contentType(ContentType.TEXT_XML.mimeType)
                .content(xmlReq)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType("text/xml;charset=UTF-8"))
        // TODO Validate xpath response
        // .andExpect(MockMvcResultMatchers.xpath("/*/soapenv:Body/").exists())
    }

    @Test
    fun `SAML - WS - isExchangeOidcToSaml`() {
        val xmlReq = setupOIDCtoSAMLRequest("srvPDP", "password", issuer)
        mvc.perform(
            MockMvcRequestBuilders.post(WS_SAMLTOKEN)
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
                .contentType(ContentType.TEXT_XML.mimeType)
                .content(xmlReq)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType("text/xml;charset=UTF-8"))
        // TODO Validate xpath response
        // .andExpect(MockMvcResultMatchers.xpath("/*/soapenv:Body/").exists())
    }

    @Test
    fun `SAML - WS - isValidateSaml`() {
        val xmlReq = setupValidateRequest("srvPDP", "password", issuer)
        mvc.perform(
            MockMvcRequestBuilders.post(WS_SAMLTOKEN)
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
                .contentType(ContentType.TEXT_XML.mimeType)
                .content(xmlReq)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType("text/xml;charset=UTF-8"))
        // TODO Validate response
        // .andExpect(MockMvcResultMatchers.xpath("/*/soapenv:Body/").exists())
    }

    @Ignore("MockkStatic is disabled on newer java versions")
    @Test
    fun `exchange oidc from tokendings and idporten to SAML`() {
        val token = "eyJraWQiOiI3YmM4MjAxYS1lNDkxLTQxZDMtYjVlZC0zNTU1NjRjMjE4MDgiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdF9oYXNoIjoiY2ZCRFJuODFYSkJnUG9NWVZyd2JFZyIsInN1YiI6IlFGeUJvYW9HQ2ZYV2hLR0tyQzl5dkVnY2lyTzdFblktNjVCU1kteGNrdG89IiwiYW1yIjpbIkJhbmtJRCJdLCJpc3MiOiJodHRwczpcL1wvdG9rZW5kaW5ncy5kZXYtZ2NwLm5haXMuaW8iLCJwaWQiOiIwODA4OTQwNjMxNiIsImxvY2FsZSI6Im5iIiwiY2xpZW50X2lkIjoiZGV2LWdjcDpwbGF0dGZvcm1zaWtrZXJoZXQ6ZGVidWctZGluZ3MiLCJzaWQiOiJRUWxzWTk5Qi1nQ0lHSGEtWHpVVXpzdHl4VjE1d3UwZmdyamZ5ZG5OSTRzIiwiYXVkIjoiZGV2LWdjcDpwbGF0dGZvcm1zaWtrZXJoZXQ6YXBpLWRpbmdzIiwiYWNyIjoiTGV2ZWw0IiwibmJmIjoxNjI5ODAzNzAxLCJpZHAiOiJodHRwczpcL1wvb2lkYy12ZXIyLmRpZmkubm9cL2lkcG9ydGVuLW9pZGMtcHJvdmlkZXJcLyIsImF1dGhfdGltZSI6MTYyOTgwMzQ4OSwiZXhwIjoxNjI5ODA0MDAxLCJpYXQiOjE2Mjk4MDM3MDEsImp0aSI6ImY4OGRjZjFkLWNlNDgtNDUwYy05MWExLWQwZThiYzdhZDNiZiJ9.nn2lF8H_GjlSYr_s7Mx_QRJMzK-_kiGIAUSZ4UQ1uT89luJ8juJbYQHykbiHiQmIF0Z5TEPgFO4Irc9GcKFVbUDRmAB7ucthCD3WBjSK1MUec_qTdynEtq3CxJMC1Edag2XN0GQLNO4ENHa0hqb9eKzMMS19W8fTw9P3ONPK4A-oi7WqvYCsNfozKsPtNuBSm0MkqKMjlEpAoXvF-TgMvm-JW9wuI3Y4DkTq7n1v0MnMJxnJAQ7twZgBcSx2Ff4Ck0uPXcvICCuvvr6EcNmNgWtBWRNwD3acOpSN17b-Tt78CdK-9-lp_SqV0Cl0g5UBKtH_Ph2s4kmkc12oY2HqxA"
        val xmlReq = getOidcToSamlRequest("srvPDP", "password", token)
        mockkZonedDateTimeNow(SignedJWT.parse(token).jwtClaimsSet.issueTime)
        mvc.perform(
            MockMvcRequestBuilders.post(WS_SAMLTOKEN)
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
                .contentType(ContentType.TEXT_XML.mimeType)
                .content(xmlReq)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType("text/xml;charset=UTF-8"))
        // TODO Validate xpath response
        // .andExpect(MockMvcResultMatchers.xpath("/*/soapenv:Body/").exists())
    }
}

internal fun setupValidateRequest(
    username: String,
    password: String,
    issuer: AccessTokenIssuer
): String {
    val samlToken: String? = issuer.issueSamlToken(username, username, "0")
    return getValidateSamlRequest(username, password, samlToken!!)
}

internal fun setupOIDCtoSAMLRequest(
    username: String,
    password: String,
    issuer: AccessTokenIssuer
): String {
    val oidcToken: String = issuer.issueToken(username).serialize()
    return getOidcToSamlRequest(username, password, oidcToken)
}

internal fun mockkZonedDateTimeNow(now: Date) {
    ZonedDateTime.now().also {
        mockkStatic(ZonedDateTime::class)
        every { ZonedDateTime.now() } returns ZonedDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault())
    }
}
