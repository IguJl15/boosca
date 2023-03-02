package buscador

import org.jsoup.Jsoup
import org.jsoup.select.Evaluator
import java.net.URI

class Finder {
    private val httpClient = HttpClientProvider

    private var foundResults = mutableMapOf<String, FoundPage>()

    fun find(url: String, queryString: String, depth: Int): List<Result>? {
        val response = httpClient.get(URI(url))

        if (response == null) {
            println("Requisição para $url falhou.")
            return emptyList()
        }

        //                       response data . html body element
        val html = Jsoup.parse(response.body()).body()
        val results = mutableListOf<Result>()

        val elements = html.select(Evaluator.Matches(queryString.toPattern()))
        println("Elementos encontrados: ${elements.count()}")
        results.addAll(elements.map {
            val elementText = it.text()

            val text = queryString.toRegex().find(elementText)!!.value
            val context = """[^\.]{0,50}$queryString.{0,50}""".toRegex().find(elementText)!!.value

            Result(text, context, url)
        })

        return results.distinctBy { it.context }

        if (depth > 0) {
            val links = html.select("a").map { it.attr("href") }.filter { !it.isNullOrEmpty() }

            for (link in links) {
                results.addAll(
                    find(link, queryString, depth - 1) ?: emptyList()
                )
            }
        }


    }
}