package com.example.executorch_neuroswipe_example_1.swipeTypingDecoders

abstract class SwipeTypingDecoder {
    abstract fun decodeSwipe(
        x: IntArray,
        y: IntArray,
        t: IntArray
    ): List<String>
}