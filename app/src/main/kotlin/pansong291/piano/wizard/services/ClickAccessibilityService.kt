package pansong291.piano.wizard.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Point
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class ClickAccessibilityService : AccessibilityService() {
    companion object {
        var onVolumeKeyDown: (() -> Unit)? = null
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
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        when (event?.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    onVolumeKeyDown?.invoke()
                }
            }
        }
        return super.onKeyEvent(event)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        aService = null
        return super.onUnbind(intent)
    }
}
