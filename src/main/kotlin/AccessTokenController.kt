import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("v1/sts")
class AccessTokenController() {

    @GetMapping("/token", produces = ["application/json"])
    fun getOIDCToken() {
        val name = authDetails().name
        val password = authDetails().credentials
    }

    fun authDetails() = SecurityContextHolder.getContext().authentication
}