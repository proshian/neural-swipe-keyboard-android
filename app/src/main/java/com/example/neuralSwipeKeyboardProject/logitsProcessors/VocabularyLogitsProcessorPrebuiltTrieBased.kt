package com.example.neuralSwipeKeyboardProject.logitsProcessors

import android.content.Context
import android.util.Log
import com.example.trie.ImmutableNode
import com.example.trie.deserialize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

class VocabularyLogitsProcessorPrebuiltTrieBased (
    private val context: Context,
    private val trieAssetPath: String
) : LogitsProcessor() {

    private val root: AtomicReference<ImmutableNode<Int>?> = AtomicReference(null)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            initializeTrie()
        }
    }

    private suspend fun initializeTrie() {
        try {
            root.set(context.assets.open(trieAssetPath).use { deserialize<Int>(it) })
        } catch(e: Exception) {
            Log.e("VocabularyLogitsProcessor", "Trie initialization failed", e)
            throw e
        }
    }

    override fun process(logits: FloatArray, inputIds: List<Int>): FloatArray {
        val resolvedRoot = root.get() ?: return logits

        val allowedIds = traverseTrie(resolvedRoot, inputIds)

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