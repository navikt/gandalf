package no.nav.gandalf.accesstoken

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldNotBe
import no.nav.security.mock.oauth2.withMockOAuth2Server
import org.junit.Test

class IssuerConfigTest {

    @Test
    fun `getKeyByKeyId should return actual key when kid is found in jwks`() {
        withMockOAuth2Server {
            val issuerConfig = IssuerConfig.from(
                this.issuerUrl("issuer1").toString(),
                this.jwksUrl("issuer1").toString()
            )
            val keyId = this.issueToken(issuerId = "issuer1").header.keyID
            issuerConfig.getKeyByKeyId(keyId) shouldNotBe null
        }
    }

    @Test
    fun `getKeyByKeyId should fail when kid is not found in jwks`() {
        withMockOAuth2Server {
            val issuerConfig = IssuerConfig.from(
                this.issuerUrl("issuer1").toString(),
                this.jwksUrl("issuer1").toString()
            )
            shouldThrow<OAuthException> {
                issuerConfig.getKeyByKeyId("shouldNotBeFound") shouldNotBe null
            }
        }
    }
}
