package pansong291.piano.wizard.coroutine

import android.content.Context
import android.content.res.AssetManager
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object AssetsExtractor {
    private val handler = Handler(Looper.getMainLooper())
    private var firstException: Throwable? = null
    var onError: ((message: String) -> Unit)? = null
    var onFinished: (() -> Unit)? = null

    fun startExtraction(context: Context, scope: CoroutineScope) {
        scope.launch {
            val targetDir = context.getExternalFilesDir(null) ?: return@launch
            copyAssetsFolder(context.assets, "", targetDir)
            firstException?.run {
                onError?.also {
                    handler.post { it((this.cause ?: this).message ?: "Unknown Error") }
                }
            }
        }.invokeOnCompletion {
            onFinished?.also { handler.post(it) }
        }
    }

    private fun copyAssetsFolder(assets: AssetManager, folder: String, targetDir: File) {
        try {
            val fileList = assets.list(folder) ?: return
            for (fileName in fileList) {
                val fullAssetPath = if (folder.isEmpty()) fileName else "$folder/$fileName"
                val isDirectory = isDirectoryInAssets(assets, fullAssetPath)
                if (isDirectory) {
                    val subDir = File(targetDir, fileName)
                    subDir.mkdirs()
                    copyAssetsFolder(assets, fullAssetPath, subDir)
                } else {
                    copyFile(
                        assets.open(fullAssetPath),
                        FileOutputStream(File(targetDir, fileName))
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (firstException == null) firstException = e
        }
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
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            outputStream.flush()
        } catch (e: Exception) {
            e.printStackTrace()
            if (firstException == null) firstException = e
        } finally {
            try {
                inputStream.close()
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
