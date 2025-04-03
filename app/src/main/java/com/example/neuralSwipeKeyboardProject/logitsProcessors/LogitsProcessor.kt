package com.example.neuralSwipeKeyboardProject.logitsProcessors

abstract class LogitsProcessor {
    abstract fun process(logits: FloatArray, inputIds: List<Int>): FloatArray
}