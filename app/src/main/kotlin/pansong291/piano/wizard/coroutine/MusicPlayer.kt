package pansong291.piano.wizard.coroutine

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
import java.util.TreeSet

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
            var note = index + kl.keyOffset
            if (!kl.semitone) note = MusicUtil.basicNoteTo12TET(note)
            keyMap[note] = Point(
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
            Thread.sleep(200)
            clickActions.forEach {
                if (!isActive) return@forEach
                if (!controlChannel.isEmpty) {
                    isPaused = controlChannel.receive()
                }
                while (isPaused) {
                    isPaused = controlChannel.receive()
                    Thread.sleep(200)
                }
                val time = System.currentTimeMillis()
                ClickAccessibilityService.click(it.points, maxOf(it.delay - 100L, 1L))
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

    fun playKeyNote(key: Int, kl: KeyLayout, offset: Int) {
        val target = key + offset
        kl.points.forEachIndexed { index, point ->
            var note = index + kl.keyOffset
            if (!kl.semitone) note = MusicUtil.basicNoteTo12TET(note)
            if (target == note) {
                ClickAccessibilityService.click(
                    listOf(
                        Point(
                            (point.x + kl.rawOffset.x).toInt(),
                            (point.y + kl.rawOffset.y).toInt()
                        )
                    ), 1
                )
                return
            }
        }
        throw MissingKeyException()
    }

    fun findSuitableOffset(mn: MusicNotation, kl: KeyLayout): Int {
        val producer = kl.points.mapIndexedTo(TreeSet()) { index, _ ->
            val note = index + kl.keyOffset
            if (kl.semitone) note else MusicUtil.basicNoteTo12TET(note)
        }
        val consumer = mn.beats.flatMapTo(TreeSet()) {
            it.tones.map { it + mn.keyNote }
        }
        val minProducer = producer.firstOrNull() ?: throw MissingKeyException()
        val maxProducer = producer.last()
        val minConsumer = consumer.first()
        val maxConsumer = consumer.last()
        // minOffset 是使 consumer 的最小值对齐 producer 的最小值，maxOffset 同理
        val minOffset = minProducer - minConsumer
        val maxOffset = maxProducer - maxConsumer
        var offset = maxOf(0, minOffset)
        while (offset <= maxOffset) {
            if (consumer.all { producer.contains(it + offset) }) return offset
            offset++
        }
        offset = minOf(-1, maxOffset)
        while (offset >= minOffset) {
            if (consumer.all { producer.contains(it + offset) }) return offset
            offset--
        }
        throw MissingKeyException()
    }
}
