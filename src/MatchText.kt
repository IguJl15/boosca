package buscador

class MatchText(
    val beforeContext: String,
    val term: String,
    val afterContext: String
) {
    constructor(term: String) : this("", term, "")
}