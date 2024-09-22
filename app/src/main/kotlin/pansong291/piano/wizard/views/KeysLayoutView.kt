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

@SuppressLint("ClickableViewAccessibility")
class KeysLayoutView(context: Context) : View(context) {
    private var showNum = true
    private var points: List<Point> = emptyList()
    private val indicator = Point()
    private val indicatorPaint = Paint()
    private val pointPaint = Paint()
    private val numPaint = Paint()
    private val textCenterY: Float
    private val touchStart = PointF()
    private val indicatorStart = Point()

    init {
        indicatorPaint.color = Color.RED
        indicatorPaint.style = Paint.Style.STROKE
        indicatorPaint.strokeWidth = 1f
        pointPaint.color = Color.WHITE
        pointPaint.style = Paint.Style.FILL
        numPaint.color = Color.BLACK
        numPaint.textSize = 48f
        numPaint.isFakeBoldText = true
        numPaint.textAlign = Paint.Align.CENTER
        // 计算文字高度中心点的偏移
        textCenterY = (numPaint.fontMetrics.ascent + numPaint.fontMetrics.descent) / 2

        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
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
        canvas.drawARGB(20, 0, 0, 0)
        val fx = indicator.x.toFloat()
        val fy = indicator.y.toFloat()
        points.forEachIndexed { i, p ->
            val pfx = p.x.toFloat()
            val pfy = p.y.toFloat()
            if (showNum) {
                canvas.drawCircle(pfx, pfy, 40f, pointPaint)
                canvas.drawText((i + 1).toString(), pfx, pfy - textCenterY, numPaint)
            } else {
                canvas.drawCircle(pfx, pfy, 15f, pointPaint)
                canvas.drawCircle(pfx, pfy, 10f, numPaint)
            }
        }
        canvas.drawLine(0f, fy, width.toFloat(), fy, indicatorPaint)
        canvas.drawLine(fx, 0f, fx, height.toFloat(), indicatorPaint)
    }

    fun resetIndicator() {
        setIndicator(Point(width / 2, height / 2))
    }

    fun getIndicator(): Point {
        return indicator
    }

    fun setIndicator(p: Point) {
        indicator.set(p.x, p.y)
        postInvalidate()
    }

    fun getPoints(): List<Point> {
        return points
    }

    fun setPoints(p: List<Point>) {
        points = p
        postInvalidate()
    }

    fun isShowNum(): Boolean {
        return showNum
    }

    fun setShowNum(s: Boolean) {
        showNum = s
        postInvalidate()
    }
}
