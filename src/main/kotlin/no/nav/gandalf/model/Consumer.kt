package no.nav.gandalf.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

@JsonIgnoreProperties(ignoreUnknown = true)
class Consumer {
    var authority: String? = null

    @JsonProperty("ID")
    @SerializedName("ID")
    var id: String? = null
}
