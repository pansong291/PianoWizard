package pansong291.piano.wizard.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Point
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import pansong291.piano.wizard.R

class ClickAccessibilityService : AccessibilityService() {
    companion object {
        var onVolumeKeyDown: (() -> Unit)? = null
        private var aService: ClickAccessibilityService? = null

        fun click(points: List<Point>, duration: Long) {
            catching {
                if (points.isEmpty()) return
                aService?.dispatchGesture(GestureDescription.Builder().apply {
                    points.forEach {
                        addStroke(GestureDescription.StrokeDescription(Path().apply {
                            moveTo(it.x.toFloat(), it.y.toFloat())
                        }, 0, duration))
                    }
                }.build(), null, null)
            }
        }

        fun checkAccessibility(viewId: String): Boolean {
            catching {
                return aService?.rootInActiveWindow?.findAccessibilityNodeInfosByViewId(viewId)
                    ?.find {
                        it.actionList.find { it.id == R.id.action_accessibility_check } != null
                    }?.performAction(R.id.action_accessibility_check) ?: false
            }
            return false
        }

        private inline fun catching(block: () -> Unit) {
            try {
                block()
            } catch (e: Throwable) {
                e.printStackTrace()
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
        catching {
            when (event?.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        onVolumeKeyDown?.invoke()
                    }
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
