package pansong291.piano.wizard.utils

import android.os.Build
import org.mozilla.universalchardet.UniversalDetector
import pansong291.piano.wizard.consts.StringConst
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset
import java.nio.file.Paths

object FileUtil {
    val regex_filename_sanitize = Regex("[/\\\\:*?\"<>|\\x00-\\x1F]")

    fun readNoBOMText(file: File): String {
        return removeBOM(file.readText(detectFileEncoding(file)))
    }

    fun detectFileEncoding(file: File): Charset {
        val buffer = ByteArray(4096)
        val detector = UniversalDetector(null)
        FileInputStream(file).use { fis ->
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } > 0 && !detector.isDone) {
                detector.handleData(buffer, 0, bytesRead)
            }
            detector.dataEnd()
        }
        return detector.detectedCharset?.let { Charset.forName(it) } ?: Charset.defaultCharset()
    }

    fun removeBOM(text: String): String {
        return text.removePrefix("\ufeff") // UTF_8, UTF_16 (LE/BE)
            .removePrefix("\ufffe\u0000\u0000") // UTF_32LE
            .removePrefix("\u0000\u0000\ufeff") // UTF_32BE
    }

    fun sanitizeFileName(name: String): String {
        return name.replace(regex_filename_sanitize, "_")
    }

    fun findAvailableFileName(path: String, name: String, ext: String): String {
        val fixName = sanitizeFileName(name)
        var i = 0
        var filename = fixName + ext
        while (File(path, filename).exists()) {
            i++
            filename = "$fixName ($i)$ext"
        }
        return filename
    }

    fun renameToBakFile(file: File): Boolean {
        val bakName = findAvailableFileName(file.parent!!, file.name, StringConst.BAK_FILE_EXT)
        return file.renameTo(File(file.parent!!, bakName))
    }

    fun pathJoin(parent: String, child: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 26 (Oreo) 及以上版本，使用 Paths
            Paths.get(parent, child).toString()
        } else {
            // Android 26 以下版本，使用 File
            File(parent, child).path
        }
    }
}
