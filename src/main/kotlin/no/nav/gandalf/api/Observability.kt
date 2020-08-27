package no.nav.gandalf.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class Observability {

    @Operation(hidden = true)
    @GetMapping("/isAlive")
    fun isAlive() = true

    @Operation(hidden = true)
    @GetMapping("/isReady")
    fun isReady() = true

    @Operation(summary = "Ping the server and retrieve the server response.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Ping",
                content = [
                    (
                        Content(
                            mediaType = "application/json",
                            schema = Schema(implementation = Ping::class)
                        )
                        )
                ]
            ),
            ApiResponse(
                responseCode = "200",
                description = "Application response OK",
                content = [Content()]
            )
        ]
    )
    @GetMapping("/ping")
    fun ping() = ResponseEntity
        .status(HttpStatus.OK)
        .body(Ping(true))
}

data class Ping(
    val status: Boolean
)
