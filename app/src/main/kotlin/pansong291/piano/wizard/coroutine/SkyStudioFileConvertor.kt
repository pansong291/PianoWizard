package pansong291.piano.wizard.coroutine

import android.app.Application
import android.os.Environment
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
            val result = tryResult {
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
            onResult?.also { handler.post { it(result) } }
        }.invokeOnCompletion {
            onFinished?.also { handler.post(it) }
        }
    }

    private fun convert(file: File): String {
        return file.name + " ->\n" + tryResult {
            val text = FileUtil.readNoBOMText(file)
            val sheets = GsonFactory.getSingletonGson().fromJson<List<SkyStudioSheet>>(
                text,
                TypeConst.listOfSkyStudioSheet.type
            )
            sheets.joinToString("\n") {
                "    " + convert(it, file.parent ?: Environment.getExternalStorageDirectory().path)
            }
        }
    }

    private fun convert(sheet: SkyStudioSheet, path: String): String {
        return tryResult {
            var name = sheet.name ?: application.getString(R.string.unknown_music)
            if (!sheet.author.isNullOrEmpty()) name += " - ${sheet.author}"
            if (!sheet.arrangedBy.isNullOrEmpty()) name += " + ${sheet.arrangedBy}"
            if (!sheet.transcribedBy.isNullOrEmpty()) name += " ~ ${sheet.transcribedBy}"
            val bpm = sheet.bpm?.takeIf { it > 0 }
                ?: throw ServiceException(R.string.target_must_gt_zero_message, "bpm")
            val pitchLevel = (sheet.pitchLevel?.toInt() ?: 0).takeIf { it >= 0 }
                ?: throw ServiceException(R.string.target_must_gte_zero_message, "pitchLevel")
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
                    ?: throw ServiceException(R.string.target_must_gte_zero_message, "time")
            }.mapTo(mutableListOf()) {
                it.key to it.value.map {
                    it.key?.split("Key")?.getOrNull(1)?.toIntOrNull()
                        ?: throw ServiceException(R.string.target_format_incorrect_message, "key")
                }
            }
            // 排序
            LangUtil.insertionSort(notesList) { p1, p2 -> p1.first < p2.first }
            val isSemi = MusicUtil.isSemitone(pitchLevel)
            val basePitch = MusicUtil.naturals.indexOf(if (isSemi) pitchLevel - 1 else pitchLevel)
            val baseNote = if (basePitch < 5) 'C' + basePitch else 'A' + basePitch - 5
            val strBuilder = StringBuilder().apply {
                append("/**\n * name: ").append(sheet.name)
                append("\n * author: ").append(sheet.author)
                append("\n * arrangedBy: ").append(sheet.arrangedBy)
                append("\n * transcribedBy: ").append(sheet.transcribedBy)
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
                    MusicUtil.compileNote(MusicUtil.basicNoteTo12TET(it))
                }
            }
            val filename =
                FileUtil.findAvailableFileName(path, name, StringConst.MUSIC_NOTATION_FILE_EXT)
            File(path, filename).writeText(strBuilder.toString())
            filename
        }
    }

    private fun tryResult(block: () -> String): String {
        return try {
            block()
        } catch (e: Throwable) {
            val cause = e.cause ?: e
            if (cause is ServiceException)
                "${application.getString(R.string.error)}: ${cause.getI18NMessage(application)}"
            else "${cause.javaClass.simpleName}: ${cause.message}"
        }
    }
}
