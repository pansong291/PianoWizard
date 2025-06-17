package pansong291.piano.wizard.dialog

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import pansong291.piano.wizard.R
import pansong291.piano.wizard.consts.StringConst
import java.io.FileFilter

class MusicFolderChooseDialog(context: Context, scope: CoroutineScope) : FileChooseDialog(context) {
    init {
        initialize(scope) { ok ->
            ok.setText(R.string.current_folder)
            ok.setOnClickListener {
                onFolderChose?.invoke(adapter.basePath)
            }
        }
        setIcon(R.drawable.outline_music_file_32)
        setTitle(R.string.select_folder)
        adapter.fileFilter = FileFilter {
            it.isDirectory || it.name.endsWith(StringConst.MUSIC_NOTATION_FILE_EXT, true)
        }
    }

    fun setFolder(folder: String) {
        adapter.basePath = folder
    }
}
