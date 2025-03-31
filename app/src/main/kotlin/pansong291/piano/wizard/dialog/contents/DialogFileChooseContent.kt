package pansong291.piano.wizard.dialog.contents

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hjq.toast.Toaster
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import pansong291.piano.wizard.R
import pansong291.piano.wizard.consts.ColorConst
import pansong291.piano.wizard.consts.StringConst
import pansong291.piano.wizard.dialog.TextInputDialog
import pansong291.piano.wizard.dialog.base.IDialog
import java.io.File
import java.io.FileFilter

object DialogFileChooseContent {
    fun loadIn(dialog: IDialog): Pair<FastScrollRecyclerView, FileListAdapter> {
        val context = dialog.getContext()
        val content = View.inflate(
            context,
            R.layout.dialog_content_file_choose,
            dialog.findContentWrapper()
        )
        val adapter = FileListAdapter(context)
        // 主内容：一个回退按钮和文件列表
        val backwardItem = content.findViewById<AppCompatTextView>(android.R.id.undo).apply {
            ellipsize = TextUtils.TruncateAt.START
            setOnClickListener { adapter.backwardFolder() }
            setOnLongClickListener {
                val tid = TextInputDialog(context)
                tid.setIcon(R.drawable.outline_turn_right_32)
                tid.setTitle(R.string.go_to)
                tid.setText(adapter.basePath)
                tid.onTextConfirmed = {
                    val p = it.trim().toString()
                    if (p.startsWith(StringConst.EXTERNAL_PATH)) {
                        val f = File(p)
                        if (f.isFile) adapter.gotoFolder(f.parentFile)
                        else adapter.gotoFolder(f)
                        tid.destroy()
                    } else {
                        Toaster.show(R.string.invalid_path_message)
                    }
                }
                tid.show()
                true
            }
        }
        val recyclerView = content.findViewById<FastScrollRecyclerView>(android.R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(context).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        recyclerView.adapter = adapter
        adapter.onPathLoaded = {
            backwardItem.text = it
            // 滚动到高亮条目使其在列表的可见范围内
            if (adapter.highlight?.parent == adapter.basePath) {
                val position = adapter.findItemPosition { info ->
                    adapter.highlight?.name == info.name
                }
                if (position >= 0) recyclerView.scrollToPosition(position)
            }
        }
        return recyclerView to adapter
    }

    class FileInfo {
        var icon: Int = 0
        var name: String = ""
    }

    class FileViewHolder(val textView: AppCompatTextView) : RecyclerView.ViewHolder(textView)

    class FileListAdapter(
        private val context: Context
    ) : RecyclerView.Adapter<FileViewHolder>() {
        private lateinit var infoList: List<FileInfo>
        private lateinit var filteredList: List<FileInfo>
        var highlight: File? = null
        var basePath: String = StringConst.EXTERNAL_PATH
        var fileFilter: FileFilter = FileFilter { true }
        var onFileChose: ((path: String, file: String) -> Unit)? = null
        var onPathLoaded: ((path: String) -> Unit)? = null
        var onPathChanged: ((path: String) -> Unit)? = null
        private var infoFilter: ((FileInfo) -> Boolean)? = null
        private val dataFolder = context.getExternalFilesDir(null)?.parentFile
        private val ypFolder = File(context.getExternalFilesDir(null), "yp").path

        fun reload() {
            loadFileList(null)
        }

        fun setInfoFilter(filter: ((FileInfo) -> Boolean)?) {
            infoFilter = filter
            filteredList = filter?.let {
                infoList.filter(it)
            } ?: infoList
        }

        fun backwardFolder() {
            if (basePath == StringConst.EXTERNAL_PATH) return
            loadFileList(File(basePath).parentFile)
        }

        fun forwardFolder(folder: String) {
            val file = File(basePath, folder)
            loadFileList(file)
        }

        fun gotoFolder(folder: File?) {
            loadFileList(folder)
        }

        fun findItemPosition(predicate: (FileInfo) -> Boolean): Int {
            return filteredList.indexOfFirst(predicate)
        }

        fun getItem(position: Int): FileInfo? {
            return filteredList.getOrNull(position)
        }

        @SuppressLint("NotifyDataSetChanged")
        private fun loadFileList(folder: File?) {
            val folderFile = folder ?: File(basePath)
            if (folderFile.path != basePath) {
                basePath = folderFile.path
                onPathChanged?.invoke(basePath)
            }
            val childFiles = folderFile.listFiles(FileFilter {
                it.isDirectory
            })?.toMutableList() ?: mutableListOf()
            dataFolder?.run {
                if (folderFile.path == this.parent && childFiles.indexOfFirst { it.name == this.name } < 0) {
                    childFiles += this
                }
                if (folderFile.path == ypFolder) {
                    childFiles.clear()
                    File(ypFolder, StringConst.TRIAL_MUSIC_NAME).takeIf { fileFilter.accept(it) }
                        ?.let { childFiles += it }
                }
            }
            infoList = childFiles.map {
                FileInfo().apply {
                    icon = if (it.isDirectory) R.drawable.outline_folder_24
                    else R.drawable.outline_file_24
                    name = it.name
                }
            }.sortedWith { p, q ->
                when (p.icon) {
                    q.icon -> p.name.compareTo(q.name)
                    R.drawable.outline_folder_24 -> -1
                    else -> 1
                }
            }
            setInfoFilter(infoFilter)
            notifyDataSetChanged()
            onPathLoaded?.invoke(folderFile.path)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.list_item_file, parent, false) as AppCompatTextView
            return FileViewHolder(view)
        }

        override fun getItemCount(): Int {
            return filteredList.size
        }

        override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
            val item = filteredList[position]
            holder.textView.setTextColor(
                if (highlight?.parent == basePath && highlight?.name == item.name) ColorConst.GREEN_600
                else Color.BLACK
            )
            holder.textView.text = item.name
            holder.textView.setCompoundDrawablesRelativeWithIntrinsicBounds(item.icon, 0, 0, 0)
            holder.itemView.setOnClickListener {
                if (item.icon == R.drawable.outline_folder_24) {
                    forwardFolder(item.name)
                } else {
                    onFileChose?.invoke(basePath, item.name)
                }
            }
        }
    }
}
