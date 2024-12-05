package pansong291.piano.wizard.coroutine

import android.content.Context
import android.content.res.AssetManager
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

object AssetsExtractor {
    private val handler = Handler(Looper.getMainLooper())
    private var firstException: Throwable? = null
    var onError: ((message: String) -> Unit)? = null
    var onFinished: (() -> Unit)? = null

    fun startExtraction(context: Context, scope: CoroutineScope, folder: String) {
        handler.postDelayed({
            scope.launch {
                withContext(Dispatchers.IO) {
                    val filesDir = context.getExternalFilesDir(null) ?: return@withContext
                    val targetDir = if (folder.isEmpty()) filesDir else File(filesDir, folder)
                    overrideAsFolder(targetDir)
                    copyAssetsFolder(context.assets, folder, targetDir)
                }
                firstException?.run {
                    onError?.also {
                        handler.post { it((this.cause ?: this).message ?: "Unknown Error") }
                    }
                }
            }.invokeOnCompletion {
                onFinished?.also { handler.post(it) }
            }
        }, 100)
    }

    private fun copyAssetsFolder(assets: AssetManager, folder: String, targetDir: File) {
        try {
            assets.list(folder)?.forEach {
                val fullAssetPath = if (folder.isEmpty()) it else "$folder/$it"
                val isDirectory = isDirectoryInAssets(assets, fullAssetPath)
                val subFile = File(targetDir, it)
                if (isDirectory) {
                    overrideAsFolder(subFile)
                    copyAssetsFolder(assets, fullAssetPath, subFile)
                } else {
                    subFile.deleteRecursively()
                    copyFile(
                        assets.open(fullAssetPath),
                        FileOutputStream(subFile)
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (firstException == null) firstException = e
        }
    }

    private fun overrideAsFolder(folder: File) {
        if (folder.isDirectory) return
        if (folder.exists()) folder.delete()
        folder.mkdirs()
    }

    private fun isDirectoryInAssets(assets: AssetManager, path: String): Boolean {
        return try {
            !assets.list(path).isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
    }

    private fun copyFile(inputStream: InputStream, outputStream: OutputStream) {
        try {
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            outputStream.flush()
        } catch (e: Exception) {
            e.printStackTrace()
            if (firstException == null) firstException = e
        } finally {
            inputStream.runCatching { close() }
            outputStream.runCatching { close() }
        }
    }
}
