package io.github.proshian.neuralswipetyping.logitsProcessors

import android.util.Log
import io.github.proshian.neuralswipetyping.tokenizers.WordTokenizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

/**
 * With the Yandex Cup vocabulary, consumes approximately 290 MB.
 */
class VocabularyLogitsProcessorMapBased(
    private val tokenizer: WordTokenizer,
    private val vocab: List<String>,
) : LogitsProcessor() {

    private val prefixToAllowedIds: AtomicReference<Map<List<Int>, Set<Int>>?> = AtomicReference(null)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            prefixToAllowedIds.set(createPrefixToAllowedIds())
        }
    }

    private suspend fun createPrefixToAllowedIds(): Map<List<Int>, Set<Int>> {
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
        val resolvedPrefixToAllowedIds = prefixToAllowedIds.get() ?: return logits

        val allowedIds = resolvedPrefixToAllowedIds[inputIds] ?: run {
            Log.w("", "empty allowed ids for prefix ${inputIds.joinToString(", ")}")
            return logits
        }
        (logits.indices)
            .filterNot { it in allowedIds }
            .forEach { logits[it] = Float.NEGATIVE_INFINITY }
        return logits
    }
}