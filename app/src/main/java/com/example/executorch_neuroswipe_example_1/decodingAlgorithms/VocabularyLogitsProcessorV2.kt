package com.example.executorch_neuroswipe_example_1.decodingAlgorithms

import android.util.Log
import com.example.executorch_neuroswipe_example_1.tokenizers.RuSubwordTokenizer

class VocabularyLogitsProcessorV2(
    private val tokenizer: RuSubwordTokenizer,
    private val vocab: List<String>,
) : LogitsProcessor() {

    private data class TrieNode(
        val children: MutableMap<Int, TrieNode> = mutableMapOf()
    )

    private val root = TrieNode()

    init {
        buildTrie()
    }

    private fun buildTrie() {
        vocab.forEach { word ->
            val tokens = tokenizer.tokenize(word).toList()
            var currentNode = root
            for (token in tokens) {
                currentNode = currentNode.children.getOrPut(token) { TrieNode() }
            }
        }
    }

    override fun process(logits: FloatArray, inputIds: List<Int>): FloatArray {
        val allowedIds = traverseTrie(inputIds)
        logits.indices
            .filterNot { it in allowedIds }
            .forEach { logits[it] = Float.NEGATIVE_INFINITY }
        return logits
    }

    private fun traverseTrie(inputIds: List<Int>): Set<Int> {
        var currentNode = root
        for (token in inputIds) {
            currentNode = currentNode.children[token] ?: run {
                Log.w("VocabularyLogitsProcessor", "Traversal failed for token $token in inputIds $inputIds")
                return emptySet()
            }
        }
        return currentNode.children.keys
    }

}