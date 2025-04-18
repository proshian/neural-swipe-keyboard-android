package io.github.proshian.neuralswipetyping.decodingAlgorithms

import io.github.proshian.neuralswipetyping.logitsProcessors.LogitsProcessor
import org.pytorch.executorch.EValue
import org.pytorch.executorch.Module
import org.pytorch.executorch.Tensor
import io.github.proshian.neuralswipetyping.neuralNetworkComponents.logSoftmax

/**
 * Greedy search decoding algorithm.
 *
 * Selects the most probable token at each step until it generates the [eosToken] or
 * reaches [maxSteps].
 *
 * @param module The decoder neural network that models the probability of the next token
 *               given the previous tokens.
 *               Expected to support `execute("decode", ...)`
 *               returning logits shaped `[seq_len, 1, vocab_size]`.
 * @param sosToken The start-of-sequence token ID (used to initialize decoding).
 * @param eosToken The end-of-sequence token ID (stops decoding when generated).
 * @param maxSteps The maximum number of decoding steps to take.
 * @param logitsProcessor Optional post-processor for logits. 
 *                        Can be used to filter out impossible tokens for a given prefix 
 *                        according to vocabulary,
 *                        or for techniques like top-k sampling, etc.
 *
 * @return A list containing a single [ScoredTokenSequenceCandidate].
 *         The score is the log probability of the sequence (of the word).
 */
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
    var sequenceNeglogProb = 0.0f

    for (step in 0 until maxSteps) {
        val decoderInput = Tensor.fromBlob(
            decoderInputList.toIntArray(),
            longArrayOf(decoderInputList.size.toLong(), 1)
        )

        val allLogitsTensor = module
            .execute("decode", EValue.from(decoderInput), encoded)
            .single()
            .toTensor()
        val nextTokenLogits = getLastStepLogits(allLogitsTensor)
        val processedLogits = logitsProcessor?.process(nextTokenLogits, decoderInputList.toList())
            ?: nextTokenLogits

        val logProbs = logSoftmax(processedLogits)
        val mostProbableTokenId = logProbs.indices.maxByOrNull { logProbs[it] }!!
        sequenceNeglogProb -= logProbs[mostProbableTokenId]
        decoderInputList.add(mostProbableTokenId)

        if (mostProbableTokenId == eosToken) {
            break
        }
    }

    return listOf(ScoredTokenSequenceCandidate(decoderInputList.toIntArray(), sequenceNeglogProb))
}
