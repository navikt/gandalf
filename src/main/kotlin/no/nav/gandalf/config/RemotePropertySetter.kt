package no.nav.gandalf.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import javax.annotation.PostConstruct

@Profile("remote")
@Configuration
class RemotePropertySetter {

    @PostConstruct
    fun setProperty() {
        System.setProperty("oracle.jdbc.fanEnabled", "false")
    }
}
