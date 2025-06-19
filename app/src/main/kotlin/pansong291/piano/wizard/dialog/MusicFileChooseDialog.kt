package pansong291.piano.wizard.dialog

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import pansong291.piano.wizard.R
import pansong291.piano.wizard.consts.StringConst
import java.io.File
import java.io.FileFilter

class MusicFileChooseDialog(context: Context, scope: CoroutineScope) : FileChooseDialog(context) {
    init {
        initialize(scope) { ok ->
            ok.setText(R.string.goto_default_folder)
            ok.setOnClickListener {
                adapter.gotoFolder(File(context.getExternalFilesDir(null), "yp"))
            }
        }
        setIcon(R.drawable.outline_music_file_32)
        setTitle(R.string.select_music)
        adapter.fileFilter = FileFilter {
            it.isDirectory || it.name.endsWith(StringConst.MUSIC_NOTATION_FILE_EXT, true)
        }
    }

    fun setFolder(folder: String) {
        adapter.basePath = folder
    }
}
