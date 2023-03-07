package buscador

import java.net.http.HttpRequest
import java.net.http.HttpResponse
import buscador.Validator.isAValidUrl

object App {
    var verbose = false
    var silent = false
    var insecure = false
    var insensitive = false

    fun parseFlags(options: Collection<String>) {
        if ("--verbose" in options || "-v" in options) this.verbose = true
        if ("--silent" in options || "-s" in options) this.silent = true
        if ("--insensitive" in options || "-i" in options) this.insensitive = true
        if ("--insecure" in options) this.insecure = true
    }

    fun printVerbose(message: Any?) {
        if (verbose) println(message)
    }

    fun printHelp() {
        val help = """
            
Usage: buscador [OPTION] URL EXPRESSION DEPTH

Options:
-v, --verbose       Imprime mais detalhes durante a operação
-i, --insensitive   Case insensitive match
-h, --help      Imprime este texto de ajuda
        """.trimIndent()
        println(help)
    }

    fun String.truncOrPad(width: Int): String {
        return if (length > width) subSequence(0, width).toString()
        else padEnd(width)
    }

    fun printResponseDetails(response: HttpResponse<String>, request: HttpRequest, elapsedTime: Long) {
        printVerbose(
            """
Tempo da requisição: ${elapsedTime}ms
Código de status: ${response.statusCode()}
        """.trimIndent()
//
//Cabeçalho da Requisição:
//${request.headers()}
//
//Cabeçalho da Resposta:
//${response.headers()}
        )
    }

    fun getRegexOptions(): Set<RegexOption> {
        return mutableSetOf<RegexOption>(
        ).apply {
            if (insensitive) add(RegexOption.IGNORE_CASE)
        }
    }

    fun printLoud(message: Any) {
        if(!silent) println(message)
    }
}

fun main(args: Array<String>) {
    if (args.isEmpty() || "-h" in args || "--help" in args) {
        App.printHelp()
        return
    }

    val flags = args.filter { it.startsWith('-') }.toSet()
    App.parseFlags(flags)

    val arguments = args.subtract(flags).toList()
    if (arguments.size != 3) {
        println("Invalid arguments count. Should be 3, found: ${arguments.size}.")
        return
    }

    val url = arguments.first()
    val query = arguments[1]
    val depth = arguments.last().toIntOrNull()

    if (url.isNullOrEmpty() || !url.isAValidUrl()) {
        println("Invalid URL. Use --help to see more information.")
        return
    }
    if (query.isNullOrEmpty()) {
        println("Invalid query. Use --help to see more information.")
        return
    }
    if (depth == null) {
        println("Invalid depth number. Use --help to see more information.")
        return
    }

    run(url, query, depth)
}

fun run(initialUrl: String, query: String, depth: Int) {
    val finder = Finder(initialUrl, query)

    val rootPageResult = finder.find(finder.rootUrl, depth)

    App.printVerbose("Total de páginas escaneadas: ${finder.pagesCount()}")

    if (finder.pagesCount() == 0) return

    val sanitized = finder.pages.values
        .sortedBy { it.count }
        .filter { it.matches.isNotEmpty() }


    println("Resultados (${sanitized.size}): ")

    sanitized.forEach {
        prettyPrintResult(it)
    }
}

fun prettyPrintResult(page: Page) {
    println(
        """
            ├┬▸ ${page.title}
            │├➤ ${page.url}
            │└➤ ${page.matches.firstOrNull()?.toString() ?: ""}
            │
            """.trimIndent()
    )
}