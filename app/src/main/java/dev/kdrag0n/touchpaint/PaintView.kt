package dev.kdrag0n.touchpaint

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

const val MAX_FINGERS = 10

class PaintView(context: Context, attrs: AttributeSet) : View(context, attrs)
{
    private val bgPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    private val brushPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private var fingers = 0
    private val paths = mutableListOf<Path>()
    private val curPaths = arrayOfNulls<Path>(MAX_FINGERS)
    private val fingerDown = Array(MAX_FINGERS) { false }
    private val pathStarted = Array(MAX_FINGERS) { false }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawPaint(bgPaint)
        for (path in paths) {
            canvas?.drawPath(path, brushPaint)
        }
    }

    private fun clearCanvas() {
        paths.clear()
        for (i in curPaths.indices) {
            curPaths[i] = null
        }
    }

    private fun fingerDown(slot: Int) {
        if (fingerDown[slot]) {
            return
        }

        fingerDown[slot] = true
        fingers++
        if (fingers == 1) {
            clearCanvas()
        }

        val path = Path()
        paths.add(path)
        curPaths[slot] = path
        pathStarted[slot] = false
    }

    private fun fingerMove(slot: Int, x: Float, y: Float) {
        if (curPaths[slot] == null) {
            fingerDown(slot)
        }

        if (!pathStarted[slot]) {
            curPaths[slot]!!.moveTo(x, y)
            pathStarted[slot] = true
        }

        curPaths[slot]!!.lineTo(x, y)
    }

    private fun fingerUp(slot: Int) {
        if (!fingerDown[slot]) {
            return
        }

        curPaths[slot] = null
        pathStarted[slot] = false
        fingerDown[slot] = false
        fingers--
    }

    private fun allFingersUp() {
        for (i in 0 until MAX_FINGERS) {
            fingerUp(i)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event!!.actionMasked) {
            MotionEvent.ACTION_DOWN -> fingerDown(0)
            MotionEvent.ACTION_POINTER_DOWN -> fingerDown(event.actionIndex)
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val slot = event.getPointerId(i)
                    val x = event.getX(i)
                    val y = event.getY(i)

                    fingerMove(slot, x, y)
                }
            }
            MotionEvent.ACTION_UP -> allFingersUp()
            MotionEvent.ACTION_POINTER_UP -> fingerUp(event.actionIndex)
            MotionEvent.ACTION_CANCEL -> allFingersUp()
        }

        invalidate()
        return true
    }
}