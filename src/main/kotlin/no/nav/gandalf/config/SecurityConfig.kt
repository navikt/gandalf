package no.nav.gandalf.config

import javax.inject.Inject
import no.nav.gandalf.ldap.NAVLdapUserDetailsMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider

@Configuration
class SecurityConfig(
    val ldapConfig: LdapConfig
) : WebSecurityConfigurerAdapter() {

    @Inject
    override fun configure(auth: AuthenticationManagerBuilder) {
        if (!ldapConfig.remote.contains("remote")) {
            val encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
            auth.inMemoryAuthentication()
                    .passwordEncoder(encoder)
                    .withUser("srvPDP").password(encoder.encode("password")).roles("USER")
                    .and()
                    .withUser("srvDatapower").password(encoder.encode("password")).roles("USER")
            return
        }
        auth.authenticationProvider(activeDirectoryLdapAuthenticationProvider())
    }

    override fun configure(http: HttpSecurity) {
        http
                .csrf().disable()
                .formLogin().disable()
                .requestMatchers()
                .antMatchers("rest/**")
                .and()
                .authorizeRequests()
                .antMatchers("rest/**").authenticated()
                .antMatchers("/").permitAll()
                .and()
                .httpBasic()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }

    @Bean
    @Throws(Exception::class)
    override fun authenticationManagerBean(): AuthenticationManager? {
        return super.authenticationManagerBean()
    }

    // @Bean
    // @Throws(Exception::class)
    // override fun authenticationManagerBean(): AuthenticationManager? {
    //     return super.authenticationManagerBean()
    // }

    @Bean
    fun activeDirectoryLdapAuthenticationProvider(): AuthenticationProvider? {
        val provider = ActiveDirectoryLdapAuthenticationProvider(ldapConfig.base, "${ldapConfig.url}:${ldapConfig.port}")
        provider.setUserDetailsContextMapper(NAVLdapUserDetailsMapper())
        provider.setConvertSubErrorCodesToExceptions(true)
        provider.setUseAuthenticationRequestCredentials(true)
        provider.setSearchFilter("(&(objectClass=user)(|(sAMAccountName={1})(userPrincipalName={0})(mail={0})))")
        return provider
    }
}
