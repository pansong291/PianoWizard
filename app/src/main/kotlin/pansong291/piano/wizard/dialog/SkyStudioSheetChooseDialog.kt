package pansong291.piano.wizard.dialog

import android.annotation.SuppressLint
import android.content.Context
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import pansong291.piano.wizard.R
import pansong291.piano.wizard.consts.StringConst
import pansong291.piano.wizard.dialog.actions.DialogFilterInputActions
import pansong291.piano.wizard.dialog.base.BaseDialog
import pansong291.piano.wizard.dialog.contents.DialogFileChooseContent
import pansong291.piano.wizard.utils.ViewUtil
import java.io.FileFilter

@SuppressLint("NotifyDataSetChanged")
class SkyStudioSheetChooseDialog(context: Context) : BaseDialog(context) {
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
    var onFolderChose: ((path: String) -> Unit)? = null

    init {
        setIcon(R.drawable.outline_file_24)
        setTitle(R.string.select_sky_studio_sheet_file)
        val pair = DialogFileChooseContent.loadIn(this)
        recyclerView = pair.first
        adapter = pair.second
        sharedPreferences.getString(StringConst.SP_DATA_KEY_SKY_STUDIO_SHEET_LAST_FOLDER, null)
            ?.let { adapter.basePath = it }
        adapter.fileFilter = FileFilter {
            it.isDirectory || (it.name.endsWith(StringConst.SKY_STUDIO_SHEET_FILE_EXT) &&
                    !it.name.endsWith(StringConst.MUSIC_NOTATION_FILE_EXT))
        }
        adapter.onPathChanged = { path ->
            sharedPreferences.edit()
                .putString(StringConst.SP_DATA_KEY_SKY_STUDIO_SHEET_LAST_FOLDER, path)
                .apply()
        }
        DialogFilterInputActions.loadIn(this) { ok, _, input ->
            ok.setText(R.string.current_folder)
            ok.setOnClickListener {
                onFolderChose?.invoke(adapter.basePath)
            }
            ViewUtil.debounceInputChanges(input, 300L) { query ->
                adapter.setInfoFilter(if (query.isNullOrEmpty()) null else { info ->
                    info.name.contains(query, true)
                })
                adapter.notifyDataSetChanged()
            }
        }
    }

    fun reload() {
        adapter.reload()
    }

    override fun show() {
        adapter.reload()
        super.show()
    }
}
