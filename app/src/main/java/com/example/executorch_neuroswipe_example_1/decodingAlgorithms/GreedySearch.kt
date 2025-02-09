package com.example.executorch_neuroswipe_example_1.decodingAlgorithms

import org.pytorch.executorch.EValue
import org.pytorch.executorch.Module
import org.pytorch.executorch.Tensor
import com.example.executorch_neuroswipe_example_1.neuralNetworkComponents.logSoftmax


fun getLastStepLogits(allLogitsTensor: Tensor): FloatArray {
    val seqLen = allLogitsTensor.shape()[0].toInt()
    val outDim = allLogitsTensor.shape()[2].toInt()
    val arr = allLogitsTensor.dataAsFloatArray
    return arr.slice(outDim*(seqLen-1)..<outDim*seqLen).toFloatArray()
}

fun greedySearch(encoded: EValue, module: Module, sosToken: Int,
                 eosToken: Int, maxSteps: Int): List<ScoredTokenSequenceCandidate>  {
    val decoderInputList = mutableListOf(sosToken)
    var logProb = 0.0f

    for (step in 0 until maxSteps) {
        val decoderInput = Tensor.fromBlob(
            decoderInputList.toIntArray(),
            longArrayOf(decoderInputList.size.toLong(), 1)
        )

        val decodedEValue = module.execute(
            "decode",
            EValue.from(decoderInput), encoded)[0]

        val allLogitsTensor = decodedEValue.toTensor()
        val nextTokenLogits = getLastStepLogits(allLogitsTensor)

        val logProbs = logSoftmax(nextTokenLogits)
        val mostProbableTokenId = logProbs.indices.maxByOrNull { logProbs[it] }!!
        logProb -= logProbs[mostProbableTokenId]
        decoderInputList.add(mostProbableTokenId)

        if (mostProbableTokenId == eosToken) {
            break
        }
    }

    return listOf(ScoredTokenSequenceCandidate(decoderInputList.toIntArray(), logProb))
}


class GreedySearch(
    private val module: Module,
    private val sosToken: Int,
    private val eosToken: Int,
    private val maxSteps: Int
) : DecodingAlgorithm() {

    override fun decode(encoded: EValue): List<ScoredTokenSequenceCandidate> {
        return greedySearch(encoded, module, sosToken, eosToken, maxSteps)
    }
}
