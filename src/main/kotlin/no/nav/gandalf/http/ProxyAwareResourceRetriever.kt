package no.nav.gandalf.http

import com.nimbusds.jose.util.DefaultResourceRetriever
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI
import java.net.URISyntaxException
import java.net.URL

class ProxyAwareResourceRetriever internal constructor(
    proxyUrl: URL?,
    private val isUsePlainTextForHttps: Boolean,
    connectTimeout: Int,
    readTimeout: Int,
    sizeLimit: Int,
) :
    DefaultResourceRetriever(connectTimeout, readTimeout, sizeLimit) {

    @JvmOverloads
    constructor(proxyUrl: URL? = null, usePlainTextForHttps: Boolean = false) : this(
        proxyUrl,
        usePlainTextForHttps,
        DEFAULT_HTTP_CONNECT_TIMEOUT,
        DEFAULT_HTTP_READ_TIMEOUT,
        DEFAULT_HTTP_SIZE_LIMIT,
    ) {
    }

    @Throws(IOException::class)
    fun urlWithPlainTextForHttps(url: URL): URL {
        return try {
            val uri = url.toURI()
            if (uri.scheme != "https") {
                return url
            }
            val port = if (url.port > 0) url.port else 443
            val newUrl = (
                "http://" + uri.host + ":" + port + uri.path +
                    if (uri.query != null && uri.query.length > 0) "?" + uri.query else ""
                )
            logger.debug(
                "using plaintext connection for https url, new url is {}",
                newUrl,
            )
            URI.create(newUrl).toURL()
        } catch (e: URISyntaxException) {
            throw IOException(e)
        }
    }

    @Throws(IOException::class)
    override fun openHTTPConnection(url: URL): HttpURLConnection {
        val urlToOpen = if (isUsePlainTextForHttps) urlWithPlainTextForHttps(url) else url
        return super.openHTTPConnection(urlToOpen)
    }

    companion object {
        const val DEFAULT_HTTP_CONNECT_TIMEOUT = 21050
        const val DEFAULT_HTTP_READ_TIMEOUT = 30000
        const val DEFAULT_HTTP_SIZE_LIMIT = 50 * 1024
        private val logger =
            LoggerFactory.getLogger(ProxyAwareResourceRetriever::class.java)
    }

    init {
        if (proxyUrl != null) {
            proxy = Proxy(
                Proxy.Type.HTTP,
                InetSocketAddress(proxyUrl.host, proxyUrl.port),
            )
        }
    }
}
