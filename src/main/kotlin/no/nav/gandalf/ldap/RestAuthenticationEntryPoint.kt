package no.nav.gandalf.ldap

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.gandalf.api.INVALID_CLIENT
import no.nav.gandalf.model.ErrorDescriptiveResponse
import org.apache.http.entity.ContentType
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.IOException
import java.io.OutputStream
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class RestAuthenticationEntryPoint : AuthenticationEntryPoint {
    @Throws(IOException::class, ServletException::class)
    override fun commence(
        httpServletRequest: HttpServletRequest?,
        httpServletResponse: HttpServletResponse,
        e: AuthenticationException?
    ) {
        val response = ErrorDescriptiveResponse(INVALID_CLIENT, "Unauthorised: ${e?.message ?: "" }")
        httpServletResponse.status = HttpStatus.UNAUTHORIZED.value()
        httpServletResponse.contentType = ContentType.APPLICATION_JSON.mimeType
        val out: OutputStream = httpServletResponse.outputStream
        val mapper = ObjectMapper()
        mapper.writeValue(out, response)
        out.flush()
    }
}
