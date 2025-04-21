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
import io.github.proshian.neuralswipetyping.swipeTypingDecoders.NeuralSwipeTypingDecoder
import io.github.proshian.neuralswipetyping.swipeTypingDecoders.NeuralSwipeTypingDecoderConfig
import io.github.proshian.neuralswipetyping.swipeTypingDecoders.StandardNeuralSwipeTypingDecoderFactory
import kotlinx.serialization.json.Json



class NeuralIME : InputMethodService() {
    private var keyboardView: KeyboardView? = null
    private var candidatesRecyclerView: RecyclerView? = null
    private lateinit var candidatesAdapter: CandidateAdapter
    private val swipeDecoderFactory = StandardNeuralSwipeTypingDecoderFactory()
    private lateinit var neuralSwipeTypingDecoder: NeuralSwipeTypingDecoder
    private var currentGridName = "ru_default"

    override fun onCreate() {
        super.onCreate()
        initializeSwipeDecoder()
    }

    private fun initializeSwipeDecoder() {
        fun loadConfig(configPath: String): NeuralSwipeTypingDecoderConfig {
            val json = assets.open(configPath).use { it.reader().readText() }
            return Json.decodeFromString(json)
        }

        try {
            val config = loadConfig("swipeTypingDecoderConfigs/${currentGridName}.json")
            neuralSwipeTypingDecoder = swipeDecoderFactory.create(applicationContext, config)
        } catch (e: Exception) {
            Log.e("NeuralIME", "Swipe decoder init failed", e)
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
        val keyboardGrid = keyboardGridReader.readKeyboardGridFromAssets("keyboardLayouts/${currentGridName}.json")
        keyboardView?.setKeyboard(keyboardGrid)
    }
}
