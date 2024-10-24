package pansong291.piano.wizard.utils

import android.R.attr.key
import android.util.SparseArray
import androidx.core.util.containsKey
import java.io.File
import javax.sound.midi.MetaMessage
import javax.sound.midi.MidiSystem
import javax.sound.midi.ShortMessage
import kotlin.math.pow


object MidiUtil {
    // 基准音高（C4 在 MIDI 中是 60）
    const val MIDI_BASE_NOTE = 60

    fun convert(file: File) {
        val sequence = MidiSystem.getSequence(file)
        // 获取每四分音符的 tick 数（MIDI 文件的分辨率）
        val ticksPerBeat = sequence.resolution
        sequence.tracks.forEach { track ->
            // 存储音符开始的事件 (NOTE_ON)，以便计算持续时间
            val noteOnTimeMap = SparseArray<Long>()
            for (i in 0 until track.size()) {
                val event = track[i]
                val message = event.message
                when (message) {
                    is ShortMessage -> {
                        val velocity = message.data2
                        val noteOn = message.command == ShortMessage.NOTE_ON && velocity > 0
                        val noteOff = message.command == ShortMessage.NOTE_OFF || velocity == 0
                        val tick = event.tick // 当前 tick 时间
                        if (noteOn) {
                            val midiNote = message.data1 // 0 到 127
                            // 音符按下，保存该事件的 tick 值
                            noteOnTimeMap.put(midiNote, tick)
                        } else if (noteOff) {
                            // 音符松开事件
                            if (noteOnTimeMap.containsKey(key)) {
                                val startTick = noteOnTimeMap[key]
                                val durationTicks = tick - startTick // 持续时间（以 tick 为单位）

                                // 打印音符的开始时间和持续时间
                                println(
                                    "Note: " + key +
                                            " Start Tick: " + startTick +
                                            " Duration (ticks): " + durationTicks
                                )

                                // 将 tick 转换为时间
                                val startTimeSeconds: Double = tickToSeconds(
                                    startTick,
                                    ticksPerBeat,
                                    sequence.microsecondLength
                                )
                                val durationSeconds: Double = tickToSeconds(
                                    durationTicks,
                                    ticksPerBeat,
                                    sequence.microsecondLength
                                )

                                println("Start Time (seconds): $startTimeSeconds")
                                println("Duration (seconds): $durationSeconds")
                            }
                        }
                    }

                    is MetaMessage -> {
                        handleMetaMessage(message)
                    }
                }
            }
        }
    }

    /** 将 tick 转换为秒 */
    private fun tickToSeconds(ticks: Long, ticksPerBeat: Int, microsecondLength: Long): Double {
        // MIDI 文件中的总拍数
        val totalBeats = ticks.toDouble() / ticksPerBeat

        // MIDI 文件的总时长（以秒为单位）
        val totalSeconds = microsecondLength.toDouble() / 1000_000

        // 文件的总 tick 数
        val totalTicks = totalBeats * ticksPerBeat

        // 每 tick 的时间（以秒为单位）
        val secondsPerTick = totalSeconds / totalTicks

        return ticks * secondsPerTick
    }

    private fun handleMetaMessage(meta: MetaMessage) {
        val data = meta.data
        val data0 = data[0].toInt()
        val data1 = data[1].toInt()
        when (meta.type) {
            0x01 -> println("Text: " + String(data))
            0x02 -> println("Copyright: " + String(data))
            0x03 -> println("Track Name: " + String(data))
            0x05 -> println("Lyric: " + String(data))
            0x06 -> println("Marker: " + String(data))
            0x07 -> println("Cue Point: " + String(data))

            // 处理曲速 (Tempo) 变化，Meta Type 0x51 表示 Tempo
            0x51 -> {
                val data2 = data[2].toInt()
                val tempo =
                    ((data0 and 0xFF) shl 16) or ((data1 and 0xFF) shl 8) or (data2 and 0xFF)
                val bpm = 60000000 / tempo
                println("Tempo change: $bpm BPM")
            }
            // 处理节拍 (Time Signature)，Meta Type 0x58 表示 Time Signature
            0x58 -> {
                val numerator = data0 and 0xFF
                val denominator = 2.0.pow((data1 and 0xFF).toDouble()).toInt()
                println("Time Signature change: $numerator/$denominator")
            }

            0x59 -> {
                val key: String = getKeySignature(data0)
                val scale = if (data1 == 0) "major" else "minor"
                println("Key Signature: $key $scale")
            }
        }
    }

    // 获取调号信息
    private fun getKeySignature(key: Int): String {
        return when (key) {
            -7 -> "C♭"
            -6 -> "G♭"
            -5 -> "D♭"
            -4 -> "A♭"
            -3 -> "E♭"
            -2 -> "B♭"
            -1 -> "F"
            0 -> "C"
            1 -> "G"
            2 -> "D"
            3 -> "A"
            4 -> "E"
            5 -> "B"
            6 -> "F#"
            7 -> "C#"
            else -> "Unknown"
        }
    }
}
/*
节拍（Time Signature）：
节拍信息存储在 MetaMessage 类型 0x58 中。它包含两个重要的值：拍子的分子和分母。
调号（Key Signature）：
调号信息存储在 MetaMessage 类型 0x59 中，表示乐曲的调性（例如，C 大调或 A 小调），包括升降号以及调性模式（大调/小调）
*/
