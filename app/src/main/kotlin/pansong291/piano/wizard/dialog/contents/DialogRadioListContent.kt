package pansong291.piano.wizard.dialog.contents

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import pansong291.piano.wizard.ViewUtil
import pansong291.piano.wizard.dialog.IDialog

object DialogRadioListContent {
    fun loadIn(dialog: IDialog, data: List<String>, default: Int?): Adapter {
        val content = FastScrollRecyclerView(dialog.getAppContext())
        dialog.getContentWrapper().addView(content)
        content.layoutManager = LinearLayoutManager(dialog.getAppContext()).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        return Adapter(dialog.getAppContext(), data, default).also {
            content.adapter = it
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class Adapter(
        private val context: Context,
        private var data: List<String>,
        default: Int?
    ) : RecyclerView.Adapter<ViewHolder>() {
        private var selectedPosition = checkPosition(default)
        private val padding = ViewUtil.dpToPx(context, 16f).toInt()

        @SuppressLint("NotifyDataSetChanged")
        fun reload(data: List<String>, default: Int?) {
            this.data = data
            selectedPosition = checkPosition(default)
            notifyDataSetChanged()
        }

        fun getSelectedPosition() = selectedPosition

        fun getSelectedString(): String? {
            if (selectedPosition < 0) return null
            return data[selectedPosition]
        }

        private fun checkPosition(p: Int?): Int {
            return p?.takeIf { it < data.size && it >= 0 } ?: -1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val root = LayoutInflater.from(context)
                .inflate(android.R.layout.select_dialog_singlechoice, parent, false)
            root.setPadding(padding, 0, padding, 0)
            val txt = root.findViewById<CheckedTextView>(android.R.id.text1)
            txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
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
