package com.example.executorch_neuroswipe_example_1.decodingAlgorithms

import android.util.Log
import com.example.executorch_neuroswipe_example_1.tokenizers.RuSubwordTokenizer

class VocabularyLogitsProcessor(
    private val tokenizer: RuSubwordTokenizer,
    private val vocab: List<String>,
    private val maxTokenId: Int
) : LogitsProcessor() {

    private val prefixToAllowedIds: Map<List<Int>, Set<Int>> = createPrefixToAllowedIds()

    private fun createPrefixToAllowedIds(): Map<List<Int>, Set<Int>> {
        val map = mutableMapOf<List<Int>, MutableSet<Int>>()
        vocab.forEach { word ->
            val tokens = tokenizer.tokenize(word).toList()
            for (i in 1 until tokens.size) {
                val prefix = tokens.subList(0, i)
                val allowed = map.getOrPut(prefix) { mutableSetOf() }
                allowed.add(tokens[i])
            }
        }

        return map
    }

    override fun process(logits: FloatArray, inputIds: List<Int>): FloatArray {
        val allowedIds = prefixToAllowedIds[inputIds] ?: run {
            Log.w("", "empty allowed ids for prefix ${inputIds.joinToString(", ")}")
            emptySet<Int>()
        }
        val impossibleIds = (maxTokenId + 1 until tokenizer.idToToken.size).toSet()
        (logits.indices)
            .filterNot { it in allowedIds || it in impossibleIds }
            .forEach { logits[it] = Float.NEGATIVE_INFINITY }
        return logits
    }
}