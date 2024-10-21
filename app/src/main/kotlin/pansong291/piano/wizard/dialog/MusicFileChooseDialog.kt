package pansong291.piano.wizard.dialog

import android.content.Context
import com.hjq.toast.Toaster
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import pansong291.piano.wizard.R
import pansong291.piano.wizard.consts.StringConst
import pansong291.piano.wizard.dialog.actions.DialogConfirmActions
import pansong291.piano.wizard.dialog.base.BaseDialog
import pansong291.piano.wizard.dialog.contents.DialogFileChooseContent
import java.io.FileFilter

class MusicFileChooseDialog(context: Context) : BaseDialog(context) {
    private val sharedPreferences = context.getSharedPreferences(
        StringConst.SHARED_PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )
    private val recyclerView: FastScrollRecyclerView
    private val adapter: DialogFileChooseContent.FileListAdapter
    var onFileChose
        get() = adapter.onFileChose
        set(value) {
            adapter.onFileChose = value
        }
    var scrollTo: ((String, DialogFileChooseContent.FileInfo) -> Boolean)? = null

    init {
        setIcon(R.drawable.outline_music_file_32)
        setTitle(R.string.select_music)
        val pair = DialogFileChooseContent.loadIn(this)
        recyclerView = pair.first
        adapter = pair.second
        sharedPreferences.getString(StringConst.SP_DATA_KEY_DEFAULT_FOLDER, null)
            ?.let { adapter.basePath = it }
        adapter.fileFilter = FileFilter {
            it.isDirectory || it.name.endsWith(StringConst.MUSIC_NOTATION_FILE_EXT)
        }
        DialogConfirmActions.loadIn(this) { ok, _ ->
            ok.setText(R.string.make_as_default_folder)
            ok.setOnClickListener {
                sharedPreferences.edit()
                    .putString(StringConst.SP_DATA_KEY_DEFAULT_FOLDER, adapter.basePath).apply()
                Toaster.show(R.string.make_default_folder_feedback_message)
            }
        }
    }

    fun setHighlight(path: String) {
        adapter.highlight = path
    }

    override fun show() {
        adapter.reload()
        scrollTo?.also {
            val position = adapter.findItemPosition { info ->
                it(adapter.basePath, info)
            }
            if (position >= 0) recyclerView.scrollToPosition(position)
        }
        super.show()
    }
}
