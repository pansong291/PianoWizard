package pansong291.piano.wizard.entity

import android.graphics.Point

/**
 * 键盘布局
 */
class KeyLayout {
    /**
     * 布局名称
     */
    var name: String = ""

    /**
     * 点位，按顺序从低音到高音
     */
    var points: List<Point> = emptyList()

    /**
     * 是否启用半音，即十二平均律
     */
    var semitone: Boolean = false
}
