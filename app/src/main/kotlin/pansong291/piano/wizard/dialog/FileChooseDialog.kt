package pansong291.piano.wizard.dialog

import android.app.Application
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pansong291.piano.wizard.R
import pansong291.piano.wizard.dialog.actions.DialogCommonActions
import pansong291.piano.wizard.toast.Toaster
import java.io.File
import java.io.FileFilter

class FileChooseDialog(val application: Application) : BaseDialog(application) {
    var basePath: String = Environment.getExternalStorageDirectory().path
    var fileFilter: FileFilter = FileFilter { true }

    init {
        val content = View.inflate(application, R.layout.dialog_content_file_choose, null)
        dialog.contentView.findViewById<ViewGroup>(android.R.id.content).addView(content)
        // 主内容：一个回退按钮和文件列表
        val backwardItem = content.findViewById<TextView>(android.R.id.undo)
        val recyclerView = content.findViewById<RecyclerView>(android.R.id.list)
        val adapter = FileListAdapter(basePath, fileFilter)
        adapter.setOnItemClickListener { info, _ ->
            if (info.icon == R.drawable.outline_folder_24) {
                adapter.forwardFolder(info.name)
            } else {
                Toaster.show(info.name)
                dialog.cancel()
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(application).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        recyclerView.adapter = adapter
        backwardItem.text = application.getString(R.string.path_backward)
        backwardItem.isClickable = true
        /*val typedValue = TypedValue()
        application.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
        backwardItem.setBackgroundResource(typedValue.resourceId)*/
        backwardItem.setOnClickListener { adapter.backwardFolder() }
        DialogCommonActions.loadIn(dialog) { ok, _ ->
            // 确定按钮
            ok.setOnClickListener {
                Toaster.show(adapter.getPath())
            }
        }
    }

    inner class FileInfo {
        var icon: Int = 0
        var name: String = ""
    }

    inner class FileViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    fun interface FileItemClickListener {
        fun onItemClick(info: FileInfo, position: Int)
    }

    inner class FileListAdapter(
        private var path: String,
        private val filter: FileFilter
    ) : RecyclerView.Adapter<FileViewHolder>() {
        private lateinit var fileList: List<FileInfo>
        private var itemClickListener: FileItemClickListener? = null

        init {
            loadFileList(null)
        }

        fun getPath() = path

        fun backwardFolder() {
            val cur = File(path)
            if (Environment.getExternalStorageDirectory() == cur.parentFile) return
            loadFileList(cur.parentFile)
            cur.parent?.let { path = it }
        }

        fun forwardFolder(folder: String) {
            val file = File(path, folder)
            loadFileList(file)
            path = file.path
        }

        fun setOnItemClickListener(listener: FileItemClickListener) {
            itemClickListener = listener
        }

        private fun loadFileList(folder: File?) {
            fileList = (folder ?: File(path)).listFiles(filter)?.map {
                FileInfo().apply {
                    icon = if (it.isDirectory) R.drawable.outline_folder_24
                    else R.drawable.outline_file_24
                    name = it.name
                }
            } ?: emptyList()
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
            val view = View.inflate(application, R.layout.list_item_file, null) as TextView
            return FileViewHolder(view)
        }

        override fun getItemCount(): Int {
            return fileList.size
        }

        override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
            val item = fileList[position]
            holder.textView.text = item.name
            holder.textView.setCompoundDrawablesRelativeWithIntrinsicBounds(item.icon, 0, 0, 0)
            holder.itemView.setOnClickListener {
                itemClickListener?.onItemClick(item, position)
            }
        }
    }
}
