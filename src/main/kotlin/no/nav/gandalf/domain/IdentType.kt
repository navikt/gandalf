package no.nav.gandalf.domain

enum class IdentType(val value: String) {
    SYSTEMRESSURS("Systemressurs"),
    INTERNBRUKER("InternBruker"),
    EKSTERNBRUKER("EksternBruker"),
    SAMHANDLER("Samhandler")
}