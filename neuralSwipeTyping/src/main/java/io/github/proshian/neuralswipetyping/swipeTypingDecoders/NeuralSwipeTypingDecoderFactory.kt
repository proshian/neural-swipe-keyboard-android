package io.github.proshian.neuralswipetyping.swipeTypingDecoders

import android.content.Context
import io.github.proshian.neuralswipetyping.assetUtils.AssetUtils
import io.github.proshian.neuralswipetyping.swipePointFeaturesExtraction.FeatureExtractorFactory
import io.github.proshian.neuralswipetyping.tokenizers.WordTokenizer
import io.github.proshian.neuralswipetyping.logitsProcessors.LogitsProcessorFactory
import io.github.proshian.neuralswipetyping.decodingAlgorithms.DecodingAlgorithmFactory
import io.github.proshian.neuralswipetyping.decodingAlgorithms.StandardDecodingAlgorithmFactory
import io.github.proshian.neuralswipetyping.logitsProcessors.StandardLogitsProcessorFactory
import io.github.proshian.neuralswipetyping.swipePointFeaturesExtraction.StandardFeatureExtractorFactory
import org.pytorch.executorch.Module
import kotlinx.serialization.json.Json


interface NeuralSwipeTypingDecoderFactory {
    fun create(context: Context, config: NeuralSwipeTypingDecoderConfig): NeuralSwipeTypingDecoder
}

class StandardNeuralSwipeTypingDecoderFactory(
    private val featureFactory: FeatureExtractorFactory = StandardFeatureExtractorFactory(),
    private val logitsFactory: LogitsProcessorFactory = StandardLogitsProcessorFactory(),
    private val algorithmFactory: DecodingAlgorithmFactory = StandardDecodingAlgorithmFactory()
) : NeuralSwipeTypingDecoderFactory{
    override fun create(context: Context, config: NeuralSwipeTypingDecoderConfig): NeuralSwipeTypingDecoder {
        val model = Module.load(AssetUtils.assetFilePath(context, config.modelPath))
            ?: throw IllegalStateException("Model loading failed")

        val wordTokenizer = Json.decodeFromString<WordTokenizer>(
            context.assets.open(config.wordTokenizerPath).use { it.reader().readText() }
        )

        val logitsProcessor = logitsFactory.create(
            context,
            config.logitsProcessorConfig,
            wordTokenizer
        )

        val decodingAlgorithm = algorithmFactory.create(
            model,
            wordTokenizer,
            config.decodingAlgorithmConfig,
            logitsProcessor
        )

        val featureExtractor = featureFactory.create(
            context,
            config.featureExtractorConfig
        )

        return NeuralSwipeTypingDecoder(
            model,
            decodingAlgorithm,
            wordTokenizer,
            featureExtractor
        )
    }
}