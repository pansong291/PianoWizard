package pansong291.piano.wizard.coroutine

import android.graphics.Point
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pansong291.piano.wizard.entity.KeyLayout
import pansong291.piano.wizard.entity.MusicNotation
import pansong291.piano.wizard.entity.MusicPlayingSettings
import pansong291.piano.wizard.entity.TapMode
import pansong291.piano.wizard.exceptions.MissingKeyException
import pansong291.piano.wizard.services.ClickAccessibilityService
import pansong291.piano.wizard.utils.MusicUtil

object MusicPlayer {
    private val handler = Handler(Looper.getMainLooper())

    // 使用 CONFLATED 模式，只保存最新状态，未处理的旧状态会被丢弃
    private val controlChannel = Channel<Boolean>(capacity = Channel.CONFLATED)
    private var job: Job? = null

    /**
     * 是否弹奏中 (无视暂停状态)
     */
    val isPlaying get() = job != null

    /**
     * 是否已暂停
     */
    var isPaused: Boolean = false
        private set

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
        if (mps.playbackRate <= 0) throw IllegalArgumentException("playbackRate must be greater than 0")
        if (mps.tapMode == TapMode.TapAndHold && mps.earlyRelease <= 0)
            throw IllegalArgumentException("earlyRelease must be greater than 0")
        if (mps.tapMode == TapMode.RepeatedlyTap && mps.tapInterval <= 0)
            throw IllegalArgumentException("tapInterval must be greater than 0")

        val pointList = kl.points.map {
            Point((it.x + kl.rawOffset.x).toInt(), (it.y + kl.rawOffset.y).toInt())
        }
        val hitActions = MusicUtil.getHitActions(mn, kl, offset, ignoreMissingKey, mps.playbackRate)

        job = scope.launch {
            val blockedUnit = 500L
            while (!controlChannel.isEmpty) controlChannel.tryReceive()
            pausableDelay(mps.prePlayDelay * 2, blockedUnit)
            delay(200)
            hitActions.forEach {
                if (!isActive) return@forEach
                checkPauseSignal(200)
                val time = System.currentTimeMillis()
                if (it.locations.isNotEmpty()) {
                    val holdTime = when (mps.tapMode) {
                        TapMode.TapAndHold -> maxOf(it.postDelay - mps.earlyRelease, 1)
                        TapMode.RepeatedlyTap -> maxOf(it.postDelay, 1)
                        else -> 1
                    }.toLong()
                    val hitPoints = it.locations.map { pointList[it] }
                    if (mps.tapMode == TapMode.RepeatedlyTap) {
                        val interval = mps.tapInterval.toLong()
                        var start = 0L
                        while (start < holdTime) {
                            if (start > 0) delay(interval)
                            ClickAccessibilityService.click(hitPoints, 1)
                            start += interval
                        }
                    } else {
                        ClickAccessibilityService.click(hitPoints, holdTime)
                    }
                }
                val rest = it.postDelay + time - System.currentTimeMillis()
                val chunk = rest / blockedUnit
                pausableDelay(chunk.toInt(), blockedUnit)
                delay(rest % blockedUnit)
            }
            pausableDelay(mps.postPlayDelay * 2, blockedUnit)
        }
        job?.invokeOnCompletion {
            job = null
            onStopped?.let { handler.post(it) }
        }
    }

    private suspend fun pausableDelay(count: Int, delayUnit: Long) {
        repeat(count) {
            delay(delayUnit)
            checkPauseSignal()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun checkPauseSignal(delayWhenResume: Long = 0) {
        if (!controlChannel.isEmpty) {
            isPaused = controlChannel.receive()
            if (isPaused) onPaused?.let { handler.post(it) }
        }
        while (isPaused) {
            isPaused = controlChannel.receive()
            if (!isPaused) onResume?.let { handler.post(it) }
            delay(delayWhenResume)
        }
    }

    fun pause() {
        if (isPlaying) controlChannel.trySend(true)
    }

    fun resume() {
        if (isPlaying) controlChannel.trySend(false)
    }

    fun stop() {
        job?.cancel()
        job = null
        isPaused = false
    }

    fun playKeyNote(key: Int, kl: KeyLayout, offset: Int) {
        val target = key + offset
        kl.points.forEachIndexed { index, point ->
            var note = index + kl.keyOffset
            if (!kl.semitone) note = MusicUtil.basicNoteTo12TET(note)
            if (target != note) return@forEachIndexed
            val p = Point((point.x + kl.rawOffset.x).toInt(), (point.y + kl.rawOffset.y).toInt())
            ClickAccessibilityService.click(listOf(p), 50)
            return
        }
        throw MissingKeyException()
    }
}
