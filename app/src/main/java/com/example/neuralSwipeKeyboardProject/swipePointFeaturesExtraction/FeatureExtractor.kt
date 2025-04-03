package com.example.neuralSwipeKeyboardProject.swipePointFeaturesExtraction

import org.pytorch.executorch.EValue

typealias FeatureExtractor = (IntArray, IntArray, IntArray) -> Array<EValue>