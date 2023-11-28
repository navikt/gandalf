package no.nav.gandalf.api

import mu.KotlinLogging
import no.nav.gandalf.model.ErrorDescriptiveResponse
import org.slf4j.MDC
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

internal const val INVALID_CLIENT = "invalid_client"
internal const val INVALID_REQUEST = "invalid_request"
internal const val INTERNAL_SERVER_ERROR = "internal_server_error"

private val log = KotlinLogging.logger { }

@RestControllerAdvice
object Util {
    internal val tokenHeaders =
        HttpHeaders().apply {
            add("Cache-Control", "no-store")
            add("Pragma", "no-cache")
        }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    internal fun serverErrorResponse(e: Throwable): ResponseEntity<Any> {
        log.error(e) { "Error: " + e.message }
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(INTERNAL_SERVER_ERROR)
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    internal fun unauthorizedResponse(
        e: Throwable,
        errorMessage: String,
    ): ResponseEntity<Any> {
        log.error(e) { errorMessage }
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorDescriptiveResponse(INVALID_CLIENT, errorMessage))
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    internal fun badRequestResponse(errorMessage: String): ResponseEntity<Any> {
        log.error { errorMessage }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorDescriptiveResponse(INVALID_REQUEST, errorMessage))
    }

    internal fun userDetails(): String? {
        return when (SecurityContextHolder.getContext().authentication) {
            null -> null
            else -> {
                val auth = SecurityContextHolder.getContext().authentication as UsernamePasswordAuthenticationToken
                auth.principal as String
            }
        }.also { client -> MDC.put("client_id", client) }
    }
}
