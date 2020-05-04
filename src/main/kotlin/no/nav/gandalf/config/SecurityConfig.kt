package no.nav.gandalf.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder


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
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("rest/**").authenticated()
                .anyRequest().authenticated()
                .and()
                .httpBasic()
    }

    @Throws(java.lang.Exception::class)
    override fun configure(auth: AuthenticationManagerBuilder) {
        if (isTest()) {
            return
        }
        auth
                .ldapAuthentication()
                .userDnPatterns("uid={0},ou=ServiceAccounts")
                .userSearchFilter("uid={0}")
                .groupSearchBase("ou=ServiceAccounts")
                .contextSource()
                .url("${ldapConfig.urls[0]}/${ldapConfig.base}")
                .and()
                .passwordCompare()
                .passwordEncoder(BCryptPasswordEncoder())
                .passwordAttribute("userPassword")
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder? {
        return BCryptPasswordEncoder()
    }

    fun isTest() = !ldapConfig.remote.contains("remote")
}
