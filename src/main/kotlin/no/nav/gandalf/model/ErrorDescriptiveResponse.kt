package no.nav.gandalf.model

import io.swagger.v3.oas.annotations.Parameter
import no.nav.gandalf.api.INVALID_CLIENT

class ErrorDescriptiveResponse(
    @Parameter(name = "error", description = "Type of error", example = INVALID_CLIENT)
    val error: String,
    @Parameter(name = "error_description", description = "Describe error, if any error message", example = "Unauthorised: Full authentication is required to access this resource")
    val error_description: String
)
