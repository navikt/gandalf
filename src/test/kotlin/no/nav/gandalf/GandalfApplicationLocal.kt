package no.nav.gandalf

import no.nav.gandalf.utils.ControllerUtil
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Profile

@SpringBootApplication
@Profile("local")
class GandalfApplicationLocal {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val controllerUtil = ControllerUtil()
            val mockOAuth2Server = MockOAuth2Server()
            mockOAuth2Server.start(port = 1113)
            controllerUtil.runLdap()
            SpringApplicationBuilder(GandalfApplication::class.java).run(*args)
        }
    }
}
