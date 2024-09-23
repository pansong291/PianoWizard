package pansong291.piano.wizard.dialog.actions

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.hjq.window.EasyWindow
import pansong291.piano.wizard.R

object DialogCommonActions {
    fun loadIn(dialog: EasyWindow<*>, block: (ok: Button, cancel: Button) -> Unit) {
        val actions = View.inflate(dialog.context, R.layout.dialog_actions_common, null)
        dialog.contentView.findViewById<ViewGroup>(android.R.id.extractArea).addView(actions)
        // 操作区：取消和确定按钮
        val cancel = actions.findViewById<Button>(android.R.id.button1)
        val ok = actions.findViewById<Button>(android.R.id.button2)
        cancel.setOnClickListener { dialog.cancel() }
        block.invoke(ok, cancel)
    }
}
