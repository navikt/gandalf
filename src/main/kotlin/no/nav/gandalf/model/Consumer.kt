package no.nav.gandalf.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class Consumer {
    var authority: String? = null
    var ID: String? = null
}
