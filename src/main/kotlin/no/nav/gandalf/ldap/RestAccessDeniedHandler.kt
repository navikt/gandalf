package no.nav.gandalf.ldap

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.gandalf.api.INVALID_CLIENT
import no.nav.gandalf.model.ErrorDescriptiveResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import java.io.IOException
import java.io.OutputStream
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class RestAccessDeniedHandler : AccessDeniedHandler {
    @Throws(IOException::class, ServletException::class)
    override fun handle(
        httpServletRequest: HttpServletRequest?,
        httpServletResponse: HttpServletResponse,
        e: AccessDeniedException
    ) {
        val response = ErrorDescriptiveResponse(INVALID_CLIENT, "Access Denied, ${e.message ?: ""}")
        val out: OutputStream = httpServletResponse.outputStream
        val mapper = ObjectMapper()
        mapper.writeValue(out, response)
        out.flush()
    }
}
