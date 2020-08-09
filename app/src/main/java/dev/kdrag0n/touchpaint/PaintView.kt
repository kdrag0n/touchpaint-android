package dev.kdrag0n.touchpaint

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
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

    private val fgPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val brushPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = resources.getDimension(R.dimen.default_brush_size)
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }

    private val boxPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = resources.getDimension(R.dimen.follow_box_size)
        strokeCap = Paint.Cap.SQUARE
        isAntiAlias = true
    }

    var mode = PaintMode.PAINT
        set(value) {
            field = value

            removeCallbacks(clearRunnable)
            clearCanvas()
            invalidate()
        }

    private var fingers = 0
    private lateinit var bitmap: Bitmap
    private lateinit var bufCanvas: Canvas
    private val lastPoint = Array(MAX_FINGERS) { PointF(-1f, -1f) }
    private val fingerDown = Array(MAX_FINGERS) { false }
    private var fillDown = false

    var measureEventRate = false
        set(value) {
            field = value
            if (!value) {
                stopSampleRate()
            }
        }

    // 0 = on next stroke, -1 = never, * = delay in ms
    var paintClearDelay = 0L

    private var eventsReceived = 0
    private var lastToast: Toast? = null
    private val sampleRateRunnable = Runnable {
        lastToast?.cancel()
        lastToast = null

        val toast = Toast.makeText(context, "Touch event rate: $eventsReceived Hz", Toast.LENGTH_SHORT)
        toast.show()
        lastToast = toast

        kickSampleRate()
    }

    private val clearRunnable = Runnable {
        clearCanvas()
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        when (mode) {
            PaintMode.PAINT -> canvas!!.drawBitmap(bitmap, 0f, 0f, null)
            PaintMode.FILL -> canvas!!.drawPaint(if (fillDown) fgPaint else bgPaint)
            PaintMode.FOLLOW -> {
                canvas!!.drawPaint(bgPaint)

                for (point in lastPoint) {
                    if (point.x != -1f && point.y != -1f) {
                        canvas.drawPoint(point.x, point.y, boxPaint)
                    }
                }
            }
            PaintMode.BLANK -> {
                canvas!!.drawPaint(bgPaint)
                invalidate()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
        bufCanvas = Canvas(bitmap)
        clearCanvas()
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }

    fun setBrushSize(dp: Float) {
        brushPaint.strokeWidth = dpToPx(dp)
    }

    private fun clearCanvas() {
        fillDown = false
        bufCanvas.drawPaint(bgPaint)
        lastPoint.forEach {
            it.x = -1f
            it.y = -1f
        }
    }

    private fun kickSampleRate() {
        eventsReceived = 0

        if (measureEventRate) {
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
            when (mode) {
                PaintMode.PAINT -> {
                    if (paintClearDelay > 0)
                        removeCallbacks(clearRunnable)
                    else if (paintClearDelay == 0L)
                        clearCanvas()
                }
                PaintMode.FILL -> {
                    removeCallbacks(clearRunnable)
                    fillDown = true
                }
                else -> {}
            }

            kickSampleRate()
        }
    }

    private fun fingerMove(slot: Int, x: Float, y: Float) {
        if (!fingerDown[slot]) {
            return
        }

        if (mode == PaintMode.PAINT && lastPoint[slot].x != -1f && lastPoint[slot].y != -1f) {
            bufCanvas.drawLine(x, y, lastPoint[slot].x, lastPoint[slot].y, brushPaint)
        }

        lastPoint[slot].x = x
        lastPoint[slot].y = y
    }

    private fun fingerUp(slot: Int) {
        if (!fingerDown[slot]) {
            return
        }

        lastPoint[slot].x = -1f
        lastPoint[slot].y = -1f
        fingerDown[slot] = false
        fingers--

        if (fingers == 0) {
            when (mode) {
                PaintMode.PAINT -> if (paintClearDelay > 0) {
                    postDelayed(clearRunnable, paintClearDelay)
                }
                PaintMode.FILL -> postDelayed(clearRunnable, 250)
                else -> {}
            }

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

                if (measureEventRate) {
                    eventsReceived++
                }
            }
            MotionEvent.ACTION_POINTER_UP -> fingerUp(event.actionIndex)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> allFingersUp()
        }

        invalidate()
        return true
    }
}