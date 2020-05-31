package no.nav.gandalf.accesstoken

import no.nav.gandalf.ldap.InMemoryLdap
import no.nav.gandalf.utils.ControllerUtil
import no.nav.gandalf.utils.WS_SAMLTOKEN
import org.apache.http.entity.ContentType
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import javax.annotation.PostConstruct

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = no.nav.gandalf.utils.PORT)
@TestPropertySource(locations = ["classpath:application-test.properties"])
@DirtiesContext
class WSSAMLTokenControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var issuer: AccessTokenIssuer

    private val controllerUtil = ControllerUtil()

    private val inMemoryLdap = InMemoryLdap()

    @PostConstruct
    fun setupKnownIssuers() {
        controllerUtil.setupKnownIssuers()
        controllerUtil.runLdap(inMemoryLdap)
    }

    @After
    fun clear() {
        controllerUtil.stopLdap(inMemoryLdap)
    }

    @Test
    fun `Validate Expired SAML Token`() {
        mvc.perform(
            MockMvcRequestBuilders.post(WS_SAMLTOKEN)
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
                .contentType(ContentType.TEXT_XML.mimeType)
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(MockMvcResultMatchers.content().contentType("text/xml;charset=UTF-8"))
        // .andExpect(MockMvcResultMatchers.xpath("Unauthorized user").string("")) }
    }
}
