package io.github.proshian.neuralswipetyping.tokenizers

import kotlinx.serialization.Serializable

@Serializable
sealed interface TokenMapsContainer {
    val idToToken: Map<Int, String>
    val tokenToId: Map<String, Int> get() = idToToken.entries.associate { (id, token) -> token to id }
}

@Serializable
class KeyboardTokenizer(
    override val idToToken: Map<Int, String>
) : TokenMapsContainer

@Serializable
class WordTokenizer(
    override val idToToken: Map<Int, String>,
    private val specialTokenIdsNullable: IntArray? = null
) : TokenMapsContainer {

    override val tokenToId: Map<String, Int> by lazy { super.tokenToId }

    public val sosTokenId: Int = tokenToId["<sos>"]
        ?: throw IllegalArgumentException("<sos> token must be defined")
    public val eosTokenId: Int = tokenToId["<eos>"]
        ?: throw IllegalArgumentException("<eos> token must be defined")
    public val unkTokenId: Int? = tokenToId["<unk>"]

    private val specialTokenIds = specialTokenIdsNullable ?: intArrayOf(sosTokenId, eosTokenId)
    init {
        val mandatorySpecialTokens = listOf("<sos>", "<eos>")
        for (token in mandatorySpecialTokens) {
            val tokenId = tokenToId[token] ?: error("$token not in tokenToId")
            require(tokenId in specialTokenIds) { "$token token must be in special tokens" }
        }
    }

    fun tokenize(str: String, addSpecialTokens: Boolean = true): IntArray {
        val tokens = str.map { char ->
            tokenToId[char.toString()]
                ?: unkTokenId ?: error("Unknown character '$char' is met and no <unk> token is present")
        }
        return if (addSpecialTokens) {
            intArrayOf(sosTokenId) + tokens + intArrayOf(eosTokenId)
        } else {
            tokens.toIntArray()
        }
    }

    fun detokenize(ids: IntArray, avoidSpecialTokens: Boolean = true): String {
        return ids
            .filterNot{avoidSpecialTokens && (it in specialTokenIds)}
            .map{ idToToken[it] }
            .joinToString(separator = "")
    }
}
