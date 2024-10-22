package pansong291.piano.wizard.dialog.actions

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import pansong291.piano.wizard.R
import pansong291.piano.wizard.dialog.base.IDialog

object DialogFilterInputActions {
    fun loadIn(dialog: IDialog, block: (ok: Button, cancel: Button, input: EditText) -> Unit) {
        val actions = View.inflate(
            dialog.getContext(),
            R.layout.dialog_actions_filter_input,
            dialog.findActionsWrapper()
        )
        // 操作区：过滤、取消和确定按钮，以及输入框
        val filter = actions.findViewById<Button>(android.R.id.button1)
        val input = actions.findViewById<EditText>(android.R.id.input)
        val frame = actions.findViewById<LinearLayout>(android.R.id.widget_frame)
        val cancel = actions.findViewById<Button>(android.R.id.button2)
        val ok = actions.findViewById<Button>(android.R.id.button3)
        filter.setOnClickListener {
            val toFilter = input.visibility == View.GONE
            if (toFilter) {
                input.visibility = View.VISIBLE
                frame.visibility = View.GONE
            } else {
                input.visibility = View.GONE
                frame.visibility = View.VISIBLE
            }
        }
        cancel.setOnClickListener { dialog.destroy() }
        block.invoke(ok, cancel, input)
    }
}
