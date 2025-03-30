package com.example.executorch_neuroswipe_example_1.decodingAlgorithms

import android.content.Context
import android.util.Log
import com.example.executorch_neuroswipe_example_1.tokenizers.RuSubwordTokenizer
import com.example.trie.ImmutableNode
import com.example.trie.deserialize

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class VocabularyLogitsProcessorFromPrebuiltTrieV2 (
    private val tokenizer: RuSubwordTokenizer,
    private val context: Context,
    private val maxTokenId: Int,
    private val trieAssetPath: String = "trie.ser"
) : LogitsProcessor() {

    private val root: AtomicReference<ImmutableNode<Int>?> = AtomicReference(null)
    private val initializationException = AtomicReference<Throwable?>(null)
    private val isInitializing = AtomicBoolean(false)

    init {
        // Start async initialization
        CoroutineScope(Dispatchers.IO).launch {
            initializeTrie()
        }
    }

    private suspend fun initializeTrie() {
        if (isInitializing.compareAndSet(false, true)) {
            try {
                val trieRoot = context.assets.open(trieAssetPath).use { deserialize<Int>(it) }
                root.set(trieRoot)
            } catch (e: Exception) {
                initializationException.set(e)
                Log.e("VocabularyProcessor", "Trie initialization failed", e)
            } finally {
                isInitializing.set(false)
            }
        }
    }

    override fun process(logits: FloatArray, inputIds: List<Int>): FloatArray {
        val currentRoot = root.get() ?: return logits

        val allowedIds = traverseTrie(currentRoot, inputIds)

        return logits.apply {
            indices.filterNot { it in allowedIds }
                .forEach { this[it] = Float.NEGATIVE_INFINITY }
        }
    }

    private fun traverseTrie(root: ImmutableNode<Int>, inputIds: List<Int>): Set<Int> {
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