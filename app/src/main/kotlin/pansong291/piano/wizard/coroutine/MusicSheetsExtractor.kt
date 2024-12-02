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
                        // 因为部分机器在读取大量文件时可能出现卡死，故这里使用分块目录来存放文件。
                        // yp 目录中有 0,1,2,... 等分块的子文件夹，
                        // 每个分块中最多有 1000 个文件，其中序号最大的分块文件数量可能不足，
                        // 所以先找出序号最大的文件数量，就可以知道总共有多少文件。
                        val chunks = assets.list(folder) ?: return@withContext
                        val lastIndex = (chunks.size - 1).toString()
                        val lastFiles = assets.list("$folder/$lastIndex")
                        val totalFiles = (chunks.size - 1) * 1000 + (lastFiles?.size ?: 0)
                        var current = 0
                        chunks.forEach { chunk ->
                            if (chunk == lastIndex) {
                                lastFiles
                            } else {
                                assets.list("$folder/$chunk")
                            }?.forEach {
                                val subFile = File(targetDir, it)
                                subFile.deleteRecursively()
                                copyFile(
                                    assets.open("$folder/$chunk/$it"),
                                    FileOutputStream(subFile)
                                )
                                onProgress?.also {
                                    handler.post { it(++current, totalFiles) }
                                }
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
