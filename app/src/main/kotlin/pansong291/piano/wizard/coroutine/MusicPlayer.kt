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
import pansong291.piano.wizard.consts.StringConst
import pansong291.piano.wizard.entity.KeyLayout
import pansong291.piano.wizard.entity.MusicNotation
import pansong291.piano.wizard.entity.MusicPlayingSettings
import pansong291.piano.wizard.entity.TapMode
import pansong291.piano.wizard.exceptions.MissingKeyException
import pansong291.piano.wizard.exceptions.SkipMusicException
import pansong291.piano.wizard.services.ClickAccessibilityService
import pansong291.piano.wizard.utils.FileUtil
import pansong291.piano.wizard.utils.MusicUtil
import java.io.File

object MusicPlayer {
    private const val BLOCKED_UNIT = 500L
    private val handler = Handler(Looper.getMainLooper())

    // 使用 CONFLATED 模式，只保存最新状态，未处理的旧状态会被丢弃
    private val controlChannel = Channel<Signal>(capacity = Channel.CONFLATED)
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
    var onMusicSkip: ((String) -> Unit)? = null

    fun startSingle(
        scope: CoroutineScope,
        mn: MusicNotation,
        kl: KeyLayout,
        mps: MusicPlayingSettings,
        offset: Int,
        ignoreMissingKey: Boolean = false,
    ) = startPlay(scope, SingleMode(mn, offset, ignoreMissingKey), kl, mps)

    fun startList(
        scope: CoroutineScope,
        folder: File,
        kl: KeyLayout,
        mps: MusicPlayingSettings,
        repeatAll: Boolean,
        random: Boolean,
    ) = startPlay(scope, ListMode(folder, repeatAll, random), kl, mps)

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startPlay(
        scope: CoroutineScope,
        playMode: PlayMode,
        kl: KeyLayout,
        mps: MusicPlayingSettings,
    ) {
        isPaused = false
        if (mps.playbackRate <= 0) throw IllegalArgumentException("playbackRate must be greater than 0")
        if (mps.tapMode == TapMode.TapAndHold && mps.earlyRelease <= 0)
            throw IllegalArgumentException("earlyRelease must be greater than 0")
        if (mps.tapMode == TapMode.RepeatedlyTap && mps.tapInterval <= 0)
            throw IllegalArgumentException("tapInterval must be greater than 0")

        val repeatAll = if (playMode is ListMode) playMode.repeatAll else false
        val random = if (playMode is ListMode) playMode.random else false
        // 为了及时判断是否缺少按键，需要在协程外执行
        val singleHitActions = if (playMode is SingleMode) MusicUtil.getHitActions(
            playMode.mn,
            kl,
            playMode.offset,
            playMode.ignoreMissingKey,
            mps.playbackRate
        ) else null

        val pointList = kl.points.map {
            Point((it.x + kl.rawOffset.x).toInt(), (it.y + kl.rawOffset.y).toInt())
        }
        val musicList by lazy {
            when (playMode) {
                is SingleMode -> mutableListOf(playMode.mn)
                is ListMode -> mutableListOf(
                    MusicUtil.parseMusicNotation(
                        "",
                        StringConst.TRIAL_MUSIC_NAME.substringBeforeLast(StringConst.MUSIC_NOTATION_FILE_EXT),
                        StringConst.TRIAL_MUSIC_CONTENT
                    )
                )

                else -> mutableListOf()
            }
        }

        job = scope.launch {
            while (!controlChannel.isEmpty) controlChannel.tryReceive()
            pausableDelay(mps.prePlayDelay * 2)
            listLoop@ do {
                pausableDelay(delayUnit = 100)
                // 打乱列表
                if (random) musicList.shuffle()
                musicEach@ for (mn in musicList) {
                    pausableDelay(delayUnit = 100)
                    // 若乐谱未解析则先进行解析
                    if (mn.keyNote < 0) runCatching {
                        val parsed = MusicUtil.parseMusicNotation(
                            mn.filepath,
                            mn.name,
                            FileUtil.readNoBOMText(File(mn.filepath))
                        )
                        mn.keyNote = parsed.keyNote
                        mn.bpm = parsed.bpm
                        mn.beats = parsed.beats
                    }.onFailure {
                        mn.keyNote = 0  // 移除未解析标记
                    }
                    // 解析失败则跳过
                    if (mn.beats.isEmpty()) continue@musicEach
                    val hitActions = singleHitActions ?: MusicUtil.getHitActions(
                        mn,
                        kl,
                        MusicUtil.findSuitableOffset(mn, kl, 0),
                        true,
                        mps.playbackRate
                    )
                    // 切歌通知
                    if (playMode is ListMode) onMusicSkip?.let { handler.post { it(mn.name) } }
                    delay(200)
                    actionEach@ for (hit in hitActions) {
                        if (!isActive) break@listLoop
                        try {
                            checkPauseSignal(200, checkSkip = true)
                            val time = System.currentTimeMillis()
                            if (hit.locations.isNotEmpty()) {
                                val holdTime = when (mps.tapMode) {
                                    TapMode.TapAndHold -> maxOf(hit.postDelay - mps.earlyRelease, 1)
                                    TapMode.RepeatedlyTap -> maxOf(hit.postDelay, 1)
                                    else -> 1
                                }.toLong()
                                val hitPoints = hit.locations.map { pointList[it] }
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
                            val rest = hit.postDelay + time - System.currentTimeMillis()
                            val chunk = rest / BLOCKED_UNIT
                            pausableDelay(chunk.toInt(), checkSkip = true)
                            delay(rest % BLOCKED_UNIT)
                        } catch (e: SkipMusicException) {
                            break@actionEach
                        }
                    }
                }
            } while (repeatAll)
            pausableDelay(mps.postPlayDelay * 2)
        }
        job?.invokeOnCompletion {
            job = null
            onStopped?.let { handler.post(it) }
        }
    }

    private suspend fun pausableDelay(
        count: Int = 1,
        delayUnit: Long = BLOCKED_UNIT,
        checkSkip: Boolean = false
    ) {
        repeat(count) {
            delay(delayUnit)
            checkPauseSignal(checkSkip = checkSkip)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun checkPauseSignal(delayWhenResume: Long = 0, checkSkip: Boolean = false) {
        if (!controlChannel.isEmpty) {
            checkAndUpdatePausedState(checkSkip)
            if (isPaused) onPaused?.let { handler.post(it) }
        }
        while (isPaused) {
            checkAndUpdatePausedState(checkSkip)
            if (!isPaused) onResume?.let { handler.post(it) }
            delay(delayWhenResume)
        }
    }

    private suspend fun checkAndUpdatePausedState(checkSkip: Boolean = false) {
        val signal = controlChannel.receive()
        if (signal == Signal.SKIP) {
            if (checkSkip) throw SkipMusicException()
        } else isPaused = signal == Signal.PAUSE
    }

    fun pause() {
        if (isPlaying) controlChannel.trySend(Signal.PAUSE)
    }

    fun resume() {
        if (isPlaying) controlChannel.trySend(Signal.RESUME)
    }

    fun skip() {
        if (isPlaying) controlChannel.trySend(Signal.SKIP)
    }

    fun stop() {
        job?.cancel()
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

    private enum class Signal {
        PAUSE, RESUME, SKIP
    }

    private interface PlayMode

    private class SingleMode(
        val mn: MusicNotation,
        val offset: Int,
        val ignoreMissingKey: Boolean,
    ) : PlayMode

    private class ListMode(
        val folder: File,
        val repeatAll: Boolean,
        val random: Boolean,
    ) : PlayMode
}
