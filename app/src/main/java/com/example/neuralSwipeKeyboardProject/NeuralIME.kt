package com.example.neuralSwipeKeyboardProject

import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.proshian.neuralswipetyping.keyboardGrid.KeyboardGridReader
import com.example.neuralSwipeKeyboardProject.assertUtils.AssetUtils
import io.github.proshian.neuralswipetyping.decodingAlgorithms.BeamSearch
import io.github.proshian.neuralswipetyping.decodingAlgorithms.BeamSearchBatched
import io.github.proshian.neuralswipetyping.logitsProcessors.VocabularyLogitsProcessorPrebuiltTrieBased
import io.github.proshian.neuralswipetyping.logitsProcessors.VocabularyLogitsProcessorTrieBased
import io.github.proshian.neuralswipetyping.logitsProcessors.VocabularyLogitsProcessorMapBased
import io.github.proshian.neuralswipetyping.swipePointFeaturesExtraction.TrajFeatsGetter
import io.github.proshian.neuralswipetyping.swipePointFeaturesExtraction.NearestKeysGetter
import io.github.proshian.neuralswipetyping.swipePointFeaturesExtraction.FeatureExtractorAggregator
import io.github.proshian.neuralswipetyping.swipeTypingDecoders.NeuralSwipeTypingDecoder
import io.github.proshian.neuralswipetyping.tokenizers.RuSubwordTokenizer
import org.pytorch.executorch.Module
import java.io.IOException



class NeuralIME : InputMethodService() {
    private var keyboardView: KeyboardView? = null
    private var candidatesRecyclerView: RecyclerView? = null
    private lateinit var candidatesAdapter: CandidateAdapter
    private lateinit var neuralSwipeTypingDecoder: NeuralSwipeTypingDecoder
    private var currentGridName = "default"

    override fun onCreate() {
        super.onCreate()
        initializeDecoder()
    }

    private fun initializeDecoder() {
        try {
            val modelFileName = "xnnpack_my_nearest_feats.pte"
            val modelPath = AssetUtils.assetFilePath(applicationContext, modelFileName)
            val encoderDecoderModule = Module.load(modelPath)
                ?: throw IllegalStateException("Model loading failed")

            val subwordTokenizer = RuSubwordTokenizer()

//            val vocab = loadVocabulary("voc.txt")
//            val logitsProcessor = VocabularyLogitsProcessorMapBased(subwordTokenizer, vocab)
//            val logitsProcessor = VocabularyLogitsProcessorTrieBased(subwordTokenizer, vocab)

            val logitsProcessor = VocabularyLogitsProcessorPrebuiltTrieBased(
                applicationContext, "trie.ser")


            val decodingAlgorithm = BeamSearchBatched(
                encoderDecoderModule,
                sosToken = subwordTokenizer.sosTokenId,
                eosToken = subwordTokenizer.eosTokenId,
                maxSteps = 35,
                beamSize = 5,
                logitsProcessor=logitsProcessor
            )


            val keyboardGridReader = KeyboardGridReader(this)
            val keyboardGrid = keyboardGridReader.readKeyboardGridFromAssets("${currentGridName}.json")
            val nearestKeysGetter = NearestKeysGetter(keyboardGrid)
            val trajFeatsGetter = TrajFeatsGetter()

            val coordFeatsAndNearestKeyGetter = FeatureExtractorAggregator(
                listOf(trajFeatsGetter, nearestKeysGetter)
            )

            neuralSwipeTypingDecoder = NeuralSwipeTypingDecoder(
                encoderDecoderModule,
                decodingAlgorithm,
                subwordTokenizer,
                coordFeatsAndNearestKeyGetter
            )
        } catch (e: IOException) {
            Log.e("NeuralIME", "Decoder initialization failed", e)
        }
    }


    private fun loadVocabulary(filename: String): List<String> {
        return assets.open(filename).bufferedReader().useLines {
            it.filterNot { line -> line.isBlank() }.toList()
        }
    }


    override fun onCreateInputView(): View {
        val rootView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }


        candidatesAdapter = CandidateAdapter(emptyList()) { selectedCandidate ->
            currentInputConnection?.commitText(" $selectedCandidate", 1)
        }


        candidatesRecyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.candidates_bar_height)
            )
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = candidatesAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL))
            background = ContextCompat.getDrawable(context, R.drawable.candidates_background)
        }


        keyboardView = KeyboardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        rootView.addView(candidatesRecyclerView)
        rootView.addView(keyboardView)


        keyboardView?.onSwipeListener = object : KeyboardView.OnSwipeListener {
            override fun onSwipeCompleted(x: IntArray, y: IntArray, t: IntArray) {
                Thread {
                    val candidates = neuralSwipeTypingDecoder.decodeSwipe(x, y, t)
                    Handler(Looper.getMainLooper()).post {
                        candidatesAdapter.updateCandidates(candidates)
                    }
                }.start()
            }
        }

        return rootView
    }

    override fun onStartInputView(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(attribute, restarting)
        val keyboardGridReader = KeyboardGridReader(this)
        val keyboardGrid = keyboardGridReader.readKeyboardGridFromAssets("${currentGridName}.json")
        keyboardView?.setKeyboard(keyboardGrid)
    }
}
