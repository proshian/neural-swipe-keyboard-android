package com.example.neuralSwipeKeyboardProject.logitsProcessors

import android.content.Context
import android.util.Log
import com.example.neuralSwipeKeyboardProject.tokenizers.RuSubwordTokenizer
import com.example.trie.ImmutableNode
import com.example.trie.deserialize

class VocabularyLogitsProcessorFromPrebuiltTrie(
    private val tokenizer: RuSubwordTokenizer,
    context: Context, // for asset access
    trieAssetPath: String = "trie.ser" // Default path
) : LogitsProcessor() {

    private val root: ImmutableNode<Int> = context.assets.open(trieAssetPath).use { deserialize(it) }


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
                Log.w("VocabularyLogitsProcessor",
                    "Traversal failed for token $token in inputIds $inputIds")
                return emptySet()
            }
        }
        return currentNode.children.keys
    }
}