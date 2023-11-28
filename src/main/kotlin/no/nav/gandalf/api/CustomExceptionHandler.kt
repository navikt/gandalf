package no.nav.gandalf.api

import com.unboundid.ldap.sdk.LDAPException
import no.nav.gandalf.model.ErrorDescriptiveResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@SuppressWarnings("unchecked", "rawtypes")
@ControllerAdvice
class CustomExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(
        ex: Exception,
        request: WebRequest?,
    ): ResponseEntity<Any?> {
        val error = ErrorDescriptiveResponse("Server Error", ex.localizedMessage)
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(LDAPException::class)
    fun handleLdapError(
        ex: LDAPException,
        request: WebRequest?,
    ): ResponseEntity<Any?> {
        val error = ErrorDescriptiveResponse(INVALID_CLIENT, ex.localizedMessage)
        return ResponseEntity(error, HttpStatus.UNAUTHORIZED)
    }
}
