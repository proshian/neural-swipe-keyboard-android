package com.example.executorch_neuroswipe_example_1

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.example.executorch_neuroswipe_example_1.ngtFeaturesExtraction.getDefaultGrid

class NeuralIME : InputMethodService() {
    private var keyboardView: KeyboardView? = null

    override fun onCreateInputView(): View {
        val frameLayout = FrameLayout(this)
        frameLayout.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        keyboardView = KeyboardView(this)

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.BOTTOM
        keyboardView!!.layoutParams = params

        frameLayout.addView(keyboardView)
        return frameLayout
    }


    override fun onStartInputView(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(attribute, restarting)
        keyboardView?.setKeyboard(getDefaultGrid())
    }
}
