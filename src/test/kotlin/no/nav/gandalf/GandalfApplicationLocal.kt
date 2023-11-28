package no.nav.gandalf

import no.nav.security.mock.oauth2.MockOAuth2Server
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Profile
import java.net.InetAddress

@SpringBootApplication
@Profile("local")
class GandalfApplicationLocal {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val mockOAuth2Server = MockOAuth2Server()
            mockOAuth2Server.start(InetAddress.getByName("localhost"), port = 1113)
            SpringApplicationBuilder(GandalfApplication::class.java).run(*args)
        }
    }
}
