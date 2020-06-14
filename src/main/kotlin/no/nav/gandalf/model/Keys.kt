package no.nav.gandalf.model

data class Keys(
    val keys: List<Jwk>
)

data class Jwk(
    val kty: String,
    val e: String,
    val use: String,
    val kid: String,
    val alg: String,
    val n: String

)
