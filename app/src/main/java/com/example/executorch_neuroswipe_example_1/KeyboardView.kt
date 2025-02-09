package com.example.executorch_neuroswipe_example_1

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import com.example.executorch_neuroswipe_example_1.ngtFeaturesExtraction.KeyboardGrid
import com.example.executorch_neuroswipe_example_1.ngtFeaturesExtraction.KeyboardKey
import kotlin.math.round
//import com.example.executorch_neuroswipe_example_1.swipeTypingDecoders.NeuralSwipeTypingDecoder
//




//
//val swipePoints = mutableListOf<SwipePoint>()
//data class SwipePoint(val x: Float, val y: Float, val timestamp: Long)
//
//fun getSwipeCandidates() {
//    // Assuming your decoder expects an array of coordinates and time
//    val x = swipePoints.map { it.x }
//    val y = swipePoints.map { it.y }
//    val t = swipePoints.map { it.timestamp }
//
//    // Call the decoder to get the predicted candidates
//    val candidates = NeuroSwipeTypingDecoder.getCandidates(x, y, t)
//
//    // Now update your UI with the candidates
//    updateCandidatesInUI(candidates)
//}





class KeyboardView(context: Context) : View(context) {
    private var keyboardGrid: KeyboardGrid? = null
    private val paint = Paint()
    private var scale: Float = 1f


    init {
        paint.color = Color.BLACK
        paint.textSize = 50f
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (keyboardGrid == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        // Calculate available space
        val maxWidth = MeasureSpec.getSize(widthMeasureSpec)
        val maxHeight = MeasureSpec.getSize(heightMeasureSpec)

        // Calculate scale based on available space
        val widthScale = maxWidth.toFloat() / keyboardGrid!!.width.toFloat()
        val heightScale = maxHeight.toFloat() / keyboardGrid!!.height.toFloat()
        scale = minOf(widthScale, heightScale)

        // Set final dimensions
        val desiredWidth = (keyboardGrid!!.width * scale).toInt()
        val desiredHeight = (keyboardGrid!!.height * scale).toInt()

        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        keyboardGrid?.let {
//            scale = w.toFloat() / it.width.toFloat()
            scale = minOf(
                w.toFloat() / it.width.toFloat(),
                h.toFloat() / it.height.toFloat()
            )
            paint.textSize = 50f * scale
        }
    }


    fun setKeyboard(grid: KeyboardGrid) {
        keyboardGrid = grid
        scale = width.toFloat() / grid.width.toFloat()
        paint.textSize = 50f * scale
        invalidate()  // Redraw
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        keyboardGrid?.keys?.forEach { key ->
            drawKey(canvas, key)
        }
    }

    private fun drawKey(canvas: Canvas, key: KeyboardKey) {
        val hitbox = key.hitbox
        paint.color = Color.LTGRAY
        canvas.drawRect(
            hitbox.x.toFloat() * scale,
            hitbox.y.toFloat() * scale,
            (hitbox.x + hitbox.w).toFloat() * scale,
            (hitbox.y + hitbox.h).toFloat() * scale,
            paint
        )


        // Draw black border
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f  // Adjust border thickness here
        canvas.drawRect(
            hitbox.x.toFloat() * scale,
            hitbox.y.toFloat() * scale,
            (hitbox.x + hitbox.w).toFloat() * scale,
            (hitbox.y + hitbox.h).toFloat() * scale,
            paint
        )



        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        val metrics = paint.fontMetrics
        val verticalOffset = (metrics.descent - metrics.ascent) / 2 - metrics.descent
        canvas.drawText(
            key.label,
            hitbox.x * scale + hitbox.w / 2f * scale,
            hitbox.y * scale + hitbox.h * scale / 2f + verticalOffset,
            paint
        )
    }


//
//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        when (event.action) {
//            MotionEvent.ACTION_MOVE -> {
//                // Capture the current x, y, and timestamp
//                val x = event.x
//                val y = event.y
//                val timestamp = System.currentTimeMillis() // Get the current time in milliseconds
//                swipePoints.add(SwipePoint(x, y, timestamp)) // Accumulate swipe points
//            }
//            MotionEvent.ACTION_UP -> {
//                // When the swipe is finished, decode the candidates
//                getSwipeCandidates()
//            }
//        }
//        return true
//    }
}


