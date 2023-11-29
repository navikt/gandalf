package no.nav.gandalf.config

import io.prometheus.client.CollectorRegistry
import org.springdoc.core.SpringDocUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
import javax.xml.crypto.KeySelector

@Configuration
class Application {
    companion object {
        @JvmStatic
        lateinit var springDocUtils: SpringDocUtils
    }

    init {
        springDocUtils = SpringDocUtils.getConfig().replaceWithClass(Pageable::class.java, org.springdoc.core.converters.models.Pageable::class.java)
    }

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
