package com.example.executorch_neuroswipe_example_1.decodingAlgorithms

import android.util.Log
import com.example.executorch_neuroswipe_example_1.tokenizers.RuSubwordTokenizer

class VocabularyLogitsProcessorV4(
    private val tokenizer: RuSubwordTokenizer,
    private val vocab: List<String>,
    private val maxTokenId: Int
) : LogitsProcessor() {

    private inner class DawgNode(
        val children: MutableMap<Int, DawgNode> = mutableMapOf()
    ) {
        // Generates a key based on sorted children entries for registry comparison
        fun key(): List<Pair<Int, DawgNode>> {
            return children.toList().sortedBy { it.first }
        }
    }

    private val root = DawgNode()
    private val registry = mutableMapOf<List<Pair<Int, DawgNode>>, DawgNode>()

    init {
        buildDawg()
    }

    private fun buildDawg() {
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
                // Check if current node has the token; if not, create or reuse a node
                if (!currentNode.children.containsKey(token)) {
                    val newNode = DawgNode()
                    val newNodeKey = newNode.key()
                    val existingNode = registry[newNodeKey]
                    if (existingNode != null) {
                        currentNode.children[token] = existingNode
                    } else {
                        registry[newNodeKey] = newNode
                        currentNode.children[token] = newNode
                    }
                }
                currentNode = currentNode.children[token]!!
                // Check if the current node can be replaced with a node from the registry
                val currentKey = currentNode.key()
                val existing = registry[currentKey]
                if (existing != null && existing != currentNode) {
                    currentNode.children.forEach { (t, node) ->
                        existing.children.getOrPut(t) { node }
                    }
                    currentNode = existing
                } else {
                    registry[currentKey] = currentNode
                }
            }
        }
    }

    override fun process(logits: FloatArray, inputIds: List<Int>): FloatArray {
        val allowedIds = traverseDawg(inputIds)
        logits.indices
            .filterNot { it in allowedIds }
            .forEach { logits[it] = Float.NEGATIVE_INFINITY }
        return logits
    }

    private fun traverseDawg(inputIds: List<Int>): Set<Int> {
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
        return currentNode.children.keys.toSet()
    }
}