package pansong291.piano.wizard.dialog.actions

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import pansong291.piano.wizard.R
import pansong291.piano.wizard.dialog.base.IDialog


object DialogFilterInputActions {
    fun loadIn(dialog: IDialog, block: (ok: Button, cancel: Button, input: EditText) -> Unit) {
        val context = dialog.getContext()
        val actions = View.inflate(
            context,
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
                input.post {
                    input.requestFocus()
                    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                        .showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
                }
            } else {
                input.visibility = View.GONE
                frame.visibility = View.VISIBLE
            }
        }
        input.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(v.windowToken, 0)
            }
        }

        cancel.setOnClickListener { dialog.destroy() }
        block.invoke(ok, cancel, input)
    }
}
