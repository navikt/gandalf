package no.nav.gandalf.ldap

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.context.TestPropertySource

//@RunWith(SpringRunner::class)
//@SpringBootTest
@TestPropertySource(
        locations = ["classpath:application-test.properties"],
        properties = ["spring.profiles.active=remote"]
)
@AutoConfigureMockMvc
class LdapTest {

    // @Autowired
    // private lateinit var mvc: MockMvc
//
    // @Test
    // fun `Valid User`() {
    //     val password = Password(BCryptPasswordEncoder().encode("password"))
    //     println(password.toString())
    //     mvc.perform(get("/v1/sts/test").with(httpBasic("ben", "benpassword")))
    //             .andExpect(status().isOk)
    //     // .andExpect(content().string(equalTo("Greetings from Spring Boot!")));
    // }
}
