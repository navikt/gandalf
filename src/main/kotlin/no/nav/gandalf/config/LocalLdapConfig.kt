package no.nav.gandalf.config

import no.nav.gandalf.ldap.InMemoryLdap
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("local")
class LocalLdapConfig : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val ldap = InMemoryLdap()
        ldap.start()
        applicationContext.beanFactory.registerSingleton("inMemoryLdap", ldap)
    }
}
