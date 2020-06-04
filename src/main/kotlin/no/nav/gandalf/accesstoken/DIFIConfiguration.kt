package no.nav.gandalf.accesstoken

import com.nimbusds.oauth2.sdk.`as`.AuthorizationServerMetadata
import no.nav.gandalf.http.ProxyAwareResourceRetriever
import org.springframework.stereotype.Component
import java.net.URL

@Component
class DIFIConfiguration {

    fun getAuthServerMetadata(wellknownUrl: String): AuthorizationServerMetadata {
        return AuthorizationServerMetadata.parse(ProxyAwareResourceRetriever().retrieveResource(URL(wellknownUrl)).content)
    }
}
