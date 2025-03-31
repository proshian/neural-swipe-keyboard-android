package com.example.neuralSwipeKeyboardProject.swipeTypingDecoders

abstract class SwipeTypingDecoder {
    abstract fun decodeSwipe(
        x: IntArray,
        y: IntArray,
        t: IntArray
    ): List<String>
}