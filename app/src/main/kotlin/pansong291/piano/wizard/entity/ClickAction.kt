package pansong291.piano.wizard.entity

import android.graphics.Point

/**
 * 点击操作
 */
class ClickAction {
    /**
     * 要点击的位置
     */
    var points: List<Point> = emptyList()

    /**
     * 点击后的延时
     */
    var delay: Int = 0
}
