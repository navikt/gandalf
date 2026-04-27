package no.nav.gandalf

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext

class WireMockInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val server = WireMockServer(wireMockConfig().dynamicPort()).also { it.start() }
        applicationContext.beanFactory.registerSingleton("wireMockServer", server)
        WireMock.configureFor("localhost", server.port())
        TestPropertyValues
            .of("wiremock.server.port=${server.port()}")
            .applyTo(applicationContext.environment)
    }
}
