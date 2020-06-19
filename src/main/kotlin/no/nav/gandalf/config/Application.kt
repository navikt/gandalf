package no.nav.gandalf.config

import io.prometheus.client.CollectorRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import javax.xml.crypto.KeySelector

@Configuration
class Application {

    @Autowired
    private lateinit var keySelector: KeySelector

    @Bean
    fun keySelector(): KeySelector? {
        return keySelector
    }

    @Bean
    @Profile("remote")
    fun prometheusCollector(): CollectorRegistry? {
        return CollectorRegistry.defaultRegistry
    }
}
