package no.nav.gandalf

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GandalfApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<GandalfApplication>(*args)
        }
    }
}