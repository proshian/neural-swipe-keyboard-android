package io.github.proshian.neuralswipetyping.swipePointFeaturesExtraction

import org.pytorch.executorch.EValue


class FeatureExtractorAggregator(private val extractors: List<FeatureExtractor>) :
    FeatureExtractor {
    override fun extractFeatures(x: IntArray, y: IntArray, t: IntArray): Array<EValue> {
        return extractors.flatMap { it.extractFeatures(x, y, t).toList() }.toTypedArray()
    }
}