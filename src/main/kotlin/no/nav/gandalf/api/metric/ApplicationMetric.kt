package no.nav.gandalf.api.metric

import io.prometheus.client.Counter
import io.prometheus.client.Histogram

object ApplicationMetric {

    internal val tokenOK: Counter = Counter.build()
        .help("Issued OIDC tokens OK.")
        .namespace("securitytokenservice")
        .name("_oidctokenOk")
        .register()

    internal val tokenNotOk: Counter = Counter.build()
        .help("Issued OIDC tokens failed pga bad request or unauthorized.")
        .namespace("securitytokenservice")
        .name("_oidctokenNotOk")
        .register()

    internal val tokenError: Counter = Counter.build()
        .help("Issued OIDC tokens failed pga internal server error.")
        .namespace("securitytokenservice")
        .name("_oidctokenError")
        .register()

    internal val hisogramToken: Histogram = Histogram.build()
        .help("Request latency in seconds /token.")
        .namespace("requests_latency")
        .name("_seconds_token")
        .register()

    internal val token2Ok: Counter = Counter.build()
        .help("Issued OIDC tokens (grensesnitt for stormaskin) OK.")
        .namespace("securitytokenservice")
        .name("_oidctoken2Ok")
        .register()

    internal val token2NotOk: Counter = Counter.build()
        .help("Issued OIDC tokens (grensesnitt for stormaskin) failed pga bad request or unauthorized.")
        .namespace("securitytokenservice")
        .name("_oidctoken2NotOk")
        .register()

    internal val token2Error: Counter = Counter.build()
        .help("Issued OIDC tokens (grensesnitt for stormaskin) failed pga internal server error.")
        .namespace("securitytokenservice")
        .name("_oidctoken2Error")
        .register()
}
