package io.github.proshian.neuralswipetyping.swipePointFeaturesExtraction

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class FeatureExtractorConfig(
    val type: String,
    val params: Map<String, JsonElement>
)