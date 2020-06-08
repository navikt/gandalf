package no.nav.gandalf.accesstoken

import com.nimbusds.oauth2.sdk.`as`.AuthorizationServerMetadata
import mu.KotlinLogging
import no.nav.gandalf.http.ProxyAwareResourceRetriever
import org.springframework.stereotype.Component
import java.net.URL

private val log = KotlinLogging.logger { }

@Component
class DIFIConfiguration {

    fun getAuthServerMetadata(wellknownUrl: String): AuthorizationServerMetadata {
        log.info { "Metadata from wellknown: $wellknownUrl" }
        return AuthorizationServerMetadata.parse(ProxyAwareResourceRetriever().retrieveResource(URL(wellknownUrl)).content)
    }
}
