package com.example.neuralSwipeKeyboardProject.logitsProcessors

import android.util.Log
import com.example.neuralSwipeKeyboardProject.tokenizers.RuSubwordTokenizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

class VocabularyLogitsProcessorTrieBased(
    private val tokenizer: RuSubwordTokenizer,
    private val vocab: List<String>,
) : LogitsProcessor() {

    private data class TrieNode(
        val children: MutableMap<Int, TrieNode> = mutableMapOf()
    )

    private val root: AtomicReference<TrieNode?> = AtomicReference(null)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            root.set(buildTrie())
        }
    }

    private suspend fun buildTrie(): TrieNode{
        val futureTrieRoot = TrieNode()
        vocab.forEach { word ->
            val tokens = tokenizer.tokenize(word).toList()
            var currentNode = futureTrieRoot
            for (token in tokens) {
                currentNode = currentNode.children.getOrPut(token) { TrieNode() }
            }
        }
        return futureTrieRoot
    }

    override fun process(logits: FloatArray, inputIds: List<Int>): FloatArray {
        val resolvedRoot = root.get() ?: return logits
        val allowedIds = traverseTrie(resolvedRoot, inputIds)
        logits.indices
            .filterNot { it in allowedIds }
            .forEach { logits[it] = Float.NEGATIVE_INFINITY }
        return logits
    }

    private fun traverseTrie(root: TrieNode, inputIds: List<Int>): Set<Int> {
        var currentNode = root
        for (token in inputIds) {
            currentNode = currentNode.children[token] ?: run {
                Log.w("VocabularyLogitsProcessor",
                    "Traversal failed for token $token in inputIds $inputIds")
                return emptySet()
            }
        }
        return currentNode.children.keys
    }

}