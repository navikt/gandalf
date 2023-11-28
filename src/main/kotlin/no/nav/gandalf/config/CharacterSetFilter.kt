package no.nav.gandalf.config

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import java.io.IOException

class CharacterSetFilter : Filter {

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        next: FilterChain,
    ) {
        request.characterEncoding = "UTF-8"
        next.doFilter(request, response)
    }
}
