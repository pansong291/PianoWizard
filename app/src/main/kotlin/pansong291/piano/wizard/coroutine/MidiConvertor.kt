package pansong291.piano.wizard.coroutine

import android.os.Handler
import android.os.Looper
import android.util.SparseArray
import androidx.core.util.containsKey
import com.sun.media.sound.MidiUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pansong291.piano.wizard.consts.StringConst
import pansong291.piano.wizard.utils.FileUtil
import pansong291.piano.wizard.utils.MusicUtil
import java.io.File
import java.util.TreeMap
import javax.sound.midi.MetaMessage
import javax.sound.midi.MidiSystem
import javax.sound.midi.ShortMessage
import kotlin.math.pow

object MidiConvertor {
    /** 基准音高 (C4 在 MIDI 中是 60) */
    private const val MIDI_BASE_NOTE = 60

    /** 大调五度循环映射表 */
    private val MAJOR_KEYS: Array<String> =
        arrayOf("Cb", "Gb", "Db", "Ab", "Eb", "Bb", "F", "C", "G", "D", "A", "E", "B", "F#", "C#")

    /** 小调五度循环映射表 */
    private val MINOR_KEYS: Array<String> =
        arrayOf("Ab", "Eb", "Bb", "F", "C", "G", "D", "A", "E", "B", "F#", "C#", "G#", "D#", "A#")

    private var keyNote = "C"
    private var bpm = 1
    private var numerator = 4
    private var denominator = 4

    /** Pulses Per Quarter Note，每四分音符的滴答数 */
    private var ppq = 1

    private val handler = Handler(Looper.getMainLooper())
    var onParseResult: ((result: Map<Int, MutableList<MidiNote>>?, message: String?) -> Unit)? =
        null
    var onParseFinished: (() -> Unit)? = null
    var onConvertResult: ((message: String?) -> Unit)? = null
    var onConvertFinished: (() -> Unit)? = null

    fun parse(scope: CoroutineScope, file: File) {
        scope.launch {
            try {
                val result = parse(file)
                onParseResult?.also { handler.post { it(result, null) } }
            } catch (e: Throwable) {
                onParseResult?.also { handler.post { it(null, getErrorMessage(e)) } }
            }
        }.invokeOnCompletion {
            onParseFinished?.also { handler.post(it) }
        }
    }

    fun convert(
        scope: CoroutineScope,
        channelNotes: Map<Int, MutableList<MidiNote>>,
        filename: String,
        path: String,
        merge: Boolean
    ) {
        scope.launch {
            try {
                convert(channelNotes, filename, path, merge)
                onConvertResult?.also { handler.post { it(null) } }
            } catch (e: Throwable) {
                onConvertResult?.also { handler.post { it(getErrorMessage(e)) } }
            }
        }.invokeOnCompletion {
            onConvertFinished?.also { handler.post(it) }
        }
    }

    private fun parse(file: File): Map<Int, MutableList<MidiNote>> {
        val sequence = MidiSystem.getSequence(file)
        // 获取每四分音符的 tick 数（MIDI 文件的分辨率）
        ppq = sequence.resolution
        println("Ticks Per Beat: $ppq")
        val channelNotes = mutableMapOf<Int, MutableList<MidiNote>>()
        sequence.tracks.forEach { track ->
            // 存储音符开始的事件 (NOTE_ON)，以便计算持续时间
            val channelNoteMap = mutableMapOf<Int, SparseArray<Long>>()
            for (i in 0 until track.size()) {
                val event = track[i]
                val tick = event.tick // 当前 tick 时间
                when (val message = event.message) {
                    is ShortMessage -> {
                        val channel = message.channel
                        val noteTimeMap = channelNoteMap.computeIfAbsent(channel) { SparseArray() }
                        val velocity = message.data2
                        when {
                            message.command == ShortMessage.CONTROL_CHANGE -> {
                                val controller = message.data1 // 控制器编号
                                val value = message.data2 // 控制器值
                                println("Control Change - Channel: $channel, Controller: $controller, Value: $value")
                            }

                            message.command == ShortMessage.NOTE_ON && velocity > 0 -> {
                                val midiNote = message.data1 // 0 到 127
                                // 音符按下，保存该事件的 tick 值
                                noteTimeMap.put(midiNote, tick)
                            }

                            message.command == ShortMessage.NOTE_OFF || velocity == 0 -> {
                                val midiNote = message.data1 // 0 到 127
                                // 音符松开事件
                                if (noteTimeMap.containsKey(midiNote)) {
                                    val startTick = noteTimeMap[midiNote]
                                    noteTimeMap.remove(midiNote)
                                    val durationTicks = tick - startTick // 持续时间（以 tick 为单位）
                                    channelNotes.computeIfAbsent(channel) { ArrayList() }
                                        .add(MidiNote(midiNote, startTick, durationTicks))

                                    // 打印音符的开始时间和持续时间
                                    println(
                                        "Channel: $channel, Note: $midiNote, start Tick: $startTick, Duration (ticks): $durationTicks"
                                    )
                                }
                            }
                        }
                    }

                    is MetaMessage -> {
                        handleMetaMessage(message)
                    }
                }
            }
        }
        return channelNotes
    }

    private fun convert(
        channelNotes: Map<Int, MutableList<MidiNote>>,
        filename: String,
        path: String,
        merge: Boolean
    ) {
        var name = filename
        val index = filename.lastIndexOf('.')
        if (index > 0) name = filename.substring(0, index)
        val channels = mutableListOf<Int>()
        val combinedNotes = mutableListOf<MidiNote>()
        channelNotes.forEach { (channel, list) ->
            if (merge) {
                channels.add(channel)
                combinedNotes.addAll(list)
            } else {
                convert(list, "$name channel($channel)", path)
            }
        }
        if (merge) convert(combinedNotes, "$name channel(${channels.joinToString()})", path)
    }

    private fun convert(notes: List<MidiNote>, name: String, path: String) {
        // 按开始时间分组并排序
        val groupedNotes = notes.groupByTo(TreeMap()) { it.startTick }.values.toList()
        // 缩减持续时间，消去音节之间的重叠
        for (i in groupedNotes.indices) {
            val n1 = groupedNotes[i][0]
            for (j in i + 1 until groupedNotes.size) {
                val n2 = groupedNotes[j][0]
                if (n1.startTick + n1.durationTicks > n2.startTick)
                    n1.durationTicks = n2.startTick - n1.startTick
                else break
            }
        }
        val strBuilder = StringBuilder().apply {
            append("[1=").append(keyNote).append(',')
            append(numerator).append('/').append(denominator).append(',')
            append(bpm).append("]\n")
        }
        val baseTime = (60_000.0 / bpm).toLong()
        var lastTick = 0L
        groupedNotes.forEach {
            val tick = it[0].startTick
            val duration = it[0].durationTicks
            if (tick > lastTick) {
                MusicUtil.appendBeat(
                    strBuilder,
                    "0",
                    tickToMilliseconds(tick - lastTick),
                    baseTime
                )
            }
            MusicUtil.appendBeat(strBuilder, it.joinToString("&") {
                MusicUtil.compile12TETNote(it.note - MIDI_BASE_NOTE)
            }, tickToMilliseconds(duration), baseTime)
            lastTick = tick + duration
        }
        val filename =
            FileUtil.findAvailableFileName(path, name, StringConst.MUSIC_NOTATION_FILE_EXT)
        File(path, filename).writeText(strBuilder.toString())
    }

    /** 将 tick 转换为毫秒 */
    private fun tickToMilliseconds(ticks: Long): Long {
        return ticks * 60_000 / bpm / ppq
    }

    private fun handleMetaMessage(metaMessage: MetaMessage) {
        val data = metaMessage.data
        when (metaMessage.type) {
            0x01 -> println("Text: " + String(data))
            0x02 -> println("Copyright: " + String(data))
            0x03 -> println("Track Name: " + String(data))
            0x05 -> println("Lyric: " + String(data))
            0x06 -> println("Marker: " + String(data))
            0x07 -> println("Cue Point: " + String(data))

            // 处理曲速 (Tempo) 变化，每分钟节拍数。
            0x51 -> {
                val tempoMPQ = MidiUtils.getTempoMPQ(metaMessage)
                if (tempoMPQ > 0) {
                    bpm = MidiUtils.convertTempo(tempoMPQ.toDouble()).toInt()
                    println("Tempo change: $bpm BPM")
                }
            }

            // 处理节拍 (Time Signature): 拍子的分子和分母。
            0x58 -> {
                val data0 = data[0].toInt()
                val data1 = data[1].toInt()
                numerator = data0 and 0xFF
                denominator = 2.0.pow((data1 and 0xFF).toDouble()).toInt()
                println("Time Signature change: $numerator/$denominator")
            }

            // 调号（Key Signature）：表示乐曲的调性（例如，C 大调或 A 小调），包括升降号以及调性模式（大调/小调）
            0x59 -> {
                val data0 = data[0].toInt()
                val data1 = data[1].toInt()
                keyNote = if (data1 == 0) MAJOR_KEYS[data0 + 7] else MINOR_KEYS[data0 + 7]
                val scale = if (data1 == 0) "major" else "minor"
                println("Key Signature: $keyNote $scale")
            }
        }
    }

    private fun getErrorMessage(e: Throwable): String {
        val cause = e.cause ?: e
        return "${cause.javaClass.simpleName}: ${cause.message}"
    }
}

data class MidiNote(
    val note: Int,
    val startTick: Long,
    var durationTicks: Long
)
