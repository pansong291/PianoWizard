package pansong291.piano.wizard.coroutine

import android.content.Context
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object MusicSheetsExtractor {
    private val handler = Handler(Looper.getMainLooper())
    private var firstException: Throwable? = null
    var onSuccess: (() -> Unit)? = null
    var onError: ((message: String) -> Unit)? = null
    var onFinished: (() -> Unit)? = null
    var onProgress: ((current: Int, total: Int) -> Unit)? = null

    fun startExtraction(context: Context, scope: CoroutineScope) {
        handler.postDelayed({
            scope.launch {
                withContext(Dispatchers.IO) {
                    val filesDir = context.getExternalFilesDir(null) ?: return@withContext
                    val folder = "yp"
                    val targetDir = File(filesDir, folder)
                    val assets = context.assets
                    overrideAsFolder(targetDir)
                    try {
                        val files = assets.list(folder) ?: return@withContext
                        val totalFiles = files.size
                        files.forEachIndexed { index, it ->
                            val subFile = File(targetDir, it)
                            subFile.deleteRecursively()
                            copyFile(assets.open("$folder/$it"), FileOutputStream(subFile))
                            onProgress?.also {
                                handler.post { it(index + 1, totalFiles) }
                            }
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        if (firstException == null) firstException = e
                    }
                }
                firstException?.apply {
                    onError?.also {
                        handler.post { it((this.cause ?: this).message ?: "Unknown Error") }
                    }
                } ?: run {
                    onSuccess?.also { handler.post(it) }
                }
            }.invokeOnCompletion {
                onFinished?.also { handler.post(it) }
            }
        }, 100)
    }

    private fun overrideAsFolder(folder: File) {
        if (folder.isDirectory) return
        if (folder.exists()) folder.delete()
        folder.mkdirs()
    }

    private fun copyFile(inputStream: InputStream, outputStream: OutputStream) {
        try {
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            outputStream.flush()
        } catch (e: Throwable) {
            e.printStackTrace()
            if (firstException == null) firstException = e
        } finally {
            inputStream.runCatching { close() }
            outputStream.runCatching { close() }
        }
    }
}
