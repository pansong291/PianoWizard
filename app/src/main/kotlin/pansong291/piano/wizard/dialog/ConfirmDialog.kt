package pansong291.piano.wizard.dialog

import android.content.Context
import android.widget.TextView
import androidx.annotation.StringRes
import pansong291.piano.wizard.dialog.actions.DialogConfirmActions
import pansong291.piano.wizard.dialog.base.BaseDialog
import pansong291.piano.wizard.dialog.contents.DialogMessageContent

class ConfirmDialog(context: Context) : BaseDialog(context) {
    private val textView: TextView = DialogMessageContent.loadIn(this)
    var onOk: (() -> Unit)? = null
    var onCancel: (() -> Unit)? = { destroy() }

    init {
        DialogConfirmActions.loadIn(this) { ok, cancel ->
            ok.setOnClickListener { onOk?.invoke() }
            cancel.setOnClickListener { onCancel?.invoke() }
        }
    }

    fun setText(@StringRes id: Int) {
        textView.setText(id)
    }

    fun setText(text: CharSequence) {
        textView.text = text
    }
}
