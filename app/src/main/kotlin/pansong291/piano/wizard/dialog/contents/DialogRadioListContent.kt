package pansong291.piano.wizard.dialog.contents

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hjq.window.EasyWindow

object DialogRadioListContent {
    fun loadIn(dialog: EasyWindow<*>, data: List<String>, default: Int?): Adapter {
        val content = RecyclerView(dialog.context)
        dialog.contentView = content
        content.layoutManager = LinearLayoutManager(dialog.context).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        return Adapter(dialog.context, data, default).also {
            content.adapter = it
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class Adapter(
        private val context: Context,
        private val data: List<String>,
        default: Int?
    ) : RecyclerView.Adapter<ViewHolder>() {
        private var selectedPosition = default?.takeIf { it < data.size } ?: -1

        fun getSelectedPosition() = selectedPosition

        fun getSelectedString(): String? {
            if (selectedPosition < 0) return null
            return data[selectedPosition]
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val root = View.inflate(context, android.R.layout.select_dialog_singlechoice, null)
            return ViewHolder(root)
        }

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val txt = holder.itemView.findViewById<CheckedTextView>(android.R.id.text1)
            txt.text = data[position]
            txt.isChecked = selectedPosition == position
            txt.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = holder.adapterPosition
                if (oldPosition >= 0) notifyItemChanged(oldPosition)
                if (selectedPosition >= 0) notifyItemChanged(selectedPosition)
            }
        }
    }
}
