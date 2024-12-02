package pansong291.piano.wizard.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import pansong291.piano.wizard.R
import pansong291.piano.wizard.consts.StringConst
import java.io.File
import java.io.FileFilter

@SuppressLint("NotifyDataSetChanged")
class MusicFileChooseDialog(context: Context) : FileChooseDialog(context) {
    init {
        initialize { ok ->
            // TODO 后续支持弹奏多个乐谱时改造 ok 按钮
            ok.visibility = View.GONE
        }
        setIcon(R.drawable.outline_music_file_32)
        setTitle(R.string.select_music)
        adapter.basePath = File(context.getExternalFilesDir(null), "yp").path
        adapter.fileFilter = FileFilter {
            it.isDirectory || it.name.endsWith(StringConst.MUSIC_NOTATION_FILE_EXT, true)
        }
    }
}
