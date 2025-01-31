package com.example.executorch_neuroswipe_example_1

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.example.executorch_neuroswipe_example_1.ngtFeaturesExtraction.KeyboardGrid
import com.example.executorch_neuroswipe_example_1.ngtFeaturesExtraction.KeyboardKey

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
}


