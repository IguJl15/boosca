package buscador

import java.net.http.HttpRequest
import java.net.http.HttpResponse
import buscador.Validator.isAValidUrl

object App {
    var verbose = false
    var insecure = false

    fun parseFlags(options: Collection<String>) {
        if ("--verbose" in options || "-v" in options) this.verbose = true
        if ("--insecure" in options || "-i" in options) this.insecure = true
    }

    fun printVerbose(message: Any?) {
        if (verbose) println(message)
    }

    fun printHelp() {
        val help = """
            
Usage: buscador [OPTION] URL EXPRESSION DEPTH

Options:
-v, --verbose   Imprime mais detalhes sobre a operação
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

Cabeçalho da Requisição:
${request.headers()}

Cabeçalho da Resposta:
${response.headers()}
        """.trimIndent()
        )
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
    val depth = arguments.last()

    if (url.isNullOrEmpty() || !url.isAValidUrl()) {
        println("Invalid URL. Use --help to see more information.")
        return
    }
    if (query.isNullOrEmpty()) {
        println("Invalid query. Use --help to see more information.")
        return
    }
    if (depth.isNullOrEmpty() || depth.toIntOrNull() == null) {
        println("Invalid depth number. Use --help to see more information.")
        return
    }

    val finder = Finder()

    val results = finder.find(url, query, depth.toInt())

    println("Resultados (${results?.size ?: 0}): ")

    results?.forEach {
        println()
        println(it.title)
        println(it.link)
        println(it.context)
    }

}