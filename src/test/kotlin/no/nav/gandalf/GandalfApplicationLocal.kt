package no.nav.gandalf

import no.nav.gandalf.ldap.InMemoryLdap
import no.nav.gandalf.utils.ControllerUtil
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile

@SpringBootApplication
@Profile("local")
class GandalfApplicationLocal {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val controllerUtil = ControllerUtil()
            controllerUtil.runLdap(InMemoryLdap())
            runApplication<GandalfApplicationLocal>(*args)
        }
    }
}
