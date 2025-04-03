package com.example.trie_builder.tokenizers

interface TokenMapsContainer {
    val idToToken: Map<Int, String>
    val tokenToId: Map<String, Int>
}

interface StringTokenizer {
    fun tokenize(str: String): IntArray
    fun detokenize(ids: IntArray): String
}



class RuSubwordTokenizer: TokenMapsContainer, StringTokenizer  {
    override val idToToken = mapOf(
        0 to "-",
        1 to "а",
        2 to "б",
        3 to "в",
        4 to "г",
        5 to "д",
        6 to "е",
        7 to "ж",
        8 to "з",
        9 to "и",
        10 to "й",
        11 to "к",
        12 to "л",
        13 to "м",
        14 to "н",
        15 to "о",
        16 to "п",
        17 to "р",
        18 to "с",
        19 to "т",
        20 to "у",
        21 to "ф",
        22 to "х",
        23 to "ц",
        24 to "ч",
        25 to "ш",
        26 to "щ",
        27 to "ъ",
        28 to "ы",
        29 to "ь",
        30 to "э",
        31 to "ю",
        32 to "я",
        33 to "<eos>",
        34 to "<unk>",
        35 to "<pad>",
        36 to "<sos>",
    )

    override val tokenToId = idToToken.map { (k, v) -> v to k }.toMap()


    override fun tokenize(str: String): IntArray {
        val tokens = mutableListOf<Int>()
        str.forEach { tokens.add(tokenToId[it.toString()] ?: tokenToId["<unk>"]!!)}
        val tokensWithSpecials = listOf(tokenToId["<sos>"]!!) + tokens + listOf(tokenToId["<eos>"]!!)
        return tokensWithSpecials.toIntArray()
    }

    override fun detokenize(ids: IntArray): String {
        return ids.filter{it < 33 }.map{ idToToken[it] }.joinToString(separator = "")
    }
}


class KeyboardKeyTokenizer: TokenMapsContainer  {
    private val idToTokenArr = charArrayOf(
        'а', 'б', 'в', 'г', 'д', 'е', 'ë', 'ж', 'з', 'и', 'й',
        'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф',
        'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я')

    override val idToToken = idToTokenArr.indices.associateWith { idToTokenArr[it].toString() }

    override val tokenToId: Map<String, Int> = idToToken.map { (k, v) -> v to k }.toMap()
}