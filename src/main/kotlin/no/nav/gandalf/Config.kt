package no.nav.gandalf

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import javax.xml.crypto.KeySelector

@Configuration
@ComponentScan("no.nav")
class ApplicationConfiguration {

    @Autowired
    private lateinit var keySelector: KeySelector

    @Bean
    fun keySelector(): KeySelector? {
        return keySelector
    }
}