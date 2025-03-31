package com.example.neuralSwipeKeyboardProject.logitsProcessors

import android.util.Log
import com.example.neuralSwipeKeyboardProject.tokenizers.RuSubwordTokenizer

class VocabularyLogitsProcessorV3(
    private val tokenizer: RuSubwordTokenizer,
    private val vocab: List<String>,
    private val maxTokenId: Int
) : LogitsProcessor() {

    // Mark TrieNode as inner to access maxTokenId from outer class
    private inner class TrieNode(
        val children: Array<TrieNode?> = Array(maxTokenId + 1) { null }
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
                if (token == tokenizer.tokenToId["<sos>"]) {
                    continue
                }
                require(token <= maxTokenId) {
                    "Token $token in word '$word' exceeds maxTokenId $maxTokenId"
                }
                if (currentNode.children[token] == null) {
                    currentNode.children[token] = TrieNode()
                }
                currentNode = currentNode.children[token]!!
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
            if (token == tokenizer.tokenToId["<sos>"]) {
                continue
            }
            if (token !in 0..maxTokenId) {
                Log.w("VocabularyLogitsProcessor", "Token $token out of bounds [0, $maxTokenId]")
                return emptySet()
            }
            currentNode = currentNode.children[token] ?: run {
                Log.w("VocabularyLogitsProcessor", "Traversal failed for token $token")
                return emptySet()
            }
        }
        return currentNode.children
            .withIndex()
            .filter { it.value != null }
            .map { it.index }
            .toSet()
    }
}