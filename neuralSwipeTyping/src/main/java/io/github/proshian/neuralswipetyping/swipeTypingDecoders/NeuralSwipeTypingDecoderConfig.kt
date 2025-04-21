package io.github.proshian.neuralswipetyping.swipeTypingDecoders

import io.github.proshian.neuralswipetyping.decodingAlgorithms.DecodingAlgorithmConfig
import io.github.proshian.neuralswipetyping.logitsProcessors.LogitsProcessorConfig
import io.github.proshian.neuralswipetyping.swipePointFeaturesExtraction.FeatureExtractorConfig
import kotlinx.serialization.Serializable


@Serializable
data class NeuralSwipeTypingDecoderConfig(
    val modelPath: String,
    val wordTokenizerPath: String,
    val decodingAlgorithmConfig: DecodingAlgorithmConfig,
    val featureExtractorConfig: FeatureExtractorConfig,
    val logitsProcessorConfig: LogitsProcessorConfig
)
