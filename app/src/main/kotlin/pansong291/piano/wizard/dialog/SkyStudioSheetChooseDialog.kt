package pansong291.piano.wizard.dialog

import android.content.Context
import pansong291.piano.wizard.R
import pansong291.piano.wizard.consts.StringConst
import java.io.FileFilter

class SkyStudioSheetChooseDialog(context: Context) : FileChooseDialog(context) {
    init {
        initialize { ok ->
            ok.setText(R.string.current_folder)
            ok.setOnClickListener {
                onFolderChose?.invoke(adapter.basePath)
            }
        }
        setTitle(R.string.select_sky_studio_sheet_file)
        sharedPreferences.getString(StringConst.SP_DATA_KEY_SKY_STUDIO_SHEET_LAST_FOLDER, null)
            ?.let { adapter.basePath = it }
        adapter.fileFilter = FileFilter {
            it.isDirectory || (it.name.endsWith(StringConst.SKY_STUDIO_SHEET_FILE_EXT, true) &&
                    !it.name.endsWith(StringConst.MUSIC_NOTATION_FILE_EXT, true))
        }
        adapter.onPathChanged = { path ->
            sharedPreferences.edit()
                .putString(StringConst.SP_DATA_KEY_SKY_STUDIO_SHEET_LAST_FOLDER, path)
                .apply()
        }
    }
}
