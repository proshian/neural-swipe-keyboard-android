package io.github.proshian.neuralswipetyping.decodingAlgorithms

import io.github.proshian.neuralswipetyping.logitsProcessors.LogitsProcessor
import org.pytorch.executorch.EValue
import org.pytorch.executorch.Module
import org.pytorch.executorch.Tensor
import io.github.proshian.neuralswipetyping.neuralNetworkComponents.logSoftmax
import java.util.PriorityQueue
import kotlin.math.pow

private data class Hypothesis(
    val score: Float,
    val tokens: List<Int>
)


class BeamSearch(
    private val module: Module,
    private val sosToken: Int,
    private val eosToken: Int,
    private val maxSteps: Int,
    private val beamSize: Int,
    private val normalizationFactor: Float = 0.5f,
    private val returnHypothesesN: Int? = null,
    private val logitsProcessor: LogitsProcessor? = null
) : DecodingAlgorithm() {

    override fun decode(encoded: EValue): List<ScoredTokenSequenceCandidate> {
        return beamSearch(
            encoded,
            module,
            sosToken,
            eosToken,
            maxSteps,
            beamSize,
            normalizationFactor,
            returnHypothesesN,
            logitsProcessor
        )
    }
}

fun beamSearch(
    encoded: EValue,
    module: Module,
    sosToken: Int,
    eosToken: Int,
    maxSteps: Int,
    beamSize: Int,
    normalizationFactor: Float = 0.5f,
    returnHypothesesN: Int? = null,
    logitsProcessor: LogitsProcessor? = null
): List<ScoredTokenSequenceCandidate> {
    val partialHypotheses = PriorityQueue<Hypothesis>(compareBy { it.score })
    partialHypotheses.add(Hypothesis(0.0f, listOf(sosToken)))

    val finalHypotheses = mutableListOf<Hypothesis>()

    val emptyPollMsg = "Unexpected state: partialHypotheses is empty." +
            "PartialHypotheses should never be empty here due to the loop condition"

    while (partialHypotheses.isNotEmpty()) {
        val currentHypothesis = partialHypotheses.poll()
            ?: error(emptyPollMsg)

        val currentTokens = currentHypothesis.tokens
        val decoderInput = Tensor.fromBlob(
            currentTokens.toIntArray(),
            longArrayOf(currentTokens.size.toLong(), 1)
        )

        val allLogitsTensor = module
            .execute("decode", EValue.from(decoderInput), encoded)
            .single()
            .toTensor()
        val nextTokenLogits = getLastStepLogits(allLogitsTensor).single()
        val processedLogits = logitsProcessor?.process(nextTokenLogits, currentTokens)
            ?: nextTokenLogits


        val logProbs = logSoftmax(processedLogits)
        val topK = logProbs
            .withIndex()
            // logProb = -inf <=> prob = 0. We don't want to consider impossible hypotheses.
            .filter { it.value != Float.NEGATIVE_INFINITY }
            .sortedByDescending { it.value }
            .take(beamSize)

        for ((tokenId, tokenLogProb) in topK) {
            // Calculate new score with length normalization
            val currentLength = currentTokens.size.toFloat()
            val denormScore = currentHypothesis.score * currentLength.pow(normalizationFactor)
            val newDenormScore = denormScore - tokenLogProb
            val newLength = currentLength + 1
            val newScore = newDenormScore / newLength.pow(normalizationFactor)

            val newTokens = currentTokens + tokenId
            val newHypothesis = Hypothesis(newScore, newTokens)

            // "-1" to account for the sosToken.
            if (tokenId == eosToken || newTokens.size - 1 >= maxSteps) {
                finalHypotheses.add(newHypothesis)
            } else {
                partialHypotheses.add(newHypothesis)
            }
        }

        if (partialHypotheses.size > beamSize) {
            val bestHypotheses = mutableListOf<Hypothesis>()
            repeat(beamSize) {
                if (partialHypotheses.isNotEmpty()) {
                    bestHypotheses.add(partialHypotheses.poll() ?: error(emptyPollMsg))
                }
            }
            partialHypotheses.clear()
            partialHypotheses.addAll(bestHypotheses)
        }
    }

    val sortedResults = finalHypotheses
        .sortedBy { it.score }
        .map { ScoredTokenSequenceCandidate(it.tokens.toIntArray(), it.score) }

    return returnHypothesesN?.let { sortedResults.take(it) } ?: sortedResults
}