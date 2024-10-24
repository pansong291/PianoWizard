package pansong291.piano.wizard.dialog.actions

import android.view.View
import android.widget.Button
import android.widget.CheckBox
import pansong291.piano.wizard.R
import pansong291.piano.wizard.dialog.base.IDialog

object DialogCheckConfirmActions {
    fun loadIn(dialog: IDialog, block: (cb: CheckBox, ok: Button, cancel: Button) -> Unit) {
        val actions = View.inflate(
            dialog.getContext(),
            R.layout.dialog_actions_check_confirm,
            dialog.findActionsWrapper()
        )
        // 操作区：复选框、取消和确定按钮
        val checkbox = actions.findViewById<CheckBox>(android.R.id.checkbox)
        val cancel = actions.findViewById<Button>(android.R.id.button1)
        val ok = actions.findViewById<Button>(android.R.id.button2)
        cancel.setOnClickListener { dialog.destroy() }
        block.invoke(checkbox, ok, cancel)
    }
}
