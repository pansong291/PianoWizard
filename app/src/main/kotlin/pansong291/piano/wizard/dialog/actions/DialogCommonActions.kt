package pansong291.piano.wizard.dialog.actions

import android.view.View
import android.widget.Button
import pansong291.piano.wizard.R
import pansong291.piano.wizard.dialog.IDialog

object DialogCommonActions {
    fun loadIn(dialog: IDialog, block: (ok: Button) -> Unit) {
        val actions = View.inflate(
            dialog.getAppContext(),
            R.layout.dialog_actions_common,
            dialog.findActionsWrapper()
        )
        // 操作区：确定按钮
        val ok = actions.findViewById<Button>(android.R.id.button1)
        ok.setOnClickListener { dialog.destroy() }
        block.invoke(ok)
    }
}
