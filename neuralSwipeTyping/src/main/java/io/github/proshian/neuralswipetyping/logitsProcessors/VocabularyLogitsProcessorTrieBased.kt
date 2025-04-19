package io.github.proshian.neuralswipetyping.logitsProcessors

import android.util.Log
import io.github.proshian.neuralswipetyping.tokenizers.WordTokenizer
import com.example.trie.traverseTrie
import com.example.trie.MutableNode
import com.example.trie.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference


/**
 * Builds a mutable trie.
 *
 * With the Yandex Cup vocabulary, consumes approximately 210 MB.
 */
class VocabularyLogitsProcessorTrieBased(
    private val tokenizer: WordTokenizer,
    private val vocab: List<String>,
) : LogitsProcessor() {
    private val root: AtomicReference<Node<Int>?> = AtomicReference(null)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            root.set(buildTrie())
        }
    }

    private suspend fun buildTrie(): MutableNode<Int>{
        val futureTrieRoot = MutableNode<Int>()
        vocab.forEach { word ->
            val tokens = tokenizer.tokenize(word).toList()
            var currentNode = futureTrieRoot
            for (token in tokens) {
                currentNode = currentNode.children.getOrPut(token) { MutableNode() }
            }
        }
        return futureTrieRoot
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