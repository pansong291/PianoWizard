package pansong291.piano.wizard.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.widget.Button
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import pansong291.piano.wizard.R
import pansong291.piano.wizard.consts.StringConst
import pansong291.piano.wizard.dialog.actions.DialogFilterInputActions
import pansong291.piano.wizard.dialog.base.BaseDialog
import pansong291.piano.wizard.dialog.contents.DialogFileChooseContent
import pansong291.piano.wizard.utils.ViewUtil
import java.io.File
import java.io.FileFilter

@SuppressLint("NotifyDataSetChanged")
open class FileChooseDialog(context: Context) : BaseDialog(context) {
    protected val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        StringConst.SHARED_PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )
    protected lateinit var recyclerView: FastScrollRecyclerView
    protected lateinit var adapter: DialogFileChooseContent.FileListAdapter
    var onFolderChose: ((path: String) -> Unit)? = null

    init {
        setIcon(R.drawable.outline_file_24)
    }

    protected fun initialize(initOk: (Button) -> Unit) {
        val pair = DialogFileChooseContent.loadIn(this)
        recyclerView = pair.first
        adapter = pair.second
        DialogFilterInputActions.loadIn(this) { ok, _, input ->
            initOk(ok)
            ViewUtil.debounceInputChanges(input, 300L) { query ->
                adapter.setInfoFilter(if (query.isNullOrEmpty()) null else { info ->
                    info.name.contains(query, true)
                })
                adapter.notifyDataSetChanged()
            }
        }
    }

    fun setOnFileChose(ofc: (path: String, file: String) -> Unit) {
        adapter.onFileChose = ofc
    }

    fun setFileFilter(filter: FileFilter) {
        adapter.fileFilter = filter
    }

    fun setHighlight(path: String?) {
        adapter.highlight = path?.let { File(path) }
    }

    fun reload() {
        adapter.reload()
    }

    override fun show() {
        adapter.reload()
        super.show()
    }
}
