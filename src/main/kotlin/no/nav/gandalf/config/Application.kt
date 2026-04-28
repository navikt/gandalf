package no.nav.gandalf.config

import org.springdoc.core.utils.SpringDocUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
    fun keySelector(): KeySelector? = keySelector
}
