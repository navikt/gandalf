package no.nav.gandalf.accesstoken

import mu.KotlinLogging
import org.apache.http.HttpHost
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.DefaultProxyRoutePlanner
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger { }

// TODO webproxy var
val webroxy = "webproxy.local:8088"

@Component
class HttpClient {

    // Setup webproxy
    private val httpClient: CloseableHttpClient =
            HttpClientBuilder
                    .create()
                    .build()

    private val proxyHttpClient: CloseableHttpClient =
            HttpClients.custom()
                    .setRoutePlanner(
                            DefaultProxyRoutePlanner(
                                    HttpHost(webroxy.substringBefore(":"), webroxy.substringAfter(":").toInt())
                            )
                    ).build()

    fun get(endpoint: String, proxy: Boolean): String {
        val get = HttpGet(endpoint)
        log.info("Making HTTP request " + get.method + " " + get.uri)
        return clientType(proxy).execute(get).entity.toString()
    }

    fun clientType(proxy: Boolean) = when {
        proxy -> proxyHttpClient
        else -> httpClient
    }
}