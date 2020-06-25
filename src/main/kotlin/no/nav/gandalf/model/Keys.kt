package no.nav.gandalf.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A Set of JSON Web Key (JWK).")
data class Keys(
    val keys: List<Jwk>
)

@Schema(description = "A JSON Web Key (JWK) is a JavaScript Object Notation (JSON) [RFC7159].")
data class Jwk(
    @Schema(description = "The 'kty' (key type) parameter identifies the cryptographic algorithm", example = "RSA")
    val kty: String,
    @Schema(description = "The 'e' Encryption key used.", example = "AQAB")
    val e: String,
    @Schema(
        description = "The 'use' (public key use) parameter identifies the intended use of the public key.",
        example = "sig"
    )
    val use: String,
    @Schema(
        description = "The 'kid' (key ID) parameter is used to match a specific key.",
        example = "5b744e31-e110-44d5-9d6b-18c7ca8107b5"
    )
    val kid: String,
    @Schema(
        description = "The 'alg' (algorithm) parameter identifies the algorithm intended for use with the key.",
        example = "RS256"
    )
    val alg: String,
    @Schema(description = "The 'n' is the public side of the signing keys.", example = "iuOsPy65ZR_...")
    val n: String
)
