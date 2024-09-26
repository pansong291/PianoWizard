package pansong291.piano.wizard.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Point
import android.view.accessibility.AccessibilityEvent
import org.greenrobot.eventbus.EventBus
import pansong291.piano.wizard.events.AccessibilityConnectedEvent

class ClickAccessibilityService : AccessibilityService() {
    companion object {
        private var aService: ClickAccessibilityService? = null

        fun click(points: List<Point>, duration: Long) {
            if (points.isEmpty()) return
            aService?.apply {
                dispatchGesture(GestureDescription.Builder().apply {
                    points.forEach {
                        addStroke(GestureDescription.StrokeDescription(Path().apply {
                            moveTo(it.x.toFloat(), it.y.toFloat())
                        }, 0, duration))
                    }
                }.build(), null, null)
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        aService = this
        EventBus.getDefault().post(AccessibilityConnectedEvent())
        Thread {
            Thread.sleep(7000)
            click(
                listOf(
                    Point(500, 900),
                    Point(500, 800),
                    Point(400, 800),
                    Point(400, 900)
                ), 1000
            )
        }.start()
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onUnbind(intent: Intent?): Boolean {
        aService = null
        return super.onUnbind(intent)
    }
}
