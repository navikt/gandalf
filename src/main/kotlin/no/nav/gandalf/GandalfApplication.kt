package no.nav.gandalf

import no.nav.gandalf.config.LocalLdapConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

@SpringBootApplication
class GandalfApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty("oracle.jdbc.fanEnabled", "false")
            val activeProfiles = System.getProperty("spring.profiles.active", "") + " " + (System.getenv("SPRING_PROFILES_ACTIVE") ?: "")
            val builder = SpringApplicationBuilder(GandalfApplication::class.java)
            if (activeProfiles.contains("local")) {
                builder.initializers(LocalLdapConfig())
            }
            builder.run(*args)
        }
    }
}
