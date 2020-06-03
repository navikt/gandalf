package no.nav.gandalf.api.metric

import io.prometheus.client.Counter
import io.prometheus.client.Histogram

object ApplicationMetric {

    // path: **/token
    internal val tokenOK = Counter.build()
        .help("Issued OIDC tokens OK.")
        .namespace("securitytokenservice")
        .name("_oidctokenOk")
        .register()

    internal val tokenNotOk = Counter.build()
        .help("Issued OIDC tokens failed pga bad request or unauthorized.")
        .namespace("securitytokenservice")
        .name("_oidctokenNotOk")
        .register()

    internal val tokenError = Counter.build()
        .help("Issued OIDC tokens failed pga internal server error.")
        .namespace("securitytokenservice")
        .name("_oidctokenError")
        .register()

    internal val requestLatencyToken = Histogram.build()
        .help("Request latency in seconds /token.")
        .namespace("requests_latency")
        .name("_seconds_token")
        .register()

    // path: **/token2
    internal val token2Ok = Counter.build()
        .help("Issued OIDC tokens (grensesnitt for stormaskin) OK.")
        .namespace("securitytokenservice")
        .name("_oidctoken2Ok")
        .register()

    internal val token2NotOk = Counter.build()
        .help("Issued OIDC tokens (grensesnitt for stormaskin) failed pga bad request or unauthorized.")
        .namespace("securitytokenservice")
        .name("_oidctoken2NotOk")
        .register()

    internal val token2Error = Counter.build()
        .help("Issued OIDC tokens (grensesnitt for stormaskin) failed pga internal server error.")
        .namespace("securitytokenservice")
        .name("_oidctoken2Error")
        .register()

    internal val requestLatencyToken2 = Histogram.build()
        .help("Request latency in seconds /token2.")
        .namespace("requests_latency")
        .name("_seconds_token_2")
        .register()

    // path: **/samltoken
    internal val samlTokenOk = Counter.build()
        .help("Issued saml token OK.")
        .namespace("securitytokenservice")
        .name("_samlTokenOk")
        .register()

    internal val samlTokenNotOk = Counter.build()
        .help("Issue saml token failed pga unauthorized eller bad request error.")
        .namespace("securitytokenservice")
        .name("_samlTokenNotOk")
        .register()

    internal val samlTokenError = Counter.build()
        .help("Issued Saml tokens failed pga internal server error.")
        .namespace("securitytokenservice")
        .name("_samlTokenError")
        .register()

    internal val requestLatencySAMLToken = Histogram.build()
        .help("Request latency in seconds /samltoken.")
        .namespace("requests_latency")
        .name("_seconds_samltoken")
        .register()

    // path: **/jwks
    internal val requestLatencyJwks = Histogram.build()
        .help("Request latency in seconds /jwks.")
        .namespace("requests_latency")
        .name("_seconds_jwks")
        .register()

    // path: **/token/exchangedifi
    internal val exchangeDIFIOk = Counter.build()
        .help("Exchange difi token OK.")
        .namespace("securitytokenservice")
        .name("_exchangeDifiTokenOk")
        .register()

    internal val exchangeDIFINotOk = Counter.build()
        .help("Exchange difi token failed pga unauthorized eller internal server error.")
        .namespace("securitytokenservice")
        .name("_exchangeDifiTokenNotOk")
        .register()

    internal val requestLatencyTokenExchangeDIFI = Histogram.build()
        .help("Request latency in seconds /token/exchangedifi.")
        .namespace("requests_latency")
        .name("_token_exchangedifi")
        .register()

    // path: **/token/exchange
    internal val exchangeSAMLTokenOk = Counter.build()
        .namespace("securitytokenservice")
        .name("_exchangeSamlTokenOk")
        .help("Exchange Saml to Oidc token OK.").register()

    internal val exchangeOIDCTokenOk = Counter.build()
        .namespace("securitytokenservice")
        .name("_exchangeOidcTokenOk")
        .help("Exchange Oidc token to Saml OK.").register()

    internal val exchangeTokenNotOk = Counter.build()
        .namespace("securitytokenservice")
        .name("_exchangeTokenNotOk")
        .help("Exchange token failed pga bad_request, unauthorized, invalid input token eller internal server error.")
        .register()

    internal val requestLatencyTokenExchange = Histogram.build()
        .help("Request latency in seconds /token/exchange.")
        .namespace("requests_latency")
        .name("_seconds_token_exchange")
        .register()

    // path: **/ws/samltoken
    internal val wsSAMLTokenOk = Counter.build()
        .namespace("securitytokenservice")
        .name("_wsSamlTokenOk")
        .help("WS issue saml token OK.").register()

    internal val wsSAMLTokenNotOk = Counter.build()
        .namespace("securitytokenservice")
        .name("_wsSamlTokenNotOk")
        .help("WS issue saml token failed pga unauthorized eller internal server error.").register()

    internal val wsExchangeOIDCTokenNotOk = Counter.build()
        .namespace("securitytokenservice")
        .name("_wsExchangeOidcTokenNotOk")
        .help("WS exchange oidc token failed pga unauthorized eller internal server error.").register()

    internal val wsExchangeOIDCTokenOk = Counter.build()
        .namespace("securitytokenservice")
        .name("_wsExchangeOidcTokenOk")
        .help("WS exchange oidc token OK.").register()

    internal val requestLatencyWSSAMLToken = Histogram.build()
        .help("Request latency in seconds /ws/samltoken.")
        .namespace("requests_latency")
        .name("_seconds_ws_samltoken")
        .register()
}
