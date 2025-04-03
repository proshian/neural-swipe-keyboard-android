package com.example.neuralSwipeKeyboardProject.swipeTypingDecoders

import com.example.neuralSwipeKeyboardProject.decodingAlgorithms.DecodingAlgorithm
import com.example.neuralSwipeKeyboardProject.decodingAlgorithms.ScoredTokenSequenceCandidate
import org.pytorch.executorch.Module
import com.example.neuralSwipeKeyboardProject.tokenizers.StringTokenizer
import org.pytorch.executorch.EValue

/**
 * Uses a neural network model to decode swipe gestures into a word candidates list sorted by probability.
 *
 * This class is responsible for:
 * 1. Encoding the swipe gesture data (x, y, t) using a neural network model.
 * - Decoding the encoded data into a list of scored token sequence candidates using a decoding algorithm.
 * - Converting the scored token sequence candidates into a list of strings using a subword tokenizer.
 *
 * @param encoderModule The neural network model used for encoding swipe gesture data int a latent
 *                      representation. Must have an "encode" method (meaning
 *                      `encoderModule.execute("encode", *encoderInputArgs)` must be valid).
 * @param decodingAlgorithm The decoding algorithm. Decodes the encoded gesture into scored token
 *                          sequence candidates (descending order of probabilities). It uses
 *                          a decoder neural network that given the encoded swipe and the already
 *                          generated prefix models the probability distribution of the next token
 *                          in the sequence. It's not always optimal to choose the most probable
 *                          token, so we use a decoding algorithm (a search algorithm) to find the
 *                          best sequence of tokens.
 *                          The decoding algorithm also uses an optional logitsProcessor that
 *                          modifies the logits (the probabilities of the tokens) on each step of
 *                          the decoding (search) algorithm. In the case of this project
 *                          the logitsProcessor is used to zero out the probabilities of the tokens
 *                          that correspond to the continuations that are impossible according
 *                          to the vocabulary. Without logitsProcessor the model still works, but
 *                          sometimes it makes mistakes and typos.
 * @param subwordTokenizer The subword tokenizer used to convert the scored token sequence candidates into strings.
 * @param xytTransform A function that transforms the x, y, t arrays into features for the encoder
 *                     module (use same features as ones applied during training).
 *
 * Example usage:
 * ```
 * val decoder = NeuralSwipeTypingDecoder(
 *     encoderModule,
 *     BeamSearch(decoderModule, sosToken, eosToken, maxSteps, beamSize, logitsProcessor),
 *     RuSubwordTokenizer(),
 *     FeatureExtractorAggregator(listOf(trajFeatsGetter, nearestKeysGetter))
 * 
 * val candidates = decoder.decodeSwipe(xCoords, yCoords, timestamps)
 * ```
 */
class NeuralSwipeTypingDecoder(
    private val encoderModule: Module,
    private val decodingAlgorithm: DecodingAlgorithm,
    private val subwordTokenizer: StringTokenizer,
    private val xytTransform: (IntArray, IntArray, IntArray) -> Array<EValue>
) : SwipeTypingDecoder() {

    fun getScoredCandidates(
        x: IntArray,
        y: IntArray,
        t: IntArray
    ): List<ScoredTokenSequenceCandidate> {
        val encoderInputArgs = xytTransform(x, y, t)
        val encodedEValue = encoderModule.execute("encode", *encoderInputArgs)[0]
        return decodingAlgorithm.decode(encodedEValue)
    }

    override fun decodeSwipe(
        x: IntArray,
        y: IntArray,
        t: IntArray
    ): List<String> {
        return getScoredCandidates(x, y, t).map{subwordTokenizer.detokenize(it.tokensSequence)}
    }
}