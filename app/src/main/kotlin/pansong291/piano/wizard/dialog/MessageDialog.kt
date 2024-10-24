package pansong291.piano.wizard.dialog

import android.content.Context
import android.widget.TextView
import androidx.annotation.StringRes
import pansong291.piano.wizard.R
import pansong291.piano.wizard.dialog.actions.DialogCommonActions
import pansong291.piano.wizard.dialog.base.BaseDialog
import pansong291.piano.wizard.dialog.contents.DialogMessageContent

class MessageDialog(context: Context) : BaseDialog(context) {
    companion object {
        fun showErrorMessage(context: Context, msg: String) {
            MessageDialog(context).apply {
                setIcon(R.drawable.outline_error_outline_32)
                setTitle(R.string.error)
                setText(msg)
                show()
            }
        }
    }

    private val textView: TextView = DialogMessageContent.loadIn(this)
    var onOkClick: (() -> Unit)? = { destroy() }

    init {
        DialogCommonActions.loadIn(this) { ok ->
            ok.setOnClickListener { onOkClick?.invoke() }
        }
    }

    fun setText(@StringRes id: Int) {
        textView.setText(id)
    }

    fun setText(text: CharSequence) {
        textView.text = text
    }
}
