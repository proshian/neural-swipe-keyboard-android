package com.example.neuralSwipeKeyboardProject.logitsProcessors

import android.content.Context
import android.util.Log
import com.example.trie.ImmutableNode
import com.example.trie.traverseTrie
import com.example.trie.deserializeImmutableTrie
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
            root.set(context.assets.open(trieAssetPath).use { deserializeImmutableTrie<Int>(it) })
        } catch(e: Exception) {
            Log.e("VocabularyLogitsProcessor", "Trie initialization failed", e)
            throw e
        }
    }

    override fun process(logits: FloatArray, inputIds: List<Int>): FloatArray {
        val resolvedRoot = root.get() ?: return logits

        val allowedIds = traverseTrie(resolvedRoot, inputIds) ?: run {
            Log.w("VocabularyLogitsProcessor", "inputIds prefix is not in the trie")
            return logits
        }

        return logits.apply {
            indices.filterNot { it in allowedIds }
                .forEach { this[it] = Float.NEGATIVE_INFINITY }
        }
    }
}