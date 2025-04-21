package io.github.proshian.neuralswipetyping.swipePointFeaturesExtraction

import android.content.Context
import io.github.proshian.neuralswipetyping.keyboardGrid.KeyboardGridReader
import io.github.proshian.neuralswipetyping.tokenizers.KeyboardTokenizer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

interface FeatureExtractorFactory {
    fun create(context: Context, config: FeatureExtractorConfig): FeatureExtractor
}


class StandardFeatureExtractorFactory : FeatureExtractorFactory {
    override fun create(context: Context, config: FeatureExtractorConfig): FeatureExtractor {
        return when (config.type) {
            "traj+nearest_keys" -> createTrajWithNearestKeys(context, config.params)
            else -> throw IllegalArgumentException("Unsupported feature extractor: ${config.type}")
        }
    }

    private fun createTrajWithNearestKeys(
        context: Context,
        params: Map<String, JsonElement>
    ): FeatureExtractor {
        val traj = TrajFeatsGetter(
            includeTime = params["include_time"]!!.jsonPrimitive.boolean,
            includeVelocities = params["include_acceleration"]!!.jsonPrimitive.boolean,
            includeAccelerations = params["include_acceleration"]!!.jsonPrimitive.boolean,
            width = params["width"]!!.jsonPrimitive.int,
            height = params["height"]!!.jsonPrimitive.int
        )

        val keyboardGrid = KeyboardGridReader(context)
            .readKeyboardGridFromAssets(params["keyboard_grid_path"]!!.jsonPrimitive.content)

        val keyboardTokenizer = Json.decodeFromString<KeyboardTokenizer>(
            context.assets.open(params["keyboard_tokenizer_path"]!!.jsonPrimitive.content)
                .use { it.reader().readText() }
        )

        val nearestKeys = NearestKeysGetter(keyboardGrid, keyboardTokenizer)
        return FeatureExtractorAggregator(listOf(traj, nearestKeys))
    }
}
