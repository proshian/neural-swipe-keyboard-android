package io.github.proshian.neuralswipetyping.logitsProcessors

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class LogitsProcessorConfig(
    val type: String,
    val params: Map<String, JsonElement>
)