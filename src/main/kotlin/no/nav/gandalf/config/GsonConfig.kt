package no.nav.gandalf.config

import com.google.gson.GsonBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GsonConfig {
    @Bean
    fun gsonBuilder(): GsonBuilder = GsonBuilder()
}
