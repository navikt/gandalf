package no.nav.gandalf.api

import io.prometheus.client.Counter

class ExceptionHandler () {
    companion object {
        val errorCounter: Counter = Counter.build().help("Interne feil").namespace("security-token-service").name("internal_server_errors_total").labelNames("exception").register()
    }

    // TODO
}
