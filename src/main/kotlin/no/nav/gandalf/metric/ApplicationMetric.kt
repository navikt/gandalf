package no.nav.gandalf.metric

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter
import io.prometheus.client.Histogram
import org.springframework.stereotype.Component

private const val METRIC_PREFIX = "securitytokenservice"
private const val LATENCY_METRIC_PREFIX = "requests_latency"

@Component
class ApplicationMetric {
    // path: **/token
    companion object {
        private val default = CollectorRegistry.defaultRegistry
        internal val tokenOK =
            Counter.build()
                .help("Issued OIDC tokens OK.")
                .namespace(METRIC_PREFIX)
                .name("oidctokenOk")
                .register(default)
        internal val issuedTokenCounterUnique =
            Counter.build()
                .namespace(METRIC_PREFIX)
                .name("tokens_issued")
                .help("Number of tokens we have issued")
                .labelNames("srvbruker")
                .register(default)
        internal val tokenNotOk =
            Counter.build()
                .help("Issued OIDC tokens failed pga bad request or unauthorized.")
                .namespace(METRIC_PREFIX)
                .name("oidctokenNotOk")
                .register(default)
        internal val tokenError =
            Counter.build()
                .help("Issued OIDC tokens failed pga internal server error.")
                .namespace(METRIC_PREFIX)
                .name("oidctokenError")
                .register(default)
        internal val requestLatencyToken =
            Histogram.build()
                .help("Request latency in seconds /token.")
                .namespace(LATENCY_METRIC_PREFIX)
                .name("seconds_token")
                .register(default)

        // path: **/token2
        internal val token2Ok =
            Counter.build()
                .help("Issued OIDC tokens (grensesnitt for stormaskin) OK.")
                .namespace(METRIC_PREFIX)
                .labelNames("srvbruker")
                .name("oidctoken2Ok")
                .register(default)
        internal val token2NotOk =
            Counter.build()
                .help("Issued OIDC tokens (grensesnitt for stormaskin) failed pga bad request or unauthorized.")
                .namespace(METRIC_PREFIX)
                .name("oidctoken2NotOk")
                .register(default)
        internal val token2Error =
            Counter.build()
                .help("Issued OIDC tokens (grensesnitt for stormaskin) failed pga internal server error.")
                .namespace(METRIC_PREFIX)
                .name("oidctoken2Error")
                .register(default)
        internal val requestLatencyToken2 =
            Histogram.build()
                .help("Request latency in seconds /token2.")
                .namespace(LATENCY_METRIC_PREFIX)
                .name("seconds_token_2")
                .register(default)

        // path: **/samltoken
        internal val samlTokenOk =
            Counter.build()
                .help("Issued saml token OK.")
                .labelNames("srvbruker")
                .namespace(METRIC_PREFIX)
                .name("samlTokenOk")
                .register(default)
        internal val samlTokenNotOk =
            Counter.build()
                .help("Issue saml token failed pga unauthorized eller bad request error.")
                .namespace(METRIC_PREFIX)
                .name("samlTokenNotOk")
                .register(default)
        internal val samlTokenError =
            Counter.build()
                .help("Issued Saml tokens failed pga internal server error.")
                .namespace(METRIC_PREFIX)
                .name("samlTokenError")
                .register(default)
        internal val requestLatencySAMLToken =
            Histogram.build()
                .help("Request latency in seconds /samltoken.")
                .namespace(LATENCY_METRIC_PREFIX)
                .name("seconds_samltoken")
                .register(default)

        // path: **/jwks
        internal val requestLatencyJwks =
            Histogram.build()
                .help("Request latency in seconds /jwks.")
                .namespace(LATENCY_METRIC_PREFIX)
                .name("seconds_jwks")
                .register(default)

        // path: **/token/exchangedifi
        internal val exchangeDIFIOk =
            Counter.build()
                .help("Exchange difi token OK.")
                .namespace(METRIC_PREFIX)
                .name("exchangeDifiTokenOk")
                .register(default)
        internal val exchangeDIFINotOk =
            Counter.build()
                .help("Exchange difi token failed pga unauthorized eller internal server error.")
                .namespace(METRIC_PREFIX)
                .name("exchangeDifiTokenNotOk")
                .register(default)
        internal val requestLatencyTokenExchangeDIFI =
            Histogram.build()
                .help("Request latency in seconds /token/exchangedifi.")
                .namespace(LATENCY_METRIC_PREFIX)
                .name("token_exchangedifi")
                .register(default)

        // path: **/token/exchange
        internal val exchangeSAMLTokenOk =
            Counter.build()
                .namespace(METRIC_PREFIX)
                .labelNames("srvbruker")
                .name("exchangeSamlTokenOk")
                .help("Exchange Saml to Oidc token OK.").register(default)
        internal val exchangeOIDCTokenOk =
            Counter.build()
                .namespace(METRIC_PREFIX)
                .labelNames("srvbruker")
                .name("exchangeOidcTokenOk")
                .help("Exchange Oidc token to Saml OK.").register(default)
        internal val exchangeTokenNotOk =
            Counter.build()
                .namespace(METRIC_PREFIX)
                .name("exchangeTokenNotOk")
                .help("Exchange token failed pga bad_request, unauthorized, invalid input token eller internal server error.")
                .register(default)
        internal val requestLatencyTokenExchange =
            Histogram.build()
                .help("Request latency in seconds /token/exchange.")
                .namespace(LATENCY_METRIC_PREFIX)
                .name("seconds_token_exchange")
                .register(default)

        // path: **/ws/samltoken
        internal val wsSAMLTokenOk =
            Counter.build()
                .namespace(METRIC_PREFIX)
                .name("wsSamlTokenOk")
                .help("WS issue saml token OK.").register(default)
        internal val wsSAMLTokenNotOk =
            Counter.build()
                .namespace(METRIC_PREFIX)
                .name("wsSamlTokenNotOk")
                .help("WS issue saml token failed pga unauthorized eller internal server error.").register(default)
        internal val wsExchangeOIDCTokenNotOk =
            Counter.build()
                .namespace(METRIC_PREFIX)
                .name("wsExchangeOidcTokenNotOk")
                .help("WS exchange oidc token failed pga unauthorized eller internal server error.").register(default)
        internal val wsExchangeOIDCTokenOk =
            Counter.build()
                .namespace(METRIC_PREFIX)
                .name("wsExchangeOidcTokenOk")
                .help("WS exchange oidc token OK.").register(default)
        internal val requestLatencyWSSAMLToken =
            Histogram.build()
                .help("Request latency in seconds /ws/samltoken.")
                .namespace(LATENCY_METRIC_PREFIX)
                .name("seconds_ws_samltoken")
                .register(default)

        // Cert
        internal val certCount: Counter =
            Counter.build()
                .help("Count days until expiry.")
                .namespace("keystore")
                .labelNames("key_alias")
                .name("cert_count")
                .register(default)

        // Ldap
        internal val ldapDuration: Histogram =
            Histogram.build()
                .help("AD - time for checking.")
                .namespace(LATENCY_METRIC_PREFIX)
                .name("ldap")
                .register(default)
    }
}
