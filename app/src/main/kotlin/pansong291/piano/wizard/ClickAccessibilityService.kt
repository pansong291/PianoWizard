package pansong291.piano.wizard

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.PointF
import android.view.accessibility.AccessibilityEvent
import org.greenrobot.eventbus.EventBus
import pansong291.piano.wizard.events.AccessibilityConnectedEvent

class ClickAccessibilityService : AccessibilityService() {
    var serviceEnabled = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        EventBus.getDefault().post(AccessibilityConnectedEvent())
        serviceEnabled = true
        Thread {
            Thread.sleep(7000)
            click(
                listOf(
                    PointF(500f, 900f),
                    PointF(500f, 800f),
                    PointF(400f, 800f),
                    PointF(400f, 900f)
                ), 1000
            )
        }.start()
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onUnbind(intent: Intent?): Boolean {
        serviceEnabled = false
        return super.onUnbind(intent)
    }

    private fun click(points: List<PointF>, duration: Long) {
        dispatchGesture(GestureDescription.Builder().apply {
            points.forEach {
                addStroke(GestureDescription.StrokeDescription(Path().apply {
                    moveTo(it.x, it.y)
                }, 0, duration))
            }
        }.build(), null, null)
    }
}
