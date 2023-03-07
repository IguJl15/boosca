package buscador

class Page(
    val url: String,
    val title: String = "Not visited",
    val matches: List<MatchText> = listOf(),
) {
    val pagesInside: ArrayList<Page> = ArrayList()

    var count: Int = 1
        private set

    fun incrementCount(): Unit {
        count += 1
    }

    companion object {
        fun empty(link: String): Page = Page(link)
    }
}