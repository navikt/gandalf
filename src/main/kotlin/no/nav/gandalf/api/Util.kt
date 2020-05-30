package no.nav.gandalf.api

import mu.KotlinLogging
import no.nav.gandalf.model.ErrorDescriptiveResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

internal const val INVALID_CLIENT = "invalid_client"
internal const val INVALID_REQUEST = "invalid_request"
internal const val INTERNAL_SERVER_ERROR = "internal_server_error"

private val log = KotlinLogging.logger { }

internal val tokenHeaders = HttpHeaders().apply {
    add("Cache-Control", "no-store")
    add("Pragma", "no-cache")
}

internal fun serverErrorResponse(e: Exception): ResponseEntity<Any> {
    log.error(e) { "Error: " + e.message }
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(INTERNAL_SERVER_ERROR)
}

internal fun unauthorizedResponse(e: Exception, errorMessage: String): ResponseEntity<Any> {
    log.error(e) { errorMessage }
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(ErrorDescriptiveResponse(INVALID_CLIENT, errorMessage))
}

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
    }
}
