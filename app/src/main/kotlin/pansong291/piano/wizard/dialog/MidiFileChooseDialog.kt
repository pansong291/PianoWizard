package pansong291.piano.wizard.dialog

import android.content.Context
import pansong291.piano.wizard.R
import pansong291.piano.wizard.consts.StringConst
import java.io.FileFilter

class MidiFileChooseDialog(context: Context) : FileChooseDialog(context) {
    init {
        initialize { ok ->
            ok.setOnClickListener { destroy() }
        }
        setTitle(R.string.select_midi_file)
        sharedPreferences.getString(StringConst.SP_DATA_KEY_MIDI_FILE_LAST_FOLDER, null)
            ?.let { adapter.basePath = it }
        adapter.fileFilter = FileFilter {
            it.isDirectory || it.name.endsWith(StringConst.MIDI_FILE_EXT, true)
        }
        adapter.onPathChanged = { path ->
            sharedPreferences.edit()
                .putString(StringConst.SP_DATA_KEY_MIDI_FILE_LAST_FOLDER, path)
                .apply()
        }
    }
}
