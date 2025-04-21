package io.github.proshian.neuralswipetyping.decodingAlgorithms

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class DecodingAlgorithmConfig(
    val type: String,
    val params: Map<String, JsonElement>
)