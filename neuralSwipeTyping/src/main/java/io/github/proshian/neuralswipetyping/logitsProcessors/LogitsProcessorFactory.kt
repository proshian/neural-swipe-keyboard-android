package io.github.proshian.neuralswipetyping.logitsProcessors

import android.content.Context
import io.github.proshian.neuralswipetyping.tokenizers.WordTokenizer
import kotlinx.serialization.json.jsonPrimitive

interface LogitsProcessorFactory {
    fun create(context: Context, config: LogitsProcessorConfig,
               tokenizer: WordTokenizer): LogitsProcessor?

}

class StandardLogitsProcessorFactory : LogitsProcessorFactory {
    override fun create(
        context: Context,
        config: LogitsProcessorConfig,
        tokenizer: WordTokenizer
    ): LogitsProcessor? {
        return when (config.type) {
            "prebuilt_trie" -> VocabularyLogitsProcessorPrebuiltTrieBased(
                context,
                config.params["trie_path"]!!.jsonPrimitive.content
            )
            "map_based" -> VocabularyLogitsProcessorMapBased(
                tokenizer,
                loadVocabulary(context, config.params["vocab_path"]!!.jsonPrimitive.content)
            )
            "trie_based" -> VocabularyLogitsProcessorTrieBased(
                tokenizer,
                loadVocabulary(context, config.params["vocab_path"]!!.jsonPrimitive.content)
            )
            "none" -> null
            else -> throw IllegalArgumentException("Unsupported logits processor: ${config.type}")
        }
    }

    private fun loadVocabulary(context: Context, path: String): List<String> {
        return context.assets.open(path).bufferedReader().useLines {
            it.filterNot(String::isBlank).toList()
        }
    }
}