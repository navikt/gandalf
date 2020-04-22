package no.nav.gandalf

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(ApplicationGeneralEnvironment::class)
class GandalfApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<GandalfApplication>(*args)
        }
    }
}