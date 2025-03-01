package com.example.executorch_neuroswipe_example_1.ngtFeaturesExtraction

import org.pytorch.executorch.EValue

typealias FeatureExtractor = (IntArray, IntArray, IntArray) -> Array<EValue>