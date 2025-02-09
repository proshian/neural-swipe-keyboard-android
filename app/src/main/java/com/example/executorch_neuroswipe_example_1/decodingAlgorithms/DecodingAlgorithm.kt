package com.example.executorch_neuroswipe_example_1.decodingAlgorithms

import org.pytorch.executorch.EValue

abstract class DecodingAlgorithm {
    abstract fun decode(encoded: EValue): List<ScoredTokenSequenceCandidate>
}