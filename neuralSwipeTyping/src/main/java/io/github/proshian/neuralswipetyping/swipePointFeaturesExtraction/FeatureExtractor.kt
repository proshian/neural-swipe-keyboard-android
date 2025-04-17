package io.github.proshian.neuralswipetyping.swipePointFeaturesExtraction

import org.pytorch.executorch.EValue

typealias FeatureExtractor = (IntArray, IntArray, IntArray) -> Array<EValue>