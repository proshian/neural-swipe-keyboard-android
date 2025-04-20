package io.github.proshian.neuralswipetyping.swipePointFeaturesExtraction

import org.pytorch.executorch.EValue

/**
 * An interface for classes that extract features from raw swipe data.
 *
 * Note that by leveraging Kotlin's SAM conversion you can
 * instantiate a FeatureExtractor implementation with a lambda kile this:
 * val myFeatureExtraction = FeatureExtractor { myFeatureExtractingFunction },
 * where myFeatureExtractingFunction is any function
 * with (IntArray, IntArray, IntArray) -> Array<EValue> signature.
 */
fun interface FeatureExtractor {
    fun extractFeatures(x: IntArray, y: IntArray, t: IntArray): Array<EValue>
}