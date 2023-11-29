package no.nav.gandalf.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import no.nav.gandalf.ldap.CustomAuthenticationProvider
import no.nav.gandalf.ldap.LDAPConnectionSetup
import no.nav.gandalf.ldap.RestAuthenticationEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

@Configuration
class SecurityConfig(
    val ldapConfig: LdapConfig
) {
    @Bean
    fun ldapAuthenticationManager(): AuthenticationManager {
        return AuthenticationManager { authentication ->
            activeDirectoryLdapAuthenticationProvider().authenticate(authentication)
        }
    }

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // If you are only creating a service that is used by non-browser clients,
            // you will likely want to disable CSRF protection same for cors
            .cors { cors -> cors.disable() }
            .csrf { csrf -> csrf.disable() }
            .formLogin { formLogin -> formLogin.disable() }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/rest/v1/sts/token2",
                    "/rest/v1/sts/token2/",
                    "/rest/v1/sts/ws/samltoken",
                    "/rest/v1/sts/ws/samltoken/",
                    // Disse to over bruker ldap for auth. men athentesering gjøres seinere.
                    "/.well-known/openid-configuration",
                    "/.well-known/openid-configuration/",
                    "/rest/v1/sts/.well-known/openid-configuration",
                    "/rest/v1/sts/.well-known/openid-configuration/",
                    "/jwks",
                    "/jwks/",
                    "/rest/v1/sts/jwks",
                    "/rest/v1/sts/jwks/",
                    "/isAlive",
                    "/isReady",
                    "/ping",
                    "/prometheus",
                    // Swagger
                    "/api/**",
                    "/swagger-ui/**"
                ).permitAll()
            }.authorizeHttpRequests {
                it.anyRequest().authenticated()
            }.httpBasic {
                it.authenticationEntryPoint(authenticationEntryPoint())
            }.sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }.addFilterBefore(CharacterSetFilter(), BasicAuthenticationFilter::class.java)
        return http.build()
    }

    @Bean
    fun authenticationEntryPoint(): RestAuthenticationEntryPoint? {
        return RestAuthenticationEntryPoint()
    }

    // @Primary
    // @Bean
    fun activeDirectoryLdapAuthenticationProvider(): AuthenticationProvider {
        return CustomAuthenticationProvider(LDAPConnectionSetup(ldapConfig))
    }

    @Bean
    fun openApiSecurity(): OpenAPI? {
        val openApi = OpenAPI()
        if (ldapConfig.url.contains("preprod.local")) {
            openApi.servers =
                mutableListOf(
                    Server().url("https://security-token-service.nais.preprod.local"),
                    Server().url("https://security-token-service.dev.adeo.no")
                )
        }
        openApi.components(
            Components()
                .addSecuritySchemes(
                    "BasicAuth",
                    SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")
                )
        ).info(
            Info()
                .title("Security-Token-Service API.")
                .version("2.0")
                .description(
                    "STS RESTful service description."
                )
        )
        return openApi
    }
}
