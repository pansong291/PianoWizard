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
            //  支持列表弹奏模式, 与单曲模式分开, 该模式无法调整变调, 自动忽略无法完整弹奏问题
            //  初始时未选择歌曲, 自动切歌, 支持循环和随机
            //  暂停时可切歌, 支持进度调整 (需要换算播放进度)
            //  在开始列表播放模式后，一次性读取列表缓存起来，切歌的时候不必刷新列表
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
