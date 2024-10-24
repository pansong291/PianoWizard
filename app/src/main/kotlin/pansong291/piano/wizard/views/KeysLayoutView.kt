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
    private var showNum = true
    private var points: List<Point> = emptyList()
    private var semitone: Boolean = false
    private var pointOffset: Int = 0

    private val indicatorPaint = Paint()
    private val textPaint = Paint()
    private val fillPaint = Paint()
    private val strokePaint = Paint()

    private val largeRadius = 18.dp()
    private val smallRadius = 4.dp()
    private val textCenterY: Float

    private val indicator = Point(-1, -1)
    private val touchStart = PointF()
    private val indicatorStart = Point()
    val rawOffset = PointF()

    init {
        val dp1 = 1.dp()
        indicatorPaint.color = Color.RED
        indicatorPaint.style = Paint.Style.STROKE
        indicatorPaint.strokeWidth = dp1

        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 18.sp()
        textPaint.isFakeBoldText = true
        textPaint.textAlign = Paint.Align.CENTER

        fillPaint.style = Paint.Style.FILL
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = dp1
        // 计算文字高度中心点的偏移
        textCenterY = (textPaint.fontMetrics.ascent + textPaint.fontMetrics.descent) / 2

        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    rawOffset.set(event.rawX - event.x, event.rawY - event.y)
                    touchStart.set(event.x, event.y)
                    indicatorStart.set(indicator.x, indicator.y)
                }

                MotionEvent.ACTION_MOVE -> {
                    indicator.set(
                        (indicatorStart.x + event.x - touchStart.x).toInt(),
                        (indicatorStart.y + event.y - touchStart.y).toInt()
                    )
                    postInvalidate()
                }
            }
            true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawARGB(50, 0, 0, 0)
        val fx = indicator.x.toFloat()
        val fy = indicator.y.toFloat()
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
        // 绘制指示器
        canvas.drawLine(0f, fy, width.toFloat(), fy, indicatorPaint)
        canvas.drawLine(fx, 0f, fx, height.toFloat(), indicatorPaint)
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

    fun isIndicatorOutOfView(): Boolean {
        if (indicator.x < 0 || indicator.x >= width) return true
        return indicator.y < 0 || indicator.y >= height
    }

    fun resetIndicator() {
        setIndicator(Point(width / 2, height / 2))
    }

    fun getIndicator() = indicator

    fun setIndicator(p: Point) {
        indicator.set(p.x, p.y)
        postInvalidate()
    }

    fun getPoints() = points

    fun setPoints(p: List<Point>) {
        points = p
        postInvalidate()
    }

    fun getSemitone() = semitone

    fun setSemitone(s: Boolean) {
        semitone = s
        postInvalidate()
    }

    fun getPointOffset() = pointOffset

    fun setPointOffset(o: Int) {
        pointOffset = o
        postInvalidate()
    }

    fun isShowNum() = showNum

    fun setShowNum(s: Boolean) {
        showNum = s
        postInvalidate()
    }
}
