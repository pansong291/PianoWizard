package pansong291.piano.wizard.dialog.contents

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Environment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import pansong291.piano.wizard.R
import pansong291.piano.wizard.consts.ColorConst
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
                // TODO 支持路径跳转, 如果其不以内部存储路径开头, 则不关闭输入框并提示路径非法,
                //  如果目标是文件, 则跳转至其父目录, 否则跳转至其内
                false
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
        var basePath: String = Environment.getExternalStorageDirectory().path
        var fileFilter: FileFilter = FileFilter { true }
        var onFileChose: ((path: String, file: String) -> Unit)? = null
        var onPathLoaded: ((path: String) -> Unit)? = null
        var onPathChanged: ((path: String) -> Unit)? = null
        private var infoFilter: ((FileInfo) -> Boolean)? = null

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
            val cur = File(basePath)
            if (Environment.getExternalStorageDirectory() == cur) return
            loadFileList(cur.parentFile)
        }

        fun forwardFolder(folder: String) {
            val file = File(basePath, folder)
            loadFileList(file)
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
            infoList = folderFile.listFiles(fileFilter)?.map {
                FileInfo().apply {
                    icon = if (it.isDirectory) R.drawable.outline_folder_24
                    else R.drawable.outline_file_24
                    name = it.name
                }
            }?.sortedWith { p, q ->
                when (p.icon) {
                    q.icon -> p.name.compareTo(q.name)
                    R.drawable.outline_folder_24 -> -1
                    else -> 1
                }
            } ?: emptyList()
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
