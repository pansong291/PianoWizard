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
import kotlin.math.abs

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
    private val handleArea = 24.dp()
    private var activeMaker = ActiveMaker.NONE
    private val snapArea = 8.dp()
    val rawOffset = PointF()

    init {
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
                    val offsetX = abs(markerStart.x - touchStart.x)
                    val offsetY = abs(markerStart.y - touchStart.y)
                    activeMaker = if (offsetX <= handleArea && offsetY > handleArea)
                        ActiveMaker.VERTICAL
                    else if (offsetY <= handleArea && offsetX > handleArea)
                        ActiveMaker.HORIZONTAL
                    else ActiveMaker.BOTH
                    postInvalidate()
                }

                MotionEvent.ACTION_MOVE -> {
                    val mp = Point(
                        (event.x + markerStart.x - touchStart.x).toInt(),
                        (event.y + markerStart.y - touchStart.y).toInt()
                    )
                    when (activeMaker) {
                        ActiveMaker.VERTICAL -> mp.y = markerStart.y
                        ActiveMaker.HORIZONTAL -> mp.x = markerStart.x
                        else -> for (it in points) {
                            if (abs(it.x - mp.x) <= snapArea && abs(it.y - mp.y) <= snapArea) {
                                mp.x = it.x
                                mp.y = it.y
                                break
                            }
                        }
                    }
                    setMarker(mp)
                }

                MotionEvent.ACTION_UP -> {
                    activeMaker = ActiveMaker.NONE
                    postInvalidate()
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
            toggleNotePointColor(note)
            if (showNum) {
                canvas.drawCircle(pfx, pfy, largeRadius, fillPaint)
                canvas.drawText(note.toString(), pfx, pfy - textCenterY, textPaint)
            } else {
                canvas.drawCircle(pfx, pfy, smallRadius, fillPaint)
                canvas.drawCircle(pfx, pfy, smallRadius, strokePaint)
            }
        }
        // 绘制定位线
        markerPaint.color = if (activeMaker == ActiveMaker.BOTH) Color.YELLOW else Color.RED
        when (activeMaker) {
            ActiveMaker.VERTICAL -> {
                canvas.drawLine(0f, fy, width.toFloat(), fy, markerPaint)
                markerPaint.color = Color.YELLOW
                canvas.drawLine(fx, 0f, fx, height.toFloat(), markerPaint)
            }

            ActiveMaker.HORIZONTAL -> {
                canvas.drawLine(fx, 0f, fx, height.toFloat(), markerPaint)
                markerPaint.color = Color.YELLOW
                canvas.drawLine(0f, fy, width.toFloat(), fy, markerPaint)
            }

            else -> {
                canvas.drawLine(0f, fy, width.toFloat(), fy, markerPaint)
                canvas.drawLine(fx, 0f, fx, height.toFloat(), markerPaint)
            }
        }
    }

    private fun toggleNotePointColor(i: Int) {
        if (semitone && MusicUtil.isSemitone(i)) {
            textPaint.color = Color.WHITE
            fillPaint.color = Color.BLACK
            strokePaint.color = Color.WHITE
        } else {
            textPaint.color = Color.BLACK
            fillPaint.color = Color.WHITE
            strokePaint.color = Color.BLACK
        }
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

    private enum class ActiveMaker {
        NONE, BOTH, VERTICAL, HORIZONTAL
    }
}
