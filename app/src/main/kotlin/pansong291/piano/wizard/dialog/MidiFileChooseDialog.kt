package pansong291.piano.wizard.dialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import pansong291.piano.wizard.R
import pansong291.piano.wizard.consts.StringConst
import java.io.FileFilter

class MidiFileChooseDialog(context: Context) : FileChooseDialog(context) {
    init {
        initialize { ok ->
            ok.setText(R.string.demo_video)
            ok.setOnClickListener {
                if (context is Activity) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setData(Uri.parse(context.getString(R.string.link_midi_demo_video)))
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    }
                }
            }
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
