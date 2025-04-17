package com.example.neuralSwipeKeyboardProject

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import io.github.proshian.neuralswipetyping.keyboardGrid.KeyboardGrid
import io.github.proshian.neuralswipetyping.keyboardGrid.KeyboardKey



class KeyboardView(context: Context) : View(context) {
    private var keyboardGrid: KeyboardGrid? = null
    private val paint = Paint()
    private var scale: Float = 1f

    private val xPoints = mutableListOf<Int>()
    private val yPoints = mutableListOf<Int>()
    private val tPoints = mutableListOf<Int>()
    private var startTime: Long = 0

    var onSwipeListener: OnSwipeListener? = null

    interface OnSwipeListener {
        fun onSwipeCompleted(x: IntArray, y: IntArray, t: IntArray)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                xPoints.clear()
                yPoints.clear()
                tPoints.clear()
                startTime = event.eventTime
                addPoint(event)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                addPoint(event)
                invalidate()
                return true
            }

            MotionEvent.ACTION_UP -> {
                addPoint(event)
                val x = xPoints.toIntArray()
                val y = yPoints.toIntArray()
                val t = tPoints.toIntArray()
                onSwipeListener?.onSwipeCompleted(x, y, t)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun addPoint(event: MotionEvent) {
        val x = (event.x / scale).toInt()
        val y = (event.y / scale).toInt()
        val t = (event.eventTime - startTime).toInt()
        xPoints.add(x)
        yPoints.add(y)
        tPoints.add(t)
    }


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

        when (key) {
            is KeyboardKey.CharacterKey -> {
                canvas.drawText(
                    key.label,
                    hitbox.x * scale + hitbox.w / 2f * scale,
                    hitbox.y * scale + hitbox.h * scale / 2f + verticalOffset,
                    paint
                )
            }
            is KeyboardKey.ActionKey -> {

            }
        }
    }
}
