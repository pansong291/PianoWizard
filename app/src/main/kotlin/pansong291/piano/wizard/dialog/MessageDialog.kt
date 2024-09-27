package pansong291.piano.wizard.dialog

import android.app.Application
import android.widget.TextView
import androidx.annotation.StringRes
import pansong291.piano.wizard.dialog.actions.DialogCommonActions
import pansong291.piano.wizard.dialog.contents.DialogMessageContent

class MessageDialog(application: Application) : BaseDialog(application) {
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
