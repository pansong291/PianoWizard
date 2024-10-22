package pansong291.piano.wizard.dialog

import android.annotation.SuppressLint
import android.content.Context
import com.hjq.toast.Toaster
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import pansong291.piano.wizard.R
import pansong291.piano.wizard.consts.StringConst
import pansong291.piano.wizard.dialog.actions.DialogFilterInputActions
import pansong291.piano.wizard.dialog.base.BaseDialog
import pansong291.piano.wizard.dialog.contents.DialogFileChooseContent
import pansong291.piano.wizard.utils.ViewUtil
import java.io.FileFilter

@SuppressLint("NotifyDataSetChanged")
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
        DialogFilterInputActions.loadIn(this) { ok, _, input ->
            // TODO 后续支持弹奏多个乐谱时改造 ok 按钮
            ok.setText(R.string.make_as_default_folder)
            ok.setOnClickListener {
                sharedPreferences.edit()
                    .putString(StringConst.SP_DATA_KEY_DEFAULT_FOLDER, adapter.basePath).apply()
                Toaster.show(R.string.make_default_folder_feedback_message)
            }

            ViewUtil.debounceInputChanges(input, 300L) { query ->
                adapter.setInfoFilter(if (query.isNullOrEmpty()) null else { info ->
                    info.name.contains(query, true)
                })
                adapter.notifyDataSetChanged()
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
