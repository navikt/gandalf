package no.nav.gandalf.config

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import javax.sql.DataSource

private val log = KotlinLogging.logger { }

@Configuration
@Profile("remote")
class DataSourceRemoteConfiguration(
    @Value("\${spring.datasource.url}")
    val url: String,
    @Value("\${spring.datasource.username}")
    val username: String,
    @Value("\${spring.datasource.password}")
    val password: String,
) {

    @Bean
    fun getDataSource(): DataSource? {
        val dataSourceBuilder = DataSourceBuilder.create()
        dataSourceBuilder.driverClassName("oracle.jdbc.OracleDriver")
        dataSourceBuilder.url(url)
        dataSourceBuilder.username(username)
        dataSourceBuilder.password(password)
        log.info { "Setting up datasource with Oracle" }
        return dataSourceBuilder.build()
    }
}
