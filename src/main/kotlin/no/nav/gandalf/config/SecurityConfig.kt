package no.nav.gandalf.config

import no.nav.gandalf.ldap.CustomAuthenticationProvider
import no.nav.gandalf.ldap.RestAuthenticationEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import javax.inject.Inject

@Configuration
class SecurityConfig(
    val ldapConfig: LdapConfig
) : WebSecurityConfigurerAdapter() {

    @Inject
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(activeDirectoryLdapAuthenticationProvider())
    }

    override fun configure(http: HttpSecurity) {
        http
            // If you are only creating a service that is used by non-browser clients,
            // you will likely want to disable CSRF protection same for cors
            .cors().disable()
            .csrf().disable()
            .formLogin().disable()
            .authorizeRequests()
            .antMatchers(
                "/rest/v1/sts/token2",
                "/rest/v1/sts/ws/samltoken",
                "/.well-known/openid-configuration",
                "/rest/v1/sts/.well-known/openid-configuration",
                "/jwks",
                "/rest/v1/sts/jwks",
                "/isAlive",
                "/isReady",
                "/actuator/**"
            ).permitAll()
            .and()
            .authorizeRequests().anyRequest().authenticated()
            .and()
            .httpBasic()
            .authenticationEntryPoint(authenticationEntryPoint())
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }

    @Bean
    fun authenticationEntryPoint(): RestAuthenticationEntryPoint? {
        return RestAuthenticationEntryPoint()
    }

    @Bean
    @Throws(Exception::class)
    override fun authenticationManagerBean(): AuthenticationManager? {
        return super.authenticationManagerBean()
    }

    @Bean
    fun activeDirectoryLdapAuthenticationProvider(): AuthenticationProvider? {
        return CustomAuthenticationProvider(ldapConfig)
    }
}
