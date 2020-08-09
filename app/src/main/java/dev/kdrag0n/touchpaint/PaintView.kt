package dev.kdrag0n.touchpaint

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

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
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private var fingers = 0
    private val paths = mutableListOf<Path>()
    private val curPaths = arrayOfNulls<Path>(MAX_FINGERS)
    private val fingerDown = Array(MAX_FINGERS) { false }
    private val pathStarted = Array(MAX_FINGERS) { false }

    var measureSampleRate = false
        set(value) {
            field = value
            if (!value) {
                stopSampleRate()
            }
        }

    private var eventsReceived = 0
    private var lastToast: Toast? = null
    private val sampleRateRunnable = Runnable {
        lastToast?.cancel()
        lastToast = null

        val toast = Toast.makeText(context, "Touch sample rate: $eventsReceived Hz", Toast.LENGTH_SHORT)
        toast.show()
        lastToast = toast

        eventsReceived = 0
        kickSampleRate()
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawPaint(bgPaint)
        for (path in paths) {
            canvas?.drawPath(path, brushPaint)
        }
    }

    fun setBrushSize(size: Float) {
        brushPaint.strokeWidth = size
    }

    private fun clearCanvas() {
        paths.clear()
        for (i in curPaths.indices) {
            curPaths[i] = null
        }
    }

    private fun kickSampleRate() {
        if (measureSampleRate) {
            postDelayed(sampleRateRunnable, 1000)
        }
    }

    private fun stopSampleRate() {
        removeCallbacks(sampleRateRunnable)
    }

    private fun fingerDown(slot: Int) {
        if (fingerDown[slot]) {
            return
        }

        fingerDown[slot] = true
        fingers++
        if (fingers == 1) {
            clearCanvas()
            kickSampleRate()
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
        eventsReceived++
    }

    private fun fingerUp(slot: Int) {
        if (!fingerDown[slot]) {
            return
        }

        curPaths[slot] = null
        pathStarted[slot] = false
        fingerDown[slot] = false
        fingers--

        if (fingers == 0) {
            stopSampleRate()
        }
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
                for (p in 0 until event.pointerCount) {
                    val slot = event.getPointerId(p)

                    for (h in 0 until event.historySize) {
                        fingerMove(slot, event.getHistoricalX(p, h), event.getHistoricalY(p, h))
                    }

                    fingerMove(slot, event.getX(p), event.getY(p))
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