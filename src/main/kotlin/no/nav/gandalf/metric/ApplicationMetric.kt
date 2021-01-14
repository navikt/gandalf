package no.nav.gandalf.metric

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter
import io.prometheus.client.Histogram
import org.springframework.stereotype.Component

private const val metricPrefix = "securitytokenservice"
private const val latencyMetricPrefix = "requests_latency"

@Component
class ApplicationMetric {
    // path: **/token
    companion object {
        private val default = CollectorRegistry.defaultRegistry
        internal val tokenOK = Counter.build()
            .help("Issued OIDC tokens OK.")
            .namespace(metricPrefix)
            .name("oidctokenOk")
            .register(default)
        internal val issuedTokenCounterUnique = Counter.build()
            .namespace(metricPrefix)
            .name("tokens_issued")
            .help("Number of tokens we have issued")
            .labelNames("srvbruker")
            .register(default)
        internal val tokenNotOk = Counter.build()
            .help("Issued OIDC tokens failed pga bad request or unauthorized.")
            .namespace(metricPrefix)
            .name("oidctokenNotOk")
            .register(default)
        internal val tokenError = Counter.build()
            .help("Issued OIDC tokens failed pga internal server error.")
            .namespace(metricPrefix)
            .name("oidctokenError")
            .register(default)
        internal val requestLatencyToken = Histogram.build()
            .help("Request latency in seconds /token.")
            .namespace(latencyMetricPrefix)
            .name("seconds_token")
            .register(default)

        // path: **/token2
        internal val token2Ok = Counter.build()
            .help("Issued OIDC tokens (grensesnitt for stormaskin) OK.")
            .namespace(metricPrefix)
            .labelNames("srvbruker")
            .name("oidctoken2Ok")
            .register(default)
        internal val token2NotOk = Counter.build()
            .help("Issued OIDC tokens (grensesnitt for stormaskin) failed pga bad request or unauthorized.")
            .namespace(metricPrefix)
            .name("oidctoken2NotOk")
            .register(default)
        internal val token2Error = Counter.build()
            .help("Issued OIDC tokens (grensesnitt for stormaskin) failed pga internal server error.")
            .namespace(metricPrefix)
            .name("oidctoken2Error")
            .register(default)
        internal val requestLatencyToken2 = Histogram.build()
            .help("Request latency in seconds /token2.")
            .namespace(latencyMetricPrefix)
            .name("seconds_token_2")
            .register(default)

        // path: **/samltoken
        internal val samlTokenOk = Counter.build()
            .help("Issued saml token OK.")
            .labelNames("srvbruker")
            .namespace(metricPrefix)
            .name("samlTokenOk")
            .register(default)
        internal val samlTokenNotOk = Counter.build()
            .help("Issue saml token failed pga unauthorized eller bad request error.")
            .namespace(metricPrefix)
            .name("samlTokenNotOk")
            .register(default)
        internal val samlTokenError = Counter.build()
            .help("Issued Saml tokens failed pga internal server error.")
            .namespace(metricPrefix)
            .name("samlTokenError")
            .register(default)
        internal val requestLatencySAMLToken = Histogram.build()
            .help("Request latency in seconds /samltoken.")
            .namespace(latencyMetricPrefix)
            .name("seconds_samltoken")
            .register(default)

        // path: **/jwks
        internal val requestLatencyJwks = Histogram.build()
            .help("Request latency in seconds /jwks.")
            .namespace(latencyMetricPrefix)
            .name("seconds_jwks")
            .register(default)

        // path: **/token/exchangedifi
        internal val exchangeDIFIOk = Counter.build()
            .help("Exchange difi token OK.")
            .namespace(metricPrefix)
            .name("exchangeDifiTokenOk")
            .register(default)
        internal val exchangeDIFINotOk = Counter.build()
            .help("Exchange difi token failed pga unauthorized eller internal server error.")
            .namespace(metricPrefix)
            .name("exchangeDifiTokenNotOk")
            .register(default)
        internal val requestLatencyTokenExchangeDIFI = Histogram.build()
            .help("Request latency in seconds /token/exchangedifi.")
            .namespace(latencyMetricPrefix)
            .name("token_exchangedifi")
            .register(default)

        // path: **/token/exchange
        internal val exchangeSAMLTokenOk = Counter.build()
            .namespace(metricPrefix)
            .labelNames("srvbruker")
            .name("exchangeSamlTokenOk")
            .help("Exchange Saml to Oidc token OK.").register(default)
        internal val exchangeOIDCTokenOk = Counter.build()
            .namespace(metricPrefix)
            .labelNames("srvbruker")
            .name("exchangeOidcTokenOk")
            .help("Exchange Oidc token to Saml OK.").register(default)
        internal val exchangeTokenNotOk = Counter.build()
            .namespace(metricPrefix)
            .name("exchangeTokenNotOk")
            .help("Exchange token failed pga bad_request, unauthorized, invalid input token eller internal server error.")
            .register(default)
        internal val requestLatencyTokenExchange = Histogram.build()
            .help("Request latency in seconds /token/exchange.")
            .namespace(latencyMetricPrefix)
            .name("seconds_token_exchange")
            .register(default)

        // path: **/ws/samltoken
        internal val wsSAMLTokenOk = Counter.build()
            .namespace(metricPrefix)
            .name("wsSamlTokenOk")
            .help("WS issue saml token OK.").register(default)
        internal val wsSAMLTokenNotOk = Counter.build()
            .namespace(metricPrefix)
            .name("wsSamlTokenNotOk")
            .help("WS issue saml token failed pga unauthorized eller internal server error.").register(default)
        internal val wsExchangeOIDCTokenNotOk = Counter.build()
            .namespace(metricPrefix)
            .name("wsExchangeOidcTokenNotOk")
            .help("WS exchange oidc token failed pga unauthorized eller internal server error.").register(default)
        internal val wsExchangeOIDCTokenOk = Counter.build()
            .namespace(metricPrefix)
            .name("wsExchangeOidcTokenOk")
            .help("WS exchange oidc token OK.").register(default)
        internal val requestLatencyWSSAMLToken = Histogram.build()
            .help("Request latency in seconds /ws/samltoken.")
            .namespace(latencyMetricPrefix)
            .name("seconds_ws_samltoken")
            .register(default)
        // Cert
        internal val certCount: Counter = Counter.build()
            .help("Count days until expiry.")
            .namespace("keystore")
            .labelNames("key_alias")
            .name("cert_count")
            .register(default)
        // Ldap
        internal val ldapDuration: Histogram = Histogram.build()
            .help("AD - time for checking.")
            .namespace(latencyMetricPrefix)
            .name("ldap")
            .register(default)
    }
}
