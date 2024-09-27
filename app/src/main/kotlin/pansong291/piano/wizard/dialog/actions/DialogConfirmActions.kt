package pansong291.piano.wizard.dialog.actions

import android.view.View
import android.widget.Button
import pansong291.piano.wizard.R
import pansong291.piano.wizard.dialog.IDialog

object DialogConfirmActions {
    fun loadIn(dialog: IDialog, block: (ok: Button, cancel: Button) -> Unit) {
        val actions = View.inflate(
            dialog.getAppContext(),
            R.layout.dialog_actions_confirm,
            dialog.findActionsWrapper()
        )
        // 操作区：取消和确定按钮
        val cancel = actions.findViewById<Button>(android.R.id.button1)
        val ok = actions.findViewById<Button>(android.R.id.button2)
        cancel.setOnClickListener { dialog.destroy() }
        block.invoke(ok, cancel)
    }
}
