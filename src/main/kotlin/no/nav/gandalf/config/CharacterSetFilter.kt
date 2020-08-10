package no.nav.gandalf.config

import java.io.IOException
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

class CharacterSetFilter : Filter {

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        next: FilterChain
    ) {
        request.characterEncoding = "UTF-8"
        next.doFilter(request, response)
    }
}
