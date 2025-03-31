package com.example.neuralSwipeKeyboardProject.decodingAlgorithms

import com.example.neuralSwipeKeyboardProject.logitsProcessors.LogitsProcessor
import org.pytorch.executorch.EValue
import org.pytorch.executorch.Module
import org.pytorch.executorch.Tensor
import com.example.neuralSwipeKeyboardProject.neuralNetworkComponents.logSoftmax
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

    while (partialHypotheses.isNotEmpty()) {
        val currentHypothesis = partialHypotheses.poll()
            ?: throw IllegalStateException(
                "Unexpected null hypothesis in partialHypotheses priority queue")

        val currentTokens = currentHypothesis.tokens
        if (currentTokens.last() == eosToken || currentTokens.size - 1 >= maxSteps) {
            finalHypotheses.add(currentHypothesis)
            continue
        }

        val decoderInput = Tensor.fromBlob(
            currentTokens.toIntArray(),
            longArrayOf(currentTokens.size.toLong(), 1)
        )

        val decodedEValue = module.execute(
            "decode",
            EValue.from(decoderInput),
            encoded
        )[0]
        val allLogitsTensor = decodedEValue.toTensor()
        val nextTokenLogits = getLastStepLogits(allLogitsTensor)
        val processedLogits = logitsProcessor?.process(nextTokenLogits, currentTokens) ?: nextTokenLogits


        val logProbs = logSoftmax(processedLogits)
        val topK = logProbs.withIndex()
            .sortedByDescending { it.value }
            .take(beamSize)

        for ((tokenIndex, tokenLogProb) in topK) {
            if (tokenLogProb == Float.NEGATIVE_INFINITY) continue

            // Calculate new score with length normalization
            val currentLength = currentTokens.size.toFloat()
            val denormScore = currentHypothesis.score * currentLength.pow(normalizationFactor)
            val newDenormScore = denormScore - tokenLogProb
            val newLength = currentLength + 1
            val newScore = newDenormScore / newLength.pow(normalizationFactor)

            val newTokens = currentTokens + tokenIndex
            val newHypothesis = Hypothesis(newScore, newTokens)

            if (tokenIndex == eosToken || newTokens.size - 1 >= maxSteps) {
                finalHypotheses.add(newHypothesis)
            } else {
                partialHypotheses.add(newHypothesis)
            }
        }

        if (partialHypotheses.size > beamSize) {
            val bestHypotheses = mutableListOf<Hypothesis>()
            repeat(beamSize) {
                if (partialHypotheses.isNotEmpty()) {
                    bestHypotheses.add(partialHypotheses.poll() ?: throw IllegalStateException(
                        "Unexpected null hypothesis in partialHypotheses priority queue"))
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