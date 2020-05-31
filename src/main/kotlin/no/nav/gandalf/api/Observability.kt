package no.nav.gandalf.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class Observability {
    @get:GetMapping("/isAlive")
    val isAlive = true

    @get:GetMapping("/isReady")
    val isReady = true
}
