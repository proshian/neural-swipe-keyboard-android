package io.github.proshian.neuralswipetyping.decodingAlgorithms

import org.pytorch.executorch.EValue

abstract class DecodingAlgorithm {
    abstract fun decode(encoded: EValue): List<ScoredTokenSequenceCandidate>
}