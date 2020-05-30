package no.nav.gandalf.config

import no.nav.gandalf.ldap.CustomAuthenticationProvider
import no.nav.gandalf.ldap.RestAccessDeniedHandler
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
            // you will likely want to disable CSRF protection
            .cors().disable()
            .csrf().disable()
            .formLogin().disable()
            .authorizeRequests()
            .antMatchers(
                "/v1/sts/token2",
                "/.well-known/openid-configuration",
                "/v1/sts/.well-known/openid-configuration",
                "/jwks",
                "/v1/sts/jwks"
            ).permitAll()
            .and()
            .authorizeRequests().anyRequest().authenticated()
            .and()
            .httpBasic()
            .authenticationEntryPoint(authenticationEntryPoint())
            .and()
            // .exceptionHandling()
            // .accessDeniedHandler(accessDeniedHandler())
            // .authenticationEntryPoint(authenticationEntryPoint())
            // .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }

    @Bean
    fun accessDeniedHandler(): RestAccessDeniedHandler? {
        return RestAccessDeniedHandler()
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
