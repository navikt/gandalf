package no.nav.gandalf.config

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
class SecurityConfig : WebSecurityConfigurerAdapter() {
    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.csrf().disable()
        // Env var for sjekking om test
        if (true) {
            return
        }
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("rest/**").authenticated()
                .anyRequest().authenticated()
                .and()
                .httpBasic()
    }

    //  @Bean
    //  fun passwordEncoder(): PasswordEncoder {
    //      return BCryptPasswordEncoder()
    //  }
//
    //  @Autowired
    //  @Throws(Exception::class)
    //  fun configureGlobal(auth: AuthenticationManagerBuilder) {
    //      auth.inMemoryAuthentication()
    //              .withUser("user1").password(passwordEncoder().encode("user1Pass"))
    //              .authorities("ROLE_USER")
    //  }
}