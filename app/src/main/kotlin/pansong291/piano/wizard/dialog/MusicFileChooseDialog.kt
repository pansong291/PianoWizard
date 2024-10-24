package pansong291.piano.wizard.dialog

import android.annotation.SuppressLint
import android.content.Context
import com.hjq.toast.Toaster
import pansong291.piano.wizard.R
import pansong291.piano.wizard.consts.StringConst
import java.io.FileFilter

@SuppressLint("NotifyDataSetChanged")
class MusicFileChooseDialog(context: Context) : FileChooseDialog(context) {
    init {
        initialize { ok ->
            // TODO 后续支持弹奏多个乐谱时改造 ok 按钮
            ok.setText(R.string.make_as_default_folder)
            ok.setOnClickListener {
                sharedPreferences.edit()
                    .putString(StringConst.SP_DATA_KEY_DEFAULT_FOLDER, adapter.basePath).apply()
                Toaster.show(R.string.make_default_folder_feedback_message)
            }
        }
        setIcon(R.drawable.outline_music_file_32)
        setTitle(R.string.select_music)
        sharedPreferences.getString(StringConst.SP_DATA_KEY_DEFAULT_FOLDER, null)
            ?.let { adapter.basePath = it }
        adapter.fileFilter = FileFilter {
            it.isDirectory || it.name.endsWith(StringConst.MUSIC_NOTATION_FILE_EXT, true)
        }
    }
}
