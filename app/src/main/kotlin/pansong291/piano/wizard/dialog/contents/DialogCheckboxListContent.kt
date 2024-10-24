package pansong291.piano.wizard.dialog.contents

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import pansong291.piano.wizard.R
import pansong291.piano.wizard.dialog.base.IDialog

object DialogCheckboxListContent {
    fun loadIn(dialog: IDialog, data: List<String>, default: Set<Int>): Adapter {
        val context = dialog.getContext()
        val content = FastScrollRecyclerView(context)
        dialog.findContentWrapper().addView(content)
        content.layoutManager = LinearLayoutManager(context).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        return Adapter(context, data, default).also {
            content.adapter = it
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class Adapter(
        private val context: Context,
        private var data: List<String>,
        default: Set<Int>
    ) : RecyclerView.Adapter<ViewHolder>() {
        private var selectedPositions = checkPositions(default)
        var onItemCheckChanged: ((p: Int, c: Boolean) -> Unit)? = null

        /**
         * @param selected 选择项。为 null 则保持原来的选择项
         */
        @SuppressLint("NotifyDataSetChanged")
        fun reload(data: List<String>, selected: Set<Int>?) {
            this.data = data
            if (selected != null) selectedPositions = selected
            notifyDataSetChanged()
        }

        fun getSelectedPositions() = selectedPositions

        private fun checkPositions(p: Set<Int>): Set<Int> {
            return p.filterTo(mutableSetOf()) { it < data.size && it >= 0 }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val root = LayoutInflater.from(context)
                .inflate(R.layout.list_item_checkbox, parent, false)
            return ViewHolder(root)
        }

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val checkBox = holder.itemView.findViewById<CheckBox>(android.R.id.checkbox)
            checkBox.text = data[position]
            checkBox.isChecked = selectedPositions.contains(position)
            holder.itemView.setOnClickListener {
                val p = holder.adapterPosition
                val toCheck = !selectedPositions.contains(p)
                if (toCheck) selectedPositions += p
                else selectedPositions -= p
                onItemCheckChanged?.invoke(p, toCheck)
                checkBox.isChecked = toCheck
            }
        }
    }
}
