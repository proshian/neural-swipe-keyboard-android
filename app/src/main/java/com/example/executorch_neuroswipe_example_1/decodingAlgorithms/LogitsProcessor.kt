package com.example.executorch_neuroswipe_example_1.decodingAlgorithms

abstract class LogitsProcessor {
    abstract fun process(logits: FloatArray, inputIds: List<Int>): FloatArray
}