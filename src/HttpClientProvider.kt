package buscador

import java.lang.IllegalArgumentException
import java.net.CacheResponse
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.ConnectException
import java.net.URISyntaxException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


object HttpClientProvider {
    private val client = HttpClient.newBuilder().apply {
        if (App.insecure) sslContext(getInsecureSSLContext())
    }.build()
    private val defaultHeaders = mapOf<String, String>(
        "User-Agent" to "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36"
    )

    fun get(uri: URI): HttpResponse<String>? {
        App.printLoud("Realizando requisição para \"${uri}\"")

        val request = try {
            HttpRequest.newBuilder(uri)
                .apply { defaultHeaders.forEach { header(it.key, it.value) } }
                .build()
        } catch (e: IllegalArgumentException) {
            return null
        }

        val startTime = System.currentTimeMillis()
        val response = try {
            client.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (e: ConnectException) {
            App.printLoud("Houve um erro de conexão durante a requisição a URL. Confira sua conexão com a internet e tente novamente.")
            App.printLoud("Utilize --verbose para visualizar mais detalhes acerca do problema")
            if (App.verbose) throw e
            return null
        } catch (e: URISyntaxException) {
            App.printLoud("Link inválido:")
            return null
        }
        val endTime = System.currentTimeMillis()

        val elapsedTime = endTime - startTime

        val statusCode = response.statusCode()
        App.printResponseDetails(response, request, elapsedTime)

        if (statusCode !in 200 until 300) {
            if (!App.verbose) App.printLoud(
                "Houve algo de errado durante a requisição. " +
                        "Tente novamente utilizando o argumento '--verbose' para visualizar mais detalhes."
            )
            return null
        }

        return response
    }

    private fun getInsecureSSLContext(): SSLContext? {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate>? = null
            override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
        })

        val sslContext: SSLContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())

        return sslContext
    }
}
