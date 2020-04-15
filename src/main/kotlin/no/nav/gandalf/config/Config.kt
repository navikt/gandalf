package no.nav.gandalf.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import java.net.http.HttpClient
import javax.xml.crypto.KeySelector


@Configuration
@ComponentScan("no.nav.no.nav.gandalf")
class Config {

    @Autowired
    private val keySelector: KeySelector? = null

    @Autowired
    private val httpClient: HttpClient? = null

    @Bean
    fun keySelector(): KeySelector? {
        return keySelector
    }

    @Bean
    fun httpClient(): HttpClient? {
        return httpClient
    }
}