package io.github.proshian.neuralswipetyping.decodingAlgorithms

class ScoredTokenSequenceCandidate (
    val tokensSequence: IntArray,
    val negLogProb: Float
)