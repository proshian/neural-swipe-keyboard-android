package io.github.proshian.neuralswipetyping.neuralNetworkComponents

import kotlin.math.exp
import kotlin.math.log
import kotlin.math.E


fun softmax(input: FloatArray): FloatArray {
    val max = input.maxOrNull() ?: 0f // To prevent overflow, subtract the max value
    val expValues = input.map { exp((it - max).toDouble()).toFloat() }
    val sumExpValues = expValues.sum()
    return expValues.map { it / sumExpValues }.toFloatArray()
}

fun logSoftmax(input: FloatArray): FloatArray {
    val max = input.maxOrNull() ?: 0f // To prevent overflow, subtract the max value
    val expValues = input.map { exp((it - max).toDouble()).toFloat() }
    val sumExpValues = expValues.sum()
    val logSumExp = log(sumExpValues.toDouble(), E).toFloat()
    return input.map { it - max - logSumExp }.toFloatArray()
}
