package buscador

data class MatchText(
    val beforeContext: String,
    val term: String,
    val afterContext: String
) {
    companion object {
        /**
         * build a [MatchText] from a regex match. Assert that the match has 3 different groups:
         * 1. Before context: text that comes before the found term
         * 2. Term: the found term
         * 3. After context: text that comes after the found term
         */
        fun fromRegexMatch(match: MatchResult) =
            MatchText(match.groupValues[1], match.groupValues[2], match.groupValues.last())

    }

    constructor(term: String) : this("", term, "")

    override fun toString(): String {
        return "$beforeContext$term$afterContext"
    }
}