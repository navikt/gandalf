package no.nav.gandalf.config

import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder

private val log = KotlinLogging.logger { }
private const val REALM_NAME = "gandalf"

@Configuration
class SecurityConfig(
    val ldapConfig: LdapConfig
) : WebSecurityConfigurerAdapter() {
    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.csrf().disable()
        if (isTest()) {
            return
        }
        http
                .csrf().disable()
                .formLogin().disable()
                .httpBasic()
                .and()
                .authorizeRequests()
                .antMatchers("rest/**").authenticated()
                .antMatchers("/").permitAll()
        // .realmName(REALM_NAME)
    }

    @Throws(java.lang.Exception::class)
    override fun configure(auth: AuthenticationManagerBuilder) {
        if (isTest()) {
            auth.inMemoryAuthentication().withUser("srvPDP").password("password").roles("USER")
            return
        }
        auth
                .ldapAuthentication()
                // .userSearchFilter("(uid={0})")
                .userDnPatterns("uid={0},ou=ServiceAccounts", "uid={0},ou=ApplAccounts,ou=ServiceAccounts")
                .userSearchBase("ou=ServiceAccounts")
                // .userSearchFilter("(uid={0})")
                // .userDnPatterns("uid={0},ou=ServiceAccounts")
                .contextSource()
                .url("${ldapConfig.urls}/${ldapConfig.base}")
                .and()
                .passwordCompare()
        // .passwordEncoder(passwordEncoder())
        // .passwordAttribute("userPassword")

        log.info { "Security configuration loaded." }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder? {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    fun isTest() = !ldapConfig.remote.contains("remote")
}
