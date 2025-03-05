package pansong291.piano.wizard.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import pansong291.piano.wizard.utils.MusicUtil
import pansong291.piano.wizard.utils.ViewUtil.dp
import pansong291.piano.wizard.utils.ViewUtil.sp

@SuppressLint("ClickableViewAccessibility")
class KeysLayoutView(context: Context) : View(context) {
    var showNum = true
        set(value) {
            field = value
            postInvalidate()
        }
    var points: List<Point> = emptyList()
        set(value) {
            field = value
            postInvalidate()
        }
    var semitone: Boolean = false
        set(value) {
            field = value
            postInvalidate()
        }
    var pointOffset = 0
        set(value) {
            field = value
            postInvalidate()
        }

    private val markerPaint = Paint()
    private val textPaint = Paint()
    private val fillPaint = Paint()
    private val strokePaint = Paint()

    private val largeRadius = 18.dp()
    private val smallRadius = 4.dp()
    private val textCenterY: Float

    private val marker = Point(-1, -1)
    private val touchStart = PointF()
    private val markerStart = Point()
    val rawOffset = PointF()

    init {
        markerPaint.color = Color.RED
        markerPaint.style = Paint.Style.STROKE
        markerPaint.strokeWidth = 1.dp()

        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 18.sp()
        textPaint.isFakeBoldText = true
        textPaint.textAlign = Paint.Align.CENTER

        fillPaint.style = Paint.Style.FILL
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = 1.dp()
        // 计算文字高度中心点的偏移
        textCenterY = (textPaint.fontMetrics.ascent + textPaint.fontMetrics.descent) / 2

        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    rawOffset.set(event.rawX - event.x, event.rawY - event.y)
                    touchStart.set(event.x, event.y)
                    markerStart.set(marker.x, marker.y)
                }

                MotionEvent.ACTION_MOVE -> {
                    setMarker(
                        Point(
                            (markerStart.x + event.x - touchStart.x).toInt(),
                            (markerStart.y + event.y - touchStart.y).toInt()
                        )
                    )
                }
            }
            true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawARGB(50, 0, 0, 0)
        val fx = marker.x.toFloat()
        val fy = marker.y.toFloat()
        // 绘制全部点位
        points.forEachIndexed { i, p ->
            val pfx = p.x.toFloat()
            val pfy = p.y.toFloat()
            val note = i + pointOffset
            toggleColor(note)
            if (showNum) {
                canvas.drawCircle(pfx, pfy, largeRadius, fillPaint)
                canvas.drawText(note.toString(), pfx, pfy - textCenterY, textPaint)
            } else {
                canvas.drawCircle(pfx, pfy, smallRadius, fillPaint)
                canvas.drawCircle(pfx, pfy, smallRadius, strokePaint)
            }
        }
        // 绘制定位线
        canvas.drawLine(0f, fy, width.toFloat(), fy, markerPaint)
        canvas.drawLine(fx, 0f, fx, height.toFloat(), markerPaint)
    }

    private fun toggleColor(i: Int) {
        if (semitone && MusicUtil.isSemitone(i)) useSemitoneColor()
        else useNaturalColor()
    }

    private fun useNaturalColor() {
        textPaint.color = Color.BLACK
        fillPaint.color = Color.WHITE
        strokePaint.color = Color.BLACK
    }

    private fun useSemitoneColor() {
        textPaint.color = Color.WHITE
        fillPaint.color = Color.BLACK
        strokePaint.color = Color.WHITE
    }

    fun isMarkerOutOfView(): Boolean {
        if (marker.x < 0 || marker.x >= width) return true
        return marker.y < 0 || marker.y >= height
    }

    fun resetMarker() {
        setMarker(Point(width / 2, height / 2))
    }

    fun getMarker() = marker

    fun setMarker(p: Point) {
        marker.set(p.x, p.y)
        postInvalidate()
    }
}
