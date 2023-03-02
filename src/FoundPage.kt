package buscador

class FoundPage(
    val url: String,
    val queryResults: List<MatchText>
) {
    var appearsOnAllPages = false
    private var count: Int = 1
        get() = field

    private val pointsTo = mutableSetOf<FoundPage>()
        get() = mutableSetOf<FoundPage>().apply { addAll(field) }

    private val pointedBy = mutableSetOf<FoundPage>()
        get() = mutableSetOf<FoundPage>().apply { addAll(field) }

    fun incrementCount() = count++
    fun pointTo(page: FoundPage) = pointsTo.add(page)
    fun pointedBy(page: FoundPage) = pointedBy.add(page)
}