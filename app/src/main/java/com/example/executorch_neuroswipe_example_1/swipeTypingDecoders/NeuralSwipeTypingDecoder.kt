package com.example.executorch_neuroswipe_example_1.swipeTypingDecoders

import com.example.executorch_neuroswipe_example_1.decodingAlgorithms.DecodingAlgorithm
import com.example.executorch_neuroswipe_example_1.decodingAlgorithms.ScoredTokenSequenceCandidate
import org.pytorch.executorch.Module
import com.example.executorch_neuroswipe_example_1.tokenizers.StringTokenizer
import org.pytorch.executorch.EValue


class NeuralSwipeTypingDecoder(
    private val encoderDecoderModule: Module,
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
        val encodedEValue = encoderDecoderModule.execute("encode", *encoderInputArgs)[0]
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