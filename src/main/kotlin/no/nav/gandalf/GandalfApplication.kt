package no.nav.gandalf

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

@SpringBootApplication
class GandalfApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplicationBuilder(GandalfApplication::class.java)
                .profiles("remote").run(*args)
            // runApplication<GandalfApplication>(*args)
        }
    }
}
