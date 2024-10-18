package pansong291.piano.wizard.utils

import org.mozilla.universalchardet.UniversalDetector
import java.io.File
import java.io.FileInputStream

object FileUtil {
    fun detectFileEncoding(file: File): String? {
        val buffer = ByteArray(4096)
        val detector = UniversalDetector(null)
        FileInputStream(file).use { fis ->
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } > 0 && !detector.isDone) {
                detector.handleData(buffer, 0, bytesRead)
            }
            detector.dataEnd()
        }
        return detector.detectedCharset
    }

    fun findAvailableFileName(path: String, name: String, ext: String): String {
        var i = 0
        var fname = name + ext
        while (File(path, fname).exists()) {
            i++
            fname = "$name ($i)$ext"
        }
        return fname
    }
}
