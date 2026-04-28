package no.nav.gandalf.metric

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component

private const val METRIC_PREFIX = "securitytokenservice"
private const val LATENCY_METRIC_PREFIX = "requests_latency"

@Component
class ApplicationMetric(
    registry: MeterRegistry,
) {
    companion object {
        private lateinit var reg: MeterRegistry

        // path: **/token
        lateinit var tokenOK: Counter
        lateinit var tokenNotOk: Counter
        lateinit var tokenError: Counter
        lateinit var requestLatencyToken: Timer

        // path: **/token2
        lateinit var token2NotOk: Counter
        lateinit var token2Error: Counter
        lateinit var requestLatencyToken2: Timer

        // path: **/samltoken
        lateinit var samlTokenNotOk: Counter
        lateinit var samlTokenError: Counter
        lateinit var requestLatencySAMLToken: Timer

        // path: **/jwks
        lateinit var requestLatencyJwks: Timer

        // path: **/token/exchangedifi
        lateinit var exchangeDIFIOk: Counter
        lateinit var exchangeDIFINotOk: Counter
        lateinit var requestLatencyTokenExchangeDIFI: Timer

        // path: **/token/exchange
        lateinit var exchangeTokenNotOk: Counter
        lateinit var requestLatencyTokenExchange: Timer

        // path: **/ws/samltoken
        lateinit var wsSAMLTokenOk: Counter
        lateinit var wsSAMLTokenNotOk: Counter
        lateinit var wsExchangeOIDCTokenNotOk: Counter
        lateinit var wsExchangeOIDCTokenOk: Counter
        lateinit var requestLatencyWSSAMLToken: Timer

        // Ldap
        lateinit var ldapDuration: Timer

        /** Use for labeled (tagged) counters and cert metrics. */
        fun meterRegistry(): MeterRegistry = reg

        // Convenience for labeled counters
        fun issuedTokenCounterUnique(srvbruker: String): Counter =
            Counter
                .builder("${METRIC_PREFIX}_tokens_issued")
                .description("Number of tokens we have issued")
                .tag("srvbruker", srvbruker)
                .register(reg)

        fun token2Ok(srvbruker: String): Counter =
            Counter
                .builder("${METRIC_PREFIX}_oidctoken2Ok")
                .description("Issued OIDC tokens (grensesnitt for stormaskin) OK.")
                .tag("srvbruker", srvbruker)
                .register(reg)

        fun samlTokenOk(srvbruker: String): Counter =
            Counter
                .builder("${METRIC_PREFIX}_samlTokenOk")
                .description("Issued saml token OK.")
                .tag("srvbruker", srvbruker)
                .register(reg)

        fun exchangeSAMLTokenOk(srvbruker: String): Counter =
            Counter
                .builder("${METRIC_PREFIX}_exchangeSamlTokenOk")
                .description("Exchange Saml to Oidc token OK.")
                .tag("srvbruker", srvbruker)
                .register(reg)

        fun exchangeOIDCTokenOk(srvbruker: String): Counter =
            Counter
                .builder("${METRIC_PREFIX}_exchangeOidcTokenOk")
                .description("Exchange Oidc token to Saml OK.")
                .tag("srvbruker", srvbruker)
                .register(reg)

        fun certDaysRemaining(
            keyAlias: String,
            daysSupplier: () -> Double,
        ) {
            Gauge
                .builder("keystore_cert_days_remaining", daysSupplier)
                .description("Days until certificate expires.")
                .tag("key_alias", keyAlias)
                .register(reg)
        }
    }

    init {
        reg = registry

        // path: **/token
        tokenOK = Counter.builder("${METRIC_PREFIX}_oidctokenOk").description("Issued OIDC tokens OK.").register(registry)
        tokenNotOk = Counter.builder("${METRIC_PREFIX}_oidctokenNotOk").description("Issued OIDC tokens failed pga bad request or unauthorized.").register(registry)
        tokenError = Counter.builder("${METRIC_PREFIX}_oidctokenError").description("Issued OIDC tokens failed pga internal server error.").register(registry)
        requestLatencyToken = Timer.builder("${LATENCY_METRIC_PREFIX}_seconds_token").description("Request latency in seconds /token.").register(registry)

        // path: **/token2
        token2NotOk = Counter.builder("${METRIC_PREFIX}_oidctoken2NotOk").description("Issued OIDC tokens (grensesnitt for stormaskin) failed pga bad request or unauthorized.").register(registry)
        token2Error = Counter.builder("${METRIC_PREFIX}_oidctoken2Error").description("Issued OIDC tokens (grensesnitt for stormaskin) failed pga internal server error.").register(registry)
        requestLatencyToken2 = Timer.builder("${LATENCY_METRIC_PREFIX}_seconds_token_2").description("Request latency in seconds /token2.").register(registry)

        // path: **/samltoken
        samlTokenNotOk = Counter.builder("${METRIC_PREFIX}_samlTokenNotOk").description("Issue saml token failed pga unauthorized eller bad request error.").register(registry)
        samlTokenError = Counter.builder("${METRIC_PREFIX}_samlTokenError").description("Issued Saml tokens failed pga internal server error.").register(registry)
        requestLatencySAMLToken = Timer.builder("${LATENCY_METRIC_PREFIX}_seconds_samltoken").description("Request latency in seconds /samltoken.").register(registry)

        // path: **/jwks
        requestLatencyJwks = Timer.builder("${LATENCY_METRIC_PREFIX}_seconds_jwks").description("Request latency in seconds /jwks.").register(registry)

        // path: **/token/exchangedifi
        exchangeDIFIOk = Counter.builder("${METRIC_PREFIX}_exchangeDifiTokenOk").description("Exchange difi token OK.").register(registry)
        exchangeDIFINotOk = Counter.builder("${METRIC_PREFIX}_exchangeDifiTokenNotOk").description("Exchange difi token failed.").register(registry)
        requestLatencyTokenExchangeDIFI = Timer.builder("${LATENCY_METRIC_PREFIX}_token_exchangedifi").description("Request latency in seconds /token/exchangedifi.").register(registry)

        // path: **/token/exchange
        exchangeTokenNotOk = Counter.builder("${METRIC_PREFIX}_exchangeTokenNotOk").description("Exchange token failed.").register(registry)
        requestLatencyTokenExchange = Timer.builder("${LATENCY_METRIC_PREFIX}_seconds_token_exchange").description("Request latency in seconds /token/exchange.").register(registry)

        // path: **/ws/samltoken
        wsSAMLTokenOk = Counter.builder("${METRIC_PREFIX}_wsSamlTokenOk").description("WS issue saml token OK.").register(registry)
        wsSAMLTokenNotOk = Counter.builder("${METRIC_PREFIX}_wsSamlTokenNotOk").description("WS issue saml token failed.").register(registry)
        wsExchangeOIDCTokenNotOk = Counter.builder("${METRIC_PREFIX}_wsExchangeOidcTokenNotOk").description("WS exchange oidc token failed.").register(registry)
        wsExchangeOIDCTokenOk = Counter.builder("${METRIC_PREFIX}_wsExchangeOidcTokenOk").description("WS exchange oidc token OK.").register(registry)
        requestLatencyWSSAMLToken = Timer.builder("${LATENCY_METRIC_PREFIX}_seconds_ws_samltoken").description("Request latency in seconds /ws/samltoken.").register(registry)

        // Ldap
        ldapDuration = Timer.builder("${LATENCY_METRIC_PREFIX}_ldap").description("AD - time for checking.").register(registry)
    }
}
