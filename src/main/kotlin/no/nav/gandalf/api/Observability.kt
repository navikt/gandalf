package no.nav.gandalf.api

import io.swagger.v3.oas.annotations.Operation
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
}
