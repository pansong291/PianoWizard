package pansong291.piano.wizard.coroutine

import android.graphics.Point
import android.os.Handler
import android.os.Looper
import android.util.SparseArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pansong291.piano.wizard.entity.ClickAction
import pansong291.piano.wizard.entity.KeyLayout
import pansong291.piano.wizard.entity.MusicNotation
import pansong291.piano.wizard.entity.MusicPlayingSettings
import pansong291.piano.wizard.entity.TapMode
import pansong291.piano.wizard.exceptions.MissingKeyException
import pansong291.piano.wizard.services.ClickAccessibilityService
import pansong291.piano.wizard.utils.MusicUtil
import java.util.TreeSet

object MusicPlayer {
    private val handler = Handler(Looper.getMainLooper())

    // 使用 CONFLATED 模式，只保存最新状态，未处理的旧状态会被丢弃
    private val controlChannel = Channel<Boolean>(capacity = Channel.CONFLATED)
    private var job: Job? = null
    private var isPaused: Boolean = false
    var onStopped: (() -> Unit)? = null
    var onPaused: (() -> Unit)? = null
    var onResume: (() -> Unit)? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun startPlay(
        scope: CoroutineScope,
        mn: MusicNotation,
        kl: KeyLayout,
        mps: MusicPlayingSettings,
        offset: Int,
        ignoreMissingKey: Boolean = false
    ) {
        isPaused = false
        if (mps.tempoRate <= 0) throw IllegalArgumentException("tempoRate must be greater than 0")
        if (mps.tapMode == TapMode.TapAndHold && mps.earlyRelease <= 0)
            throw IllegalArgumentException("earlyRelease must be greater than 0")
        if (mps.tapMode == TapMode.RepeatedlyTap && mps.tapInterval <= 0)
            throw IllegalArgumentException("tapInterval must be greater than 0")
        val pointMap = SparseArray<Point>()
        val baseTime = 60_000f / mn.bpm / mps.tempoRate

        // 构建十二平均律到按键点位的映射关系
        kl.points.forEachIndexed { index, point ->
            var note = index + kl.keyOffset
            if (!kl.semitone) note = MusicUtil.basicNoteTo12TET(note)
            pointMap.append(
                note,
                Point((point.x + kl.rawOffset.x).toInt(), (point.y + kl.rawOffset.y).toInt())
            )
        }
        val clickActions = mn.beats.map {
            ClickAction().apply {
                points = it.tones.mapNotNull { pointMap[it + mn.keyNote + offset] }
                delay = (it.durationRate * baseTime).toInt()
                if (!ignoreMissingKey && points.size < it.tones.size) {
                    throw MissingKeyException()
                }
            }
        }

        job = scope.launch {
            while (!controlChannel.isEmpty) controlChannel.tryReceive()
            delay(mps.prePlayDelay * 1000 + 200L)
            clickActions.forEach {
                if (!isActive) return@forEach
                if (!controlChannel.isEmpty) {
                    isPaused = controlChannel.receive()
                    if (isPaused) onPaused?.let { handler.post(it) }
                }
                while (isPaused) {
                    isPaused = controlChannel.receive()
                    if (!isPaused) onResume?.let { handler.post(it) }
                    delay(200)
                }
                val time = System.currentTimeMillis()
                if (it.points.isNotEmpty()) {
                    val holdTime = when (mps.tapMode) {
                        TapMode.TapAndHold -> maxOf(it.delay.toLong() - mps.earlyRelease, 1L)
                        TapMode.RepeatedlyTap -> maxOf(it.delay.toLong(), 1L)
                        else -> 1L
                    }
                    if (mps.tapMode == TapMode.RepeatedlyTap) {
                        val interval = mps.tapInterval.toLong()
                        var start = 0L
                        while (start < holdTime) {
                            if (start > 0) delay(interval)
                            ClickAccessibilityService.click(it.points, 1L)
                            start += interval
                        }
                    } else {
                        ClickAccessibilityService.click(it.points, holdTime)
                    }
                }
                delay(it.delay + time - System.currentTimeMillis())
            }
            delay(mps.postPlayDelay * 1000L)
        }
        job?.invokeOnCompletion {
            job = null
            onStopped?.let { handler.post(it) }
        }
    }

    fun pause() {
        if (isPlaying()) controlChannel.trySend(true)
    }

    fun resume() {
        if (isPlaying()) controlChannel.trySend(false)
    }

    fun stop() {
        job?.cancel()
        job = null
        isPaused = false
    }

    fun isPlaying(): Boolean {
        return job != null
    }

    fun isPaused(): Boolean {
        return isPaused
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
