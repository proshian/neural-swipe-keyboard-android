package io.github.proshian.neuralswipetyping.decodingAlgorithms

import io.github.proshian.neuralswipetyping.logitsProcessors.LogitsProcessor
import io.github.proshian.neuralswipetyping.tokenizers.WordTokenizer
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import org.pytorch.executorch.Module

interface DecodingAlgorithmFactory {
    fun create(module: Module, tokenizer: WordTokenizer, config: DecodingAlgorithmConfig,
               logitsProcessor: LogitsProcessor?): DecodingAlgorithm
}


class StandardDecodingAlgorithmFactory : DecodingAlgorithmFactory {
    override fun create(module: Module, tokenizer: WordTokenizer, config: DecodingAlgorithmConfig,
                        logitsProcessor: LogitsProcessor?): DecodingAlgorithm {
        return when (config.type) {
            "beam_search" -> createBeamSearch(module, tokenizer, config, logitsProcessor)
            else -> throw IllegalArgumentException("Unsupported algorithm: ${config.type}")
        }
    }

    private fun createBeamSearch(module: Module, tokenizer: WordTokenizer,
                                 config: DecodingAlgorithmConfig,
                                 logitsProcessor: LogitsProcessor?): BeamSearch {
        return BeamSearch(
            module,
            tokenizer.sosTokenId,
            tokenizer.eosTokenId,
            maxSteps = config.params["max_steps"]!!.jsonPrimitive.int,
            beamSize = config.params["beam_size"]!!.jsonPrimitive.int,
            logitsProcessor = logitsProcessor
        )
    }
}