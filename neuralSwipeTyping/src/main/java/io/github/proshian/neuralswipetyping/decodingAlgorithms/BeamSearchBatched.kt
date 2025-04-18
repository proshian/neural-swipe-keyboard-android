package io.github.proshian.neuralswipetyping.decodingAlgorithms

import io.github.proshian.neuralswipetyping.logitsProcessors.LogitsProcessor
import org.pytorch.executorch.EValue
import org.pytorch.executorch.Module
import org.pytorch.executorch.Tensor
import io.github.proshian.neuralswipetyping.neuralNetworkComponents.logSoftmax
import kotlin.math.pow

private data class HypothesisBatchedBeamsearch(
    val score: Float,
    val tokens: List<Int>
)

class BeamSearchBatched(
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
        return beamSearchBatched(
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

fun beamSearchBatched(
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
    var currentHypotheses = mutableListOf(HypothesisBatchedBeamsearch(0.0f, listOf(sosToken)))
    val finalHypotheses = mutableListOf<HypothesisBatchedBeamsearch>()

    for (step in 0 until maxSteps) {
        if (currentHypotheses.isEmpty()) break

        val batchSize = currentHypotheses.size
        val seqLen = currentHypotheses[0].tokens.size

        val decoderInputArray = IntArray(seqLen * batchSize) { index ->
            val hypoIdx = index % batchSize
            val tokenPos = index / batchSize
            currentHypotheses[hypoIdx].tokens[tokenPos]
        }
        val decoderInput = Tensor.fromBlob(
            decoderInputArray,
            longArrayOf(seqLen.toLong(), batchSize.toLong())
        )

        val allLogitsTensor = module.execute("decode", EValue.from(decoderInput), encoded)
            .single()
            .toTensor()
        val nextTokenLogits = getLastStepLogits(allLogitsTensor) // Shape (batchSize, vocabSize)


        val processedLogits = logitsProcessor?.let { processor ->
            Array(batchSize) { hypoIdx ->
                processor.process(
                    nextTokenLogits[hypoIdx],
                    currentHypotheses[hypoIdx].tokens
                )
            }
        } ?: nextTokenLogits

        val logProbs = processedLogits.map { logits -> logSoftmax(logits) }

        val allCandidates = mutableListOf<HypothesisBatchedBeamsearch>()
        for (hypoIdx in 0 until batchSize) {
            val currentHypo = currentHypotheses[hypoIdx]
            val currentTokens = currentHypo.tokens

            logProbs[hypoIdx].withIndex()
                .filter { it.value != Float.NEGATIVE_INFINITY }
                .sortedByDescending { it.value }
                .take(beamSize)
                .forEach { (tokenId, tokenLogProb) ->
                    val newLength = currentTokens.size + 1f
                    val denormScore = currentHypo.score * (newLength - 1f).pow(normalizationFactor)
                    val newDenormScore = denormScore - tokenLogProb
                    val newScore = newDenormScore / newLength.pow(normalizationFactor)

                    val newTokens = currentTokens + tokenId
                    val newHypothesis = HypothesisBatchedBeamsearch(newScore, newTokens)

                    if (tokenId == eosToken || newTokens.size - 1 >= maxSteps) {
                        finalHypotheses.add(newHypothesis)
                    } else {
                        allCandidates.add(newHypothesis)
                    }
                }
        }

        currentHypotheses = allCandidates
            .sortedBy { it.score }
            .take(beamSize)
            .toMutableList()
    }

    finalHypotheses.addAll(currentHypotheses)

    val sortedResults = finalHypotheses
        .sortedBy { it.score }
        .map { ScoredTokenSequenceCandidate(it.tokens.toIntArray(), it.score) }

    return returnHypothesesN?.let { sortedResults.take(it) } ?: sortedResults
}