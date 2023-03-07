package buscador

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Evaluator
import java.beans.Encoder
import java.net.URI
import java.net.URISyntaxException
import java.net.URLEncoder
import java.nio.charset.Charset

class Finder(val rootUrl: String, val queryString: String) {
    private val httpClient = HttpClientProvider
    private val contextRegex = Regex("""([^.]{0,50})($queryString)(.{0,50})""", RegexOption.IGNORE_CASE)

    val pages = mutableMapOf<String, Page>()
    fun pagesCount() = pages.size

    private fun foundedPage(page: Page) {
        if (page.url in pages) pages[page.url]!!.incrementCount()
        else pages[page.url] = page
    }

    /**
     * Returns the root page representing the first page (url). See [Page] to more details
     */
    fun find(url: String = rootUrl, depth: Int): Page? {
        val response = httpClient.get(URI(url))

        if (response == null) {
            App.printLoud("Requisição para $url falhou.")
            return null
        }

        val html = Jsoup.parse(response.body(), url)

        val matchTexts = findMatchesInPage(html.body())

        val page = Page(url, html.title(), matchTexts)
        if (url == rootUrl) foundedPage(page)

        val links = getLinks(html.body())

        val canSearchMore = depth > 0
        for (link in links) {
            val wasVisitedBefore = link in pages

            val pageFounded: Page = if (canSearchMore && !wasVisitedBefore) {
                find(link, depth - 1) ?: continue
            } else {
                Page.empty(link)
            }

            page.pagesInside.add(pageFounded)
            foundedPage(pageFounded)
        }

        return page
    }

    private fun getLinks(html: Element): List<String> {
        return html.select("a")
            .map {
                if(it.attr("href").startsWith("#")) {
                    null
                } else it.absUrl("href")
            }
            .filterNotNull()
    }

    private fun findMatchesInPage(html: Element): ArrayList<MatchText> {
        val elements = html.select(Evaluator.MatchesOwn(queryString.toPattern()))

        val results = arrayListOf<MatchText>()

        elements.mapTo(results) {
            val elementText = it.text()

            val match = contextRegex.find(elementText)
            assert(match!!.groupValues.size >= 3)

            MatchText.fromRegexMatch(match)
        }

        return results
    }
}