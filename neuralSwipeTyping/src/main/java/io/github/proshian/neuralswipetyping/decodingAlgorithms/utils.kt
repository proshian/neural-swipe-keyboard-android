package io.github.proshian.neuralswipetyping.decodingAlgorithms

import org.pytorch.executorch.Tensor

fun getLastStepLogits(allLogitsTensor: Tensor): Array<FloatArray> {
    val shape = allLogitsTensor.shape()
    val seqLen = shape[0].toInt()
    val batchSize = shape[1].toInt()
    val vocabSize = shape[2].toInt()
    val data = allLogitsTensor.dataAsFloatArray

    return Array(batchSize) { batchIdx ->
        FloatArray(vocabSize) { vocabIdx ->
            data[(seqLen - 1) * batchSize * vocabSize +
                    batchIdx * vocabSize +
                    vocabIdx]
        }
    }
}