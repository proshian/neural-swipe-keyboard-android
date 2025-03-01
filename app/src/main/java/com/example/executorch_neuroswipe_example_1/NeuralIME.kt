package com.example.executorch_neuroswipe_example_1

import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.executorch_neuroswipe_example_1.ngtFeaturesExtraction.getDefaultGrid
import com.example.executorch_neuroswipe_example_1.assertUtils.AssetUtils
import com.example.executorch_neuroswipe_example_1.decodingAlgorithms.GreedySearch
import com.example.executorch_neuroswipe_example_1.ngtFeaturesExtraction.TrajFeatsGetter
import com.example.executorch_neuroswipe_example_1.ngtFeaturesExtraction.FeatureExtractor
import com.example.executorch_neuroswipe_example_1.ngtFeaturesExtraction.NearestKeysGetter
import com.example.executorch_neuroswipe_example_1.ngtFeaturesExtraction.FeatureExtractorAggregator
import com.example.executorch_neuroswipe_example_1.swipeTypingDecoders.NeuralSwipeTypingDecoder
import com.example.executorch_neuroswipe_example_1.tokenizers.RuSubwordTokenizer
import org.pytorch.executorch.EValue
import org.pytorch.executorch.Module
import org.pytorch.executorch.Tensor
import java.io.IOException

class NeuralIME : InputMethodService() {
    private var keyboardView: KeyboardView? = null
    private var candidatesRecyclerView: RecyclerView? = null
    private lateinit var candidatesAdapter: CandidateAdapter
    private lateinit var neuralSwipeTypingDecoder: NeuralSwipeTypingDecoder

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

            val greedySearch = GreedySearch(
                encoderDecoderModule,
                sosToken = 36,
                eosToken = 33,
                maxSteps = 35
            )
            val subwordTokenizer = RuSubwordTokenizer()

            val allowedKeyLabels = arrayOf(
                "а", "б", "в", "г", "д", "е", "ë", "ж", "з", "и", "й",
                "к", "л", "м", "н", "о", "п", "р", "с", "т", "у", "ф",
                "х", "ц", "ч", "ш", "щ", "ъ", "ы", "ь", "э", "ю", "я"
            )
            val nearestKeysGetter = NearestKeysGetter(allowedKeyLabels)
            val trajFeatsGetter = TrajFeatsGetter()

            val coordFeatsAndNearestKeyGetter = FeatureExtractorAggregator(
                listOf(trajFeatsGetter, nearestKeysGetter)
            )

            neuralSwipeTypingDecoder = NeuralSwipeTypingDecoder(
                encoderDecoderModule,
                greedySearch,
                subwordTokenizer,
                coordFeatsAndNearestKeyGetter
            )
        } catch (e: IOException) {
            Log.e("NeuralIME", "Decoder initialization failed", e)
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
            // Handle candidate selection
            currentInputConnection?.commitText(" $selectedCandidate", 1)
        }



        // Candidates RecyclerView
        candidatesRecyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.candidates_bar_height)
            ).apply {
                weight = 1f
            }
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = candidatesAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL))
            background = ContextCompat.getDrawable(context, R.drawable.candidates_background)
        }

        // Keyboard View
        keyboardView = KeyboardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 3f
            }
        }

        rootView.addView(candidatesRecyclerView)
        rootView.addView(keyboardView)

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }


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
        keyboardView?.setKeyboard(getDefaultGrid())
    }
}
