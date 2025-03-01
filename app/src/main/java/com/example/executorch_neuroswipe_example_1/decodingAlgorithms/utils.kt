package com.example.executorch_neuroswipe_example_1.decodingAlgorithms

import org.pytorch.executorch.Tensor

fun getLastStepLogits(allLogitsTensor: Tensor): FloatArray {
    val seqLen = allLogitsTensor.shape()[0].toInt()
    val outDim = allLogitsTensor.shape()[2].toInt()
    val arr = allLogitsTensor.dataAsFloatArray
    return arr.slice(outDim*(seqLen-1)..<outDim*seqLen).toFloatArray()
}