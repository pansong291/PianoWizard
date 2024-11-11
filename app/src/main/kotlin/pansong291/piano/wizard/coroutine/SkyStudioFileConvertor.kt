package pansong291.piano.wizard.coroutine

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.hjq.gson.factory.GsonFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pansong291.piano.wizard.R
import pansong291.piano.wizard.consts.StringConst
import pansong291.piano.wizard.consts.TypeConst
import pansong291.piano.wizard.entity.SkyStudioSheet
import pansong291.piano.wizard.exceptions.ServiceException
import pansong291.piano.wizard.utils.FileUtil
import pansong291.piano.wizard.utils.LangUtil
import pansong291.piano.wizard.utils.MusicUtil
import java.io.File
import java.io.FileFilter

object SkyStudioFileConvertor {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var application: Application
    var onResult: ((message: String) -> Unit)? = null
    var onFinished: (() -> Unit)? = null

    fun convert(application: Application, scope: CoroutineScope, file: File) {
        this.application = application
        scope.launch {
            val pair = tryResult {
                val messages = mutableListOf<String>()
                if (file.isDirectory) {
                    file.listFiles(FileFilter {
                        it.isFile && it.name.endsWith(
                            StringConst.SKY_STUDIO_SHEET_FILE_EXT,
                            true
                        ) && !it.name.endsWith(StringConst.MUSIC_NOTATION_FILE_EXT, true)
                    })?.forEach {
                        messages.add(convert(it))
                    }
                } else {
                    messages.add(convert(file))
                }
                messages.joinToString("\n\n")
            }
            onResult?.also { handler.post { it(pair.second) } }
        }.invokeOnCompletion {
            onFinished?.also { handler.post(it) }
        }
    }

    private fun convert(file: File): String {
        return file.name + " ->\n" + tryResult {
            val text = FileUtil.readNoBOMText(file).trim()
            "    " + if (text.startsWith('[') && text.endsWith(']')) {
                val sheets = GsonFactory.getSingletonGson().fromJson<List<SkyStudioSheet>>(
                    text,
                    TypeConst.listOfSkyStudioSheet.type
                ).filter { it.isEncrypted != true }
                var success = true
                val result = sheets.joinToString("\n") {
                    val pair =
                        convert(it, file)
                    if (!pair.first) success = false
                    pair.second
                }
                if (success) FileUtil.renameToBakFile(file)
                result
            } else if (text.startsWith('<')) {
                val pair = convert(text, file)
                if (pair.first) FileUtil.renameToBakFile(file)
                pair.second
            } else throw Exception("Unsupported content")
        }.second
    }

    private fun convert(sheet: SkyStudioSheet, file: File): Pair<Boolean, String> {
        return tryResult {
            var name = omitUnknownText(sheet.name).ifBlank { file.name.substringBeforeLast('.') }
            val author = omitUnknownText(sheet.author)
            val arrangedBy = omitUnknownText(sheet.arrangedBy)
            val transcribedBy = omitUnknownText(sheet.transcribedBy)
            val bpm = sheet.bpm?.takeIf { it > 0 } ?: 120.0
            val pitchLevel = (sheet.pitchLevel?.toInt() ?: 0).takeIf { it >= 0 } ?: 0
            val numerator = when (sheet.bitsPerPage?.toInt()) {
                4 -> 1
                12 -> 3
                else -> 4
            }
            val baseTime = (60_000.0 / bpm).toLong()
            val songNotes = sheet.songNotes
            if (songNotes.isNullOrEmpty())
                throw ServiceException(R.string.target_cannot_empty_message, "songNotes")
            val notesList = songNotes.groupByTo(LinkedHashMap()) {
                // 按时间分组，相同时间的 key 构成和弦
                (it.time?.toLong() ?: 0).takeIf { it >= 0 }
                    ?: throw ServiceException(
                        R.string.target_must_gte_zero_message,
                        "time",
                        it.time.toString()
                    )
            }.mapTo(mutableListOf()) {
                it.key to it.value.map {
                    it.key?.split("Key")?.getOrNull(1)?.toIntOrNull()
                        ?: throw keyFormatException(it.key.toString())
                }
            }
            // 排序
            LangUtil.insertionSort(notesList) { p1, p2 -> p1.first < p2.first }
            val isSemi = MusicUtil.isSemitone(pitchLevel)
            val basePitch = MusicUtil.naturals.indexOf(if (isSemi) pitchLevel - 1 else pitchLevel)
            val baseNote = if (basePitch < 5) 'C' + basePitch else 'A' + basePitch - 5
            val strBuilder = StringBuilder().apply {
                append("/**\n * name: ").append(name)
                append("\n * author: ").append(author)
                append("\n * arrangedBy: ").append(arrangedBy)
                append("\n * transcribedBy: ").append(transcribedBy)
                append("\n */\n[1=").append(baseNote)
                if (isSemi) append('#')
                append(',').append(numerator).append("/4,")
                append(bpm.toLong()).append("]\n")
            }
            var last = 0L to "0"
            for (i in 0..notesList.size) {
                if (i == notesList.size) {
                    strBuilder.append(last.second).append(',')
                    break
                }
                val note = notesList[i]
                MusicUtil.appendBeat(
                    strBuilder,
                    last.second,
                    note.first - last.first,
                    baseTime
                )
                last = note.first to note.second.joinToString("&") {
                    MusicUtil.compileBasicNote(it)
                }
            }
            if (author.isNotEmpty()) name += " - $author"
            if (arrangedBy.isNotEmpty()) name += " + $arrangedBy"
            if (transcribedBy.isNotEmpty()) name += " ~ $transcribedBy"
            val filename = FileUtil.findAvailableFileName(
                file.parent!!,
                name,
                StringConst.MUSIC_NOTATION_FILE_EXT
            )
            File(file.parent!!, filename).writeText(strBuilder.toString())
            filename
        }
    }

    private fun convert(text: String, file: File): Pair<Boolean, String> {
        return tryResult {
            val gtInd = text.indexOf('>')
            if (gtInd < 0) throw Exception("symbol '>' not found")
            val lfInd = text.indexOf('\n', gtInd)
            if (lfInd < 0) throw Exception("line separator '\\n' not found")
            val firstLine = text.substring(gtInd + 1, lfInd).trim()
            val rest = text.substring(lfInd + 1).trim()
            if (rest.isEmpty())
                throw ServiceException(R.string.target_cannot_empty_message, "songNotes")
            val infoList = firstLine.split(' ').filterNot { it.isBlank() }
            val keyList = rest.split(' ').filterNot { it.isBlank() }

            val bpm = infoList.getOrNull(0)?.toDoubleOrNull()?.takeIf { it > 0 } ?: 120.0
            val pitchLevel = (infoList.getOrNull(1)?.toInt() ?: 0).takeIf { it >= 0 } ?: 0
            val numerator = when (infoList.getOrNull(2)?.toInt()) {
                4 -> 1
                12 -> 3
                else -> 4
            }
            val author = omitUnknownText(infoList.getOrNull(3))
            val transcribedBy = omitUnknownText(infoList.getOrNull(4))

            var name = file.name.substringBeforeLast('.')

            val isSemi = MusicUtil.isSemitone(pitchLevel)
            val basePitch = MusicUtil.naturals.indexOf(if (isSemi) pitchLevel - 1 else pitchLevel)
            val baseNote = if (basePitch < 5) 'C' + basePitch else 'A' + basePitch - 5
            val strBuilder = StringBuilder().apply {
                append("/**\n * name: ").append(name)
                append("\n * author: ").append(author)
                append("\n * arrangedBy: ")
                append("\n * transcribedBy: ").append(transcribedBy)
                append("\n */\n[1=").append(baseNote)
                if (isSemi) append('#')
                append(',').append(numerator).append("/4,")
                append(bpm.toLong()).append("]\n")
            }

            var dotCount = 0
            val notes = mutableListOf<Int>()
            keyList.forEach { key ->
                if (key == ".") {
                    dotCount++
                    return@forEach
                }
                appendNotes(notes, strBuilder, dotCount)
                dotCount = 0
                var note: Int? = null
                key.forEach { ch ->
                    note?.also {
                        if (ch in '1'..'5') {
                            notes.add(it * 5 + (ch - '1'))
                            note = null
                        } else throw keyFormatException(key)
                    } ?: run {
                        note = if (ch in 'A'..'C') ch - 'A'
                        else throw keyFormatException(key)
                    }
                }
                if (note != null) throw keyFormatException(key)
            }
            appendNotes(notes, strBuilder, dotCount)
            if (author.isNotEmpty()) name += " - $author"
            if (transcribedBy.isNotEmpty()) name += " ~ $transcribedBy"
            val filename = FileUtil.findAvailableFileName(
                file.parent!!,
                name,
                StringConst.MUSIC_NOTATION_FILE_EXT
            )
            File(file.parent!!, filename).writeText(strBuilder.toString())
            filename
        }
    }

    private fun appendNotes(notes: MutableList<Int>, strBuilder: StringBuilder, dotCount: Int) {
        if (notes.isNotEmpty()) {
            strBuilder.append(notes.joinToString("&") { MusicUtil.compileBasicNote(it) })
            if (dotCount > 0) strBuilder.append('*').append(dotCount + 1)
            strBuilder.append(',')
            notes.clear()
        }
    }

    private fun keyFormatException(key: String): Exception {
        return ServiceException(R.string.target_format_incorrect_message, "key", key)
    }

    private fun omitUnknownText(text: String?): String {
        return text?.takeUnless { it == "Unknown" || it == "Untitle" }.orEmpty()
    }

    private fun tryResult(block: () -> String): Pair<Boolean, String> {
        return try {
            true to block()
        } catch (e: Throwable) {
            val cause = e.cause ?: e
            false to if (cause is ServiceException)
                "${application.getString(R.string.error)}: ${cause.getI18NMessage(application)}"
            else "${cause.javaClass.simpleName}: ${cause.message}"
        }
    }
}
