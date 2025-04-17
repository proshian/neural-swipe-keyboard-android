package io.github.proshian.neuralswipetyping.logitsProcessors

abstract class LogitsProcessor {
    abstract fun process(logits: FloatArray, inputIds: List<Int>): FloatArray
}