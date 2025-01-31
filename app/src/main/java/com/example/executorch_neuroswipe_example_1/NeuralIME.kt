package com.example.executorch_neuroswipe_example_1

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import com.example.executorch_neuroswipe_example_1.ngtFeaturesExtraction.getDefaultGrid

class NeuralIME : InputMethodService() {
    private var keyboardView: KeyboardView? = null

    override fun onCreateInputView(): View {
        keyboardView = KeyboardView(this)
        return keyboardView!!
    }

    override fun onStartInputView(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(attribute, restarting)
        keyboardView?.setKeyboard(getDefaultGrid())
    }
}


