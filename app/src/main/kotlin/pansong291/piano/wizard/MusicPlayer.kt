package pansong291.piano.wizard

import android.graphics.Point
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pansong291.piano.wizard.entity.ClickAction
import pansong291.piano.wizard.entity.KeyLayout
import pansong291.piano.wizard.entity.MusicNotation
import pansong291.piano.wizard.exceptions.MissingKeyException
import pansong291.piano.wizard.services.ClickAccessibilityService
import pansong291.piano.wizard.utils.MusicUtil

object MusicPlayer {
    private val handler = Handler(Looper.getMainLooper())

    // 使用 CONFLATED 模式，只保存最新状态，未处理的旧状态会被丢弃
    private val controlChannel = Channel<Boolean>(capacity = Channel.CONFLATED)
    private var job: Job? = null
    var onStopped: (() -> Unit)? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun startPlay(
        scope: CoroutineScope,
        mn: MusicNotation,
        kl: KeyLayout,
        offset: Int,
        ignoreMissingKey: Boolean = false
    ) {
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
                points = it.tones.mapNotNull { keyMap[it + mn.keyNote + offset] }
                delay = (it.durationRate * baseTime).toInt()
                if (!ignoreMissingKey && points.size < it.tones.size) {
                    throw MissingKeyException()
                }
            }
        }

        job = scope.launch {
            var isPaused = false
            clickActions.forEach {
                if (!isActive) return@forEach
                if (!controlChannel.isEmpty) {
                    isPaused = controlChannel.receive()
                }
                while (isPaused) {
                    isPaused = controlChannel.receive()
                }
                val time = System.currentTimeMillis()
                ClickAccessibilityService.click(it.points, it.delay.toLong())
                val rest = it.delay + time - System.currentTimeMillis()
                if (rest > 0) Thread.sleep(rest)
            }
        }
        job?.invokeOnCompletion {
            job = null
            onStopped?.let { handler.post(it) }
        }
    }

    fun pause() {
        controlChannel.trySend(true)
    }

    fun resume() {
        controlChannel.trySend(false)
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    fun isPlaying(): Boolean {
        return job != null
    }
}
