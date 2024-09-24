package pansong291.piano.wizard.dialog.contents

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import pansong291.piano.wizard.R
import pansong291.piano.wizard.dialog.IDialog

object DialogRadioListContent {
    fun loadIn(dialog: IDialog, data: List<String>, default: Int?): Adapter {
        val content = FastScrollRecyclerView(dialog.getAppContext())
        dialog.findContentWrapper().addView(content)
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
        private var lastChecked: RadioButton? = null

        /**
         * @param selected 选择项。为 null 则不更新
         */
        @SuppressLint("NotifyDataSetChanged")
        fun reload(data: List<String>, selected: Int?) {
            this.data = data
            selected?.let { selectedPosition = checkPosition(it) }
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
                .inflate(R.layout.list_item_radio, parent, false)
            return ViewHolder(root)
        }

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val radio = holder.itemView.findViewById<RadioButton>(android.R.id.checkbox)
            radio.text = data[position]
            radio.isChecked = selectedPosition == position
            if (radio.isChecked) lastChecked = radio
            holder.itemView.setOnClickListener {
                if (selectedPosition == holder.adapterPosition) return@setOnClickListener
                selectedPosition = holder.adapterPosition
                radio.isChecked = true
                lastChecked?.let { it.isChecked = false }
                lastChecked = radio
            }
        }
    }
}
