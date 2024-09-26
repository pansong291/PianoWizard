package pansong291.piano.wizard

import android.graphics.Point
import pansong291.piano.wizard.entity.ClickAction
import pansong291.piano.wizard.entity.KeyLayout
import pansong291.piano.wizard.entity.MusicNotation
import pansong291.piano.wizard.services.ClickAccessibilityService
import pansong291.piano.wizard.utils.MusicUtil

object PlayMusicThreadManager {
    private var thread: Thread? = null
    var onStopped: (() -> Unit)? = null

    fun startMusic(mn: MusicNotation, kl: KeyLayout, offset: Int) {
        val keyMap = mutableMapOf<Int, Point>()
        val baseTime = 60_000f / mn.bpm

        // 构建十二平均律到按键点位的映射关系
        kl.points.forEachIndexed { index, point ->
            keyMap[if (!kl.semitone) MusicUtil.basicNoteTo12TET(index) else index] = Point(
                (point.x + kl.rawOffset.x).toInt(),
                (point.y + kl.rawOffset.y).toInt()
            )
        }
        val clickActions = mn.beats.map {
            ClickAction().apply {
                points = it.tones.mapNotNull { keyMap[it + offset] }
                delay = (it.durationRate * baseTime).toInt()
            }
        }

        thread = Thread {
            try {
                clickActions.forEach {
                    val time = System.currentTimeMillis()
                    ClickAccessibilityService.click(it.points, it.delay.toLong())
                    while (System.currentTimeMillis() - time < it.delay) {
                    }
                }
            } finally {
                onStopped?.invoke()
            }
        }.apply { start() }
    }

    fun stop() {
        thread?.interrupt()
        thread = null
    }

    fun isPlaying(): Boolean {
        return thread != null
    }
}
