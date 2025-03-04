package com.example.executorch_neuroswipe_example_1.decodingAlgorithms

import org.pytorch.executorch.EValue
import org.pytorch.executorch.Module
import org.pytorch.executorch.Tensor
import com.example.executorch_neuroswipe_example_1.neuralNetworkComponents.logSoftmax


class GreedySearch(
    private val module: Module,
    private val sosToken: Int,
    private val eosToken: Int,
    private val maxSteps: Int,
    private val logitsProcessor: LogitsProcessor? = null
) : DecodingAlgorithm() {

    override fun decode(encoded: EValue): List<ScoredTokenSequenceCandidate> {
        return greedySearch(
            encoded,
            module,
            sosToken,
            eosToken,
            maxSteps,
            logitsProcessor
        )
    }
}

fun greedySearch(
    encoded: EValue,
    module: Module,
    sosToken: Int,
    eosToken: Int,
    maxSteps: Int,
    logitsProcessor: LogitsProcessor? = null
): List<ScoredTokenSequenceCandidate> {
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
        val processedLogits = logitsProcessor?.process(nextTokenLogits, decoderInputList.toList())
            ?: nextTokenLogits

        val logProbs = logSoftmax(processedLogits)
        val mostProbableTokenId = logProbs.indices.maxByOrNull { logProbs[it] }!!
        logProb -= logProbs[mostProbableTokenId]
        decoderInputList.add(mostProbableTokenId)

        if (mostProbableTokenId == eosToken) {
            break
        }
    }

    return listOf(ScoredTokenSequenceCandidate(decoderInputList.toIntArray(), logProb))
}


