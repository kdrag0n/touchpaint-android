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

    private val paths = mutableListOf<Path>()
    private val curPaths = arrayOfNulls<Path>(MAX_FINGERS)

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawPaint(bgPaint)
        for (path in paths) {
            canvas?.drawPath(path, brushPaint)
        }
    }

    private fun fingerDown(slot: Int, x: Float, y: Float) {
        val path = Path()
        path.moveTo(x, y)
        paths.add(path)
        curPaths[slot] = path
    }

    private fun fingerMove(slot: Int, x: Float, y: Float) {
        if (curPaths[slot] == null) {
            fingerDown(slot, x, y)
        }

        curPaths[slot]!!.lineTo(x, y)
    }

    private fun fingerUp(slot: Int) {
        curPaths[slot] = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        for (i in 0 until event!!.pointerCount) {
            val slot = event.getPointerId(i)
            val x = event.getX(i)
            val y = event.getY(i)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> fingerDown(slot, x, y)
                MotionEvent.ACTION_MOVE -> fingerMove(slot, x, y)
                MotionEvent.ACTION_UP -> fingerUp(slot)
            }
        }

        invalidate()
        return true
    }
}