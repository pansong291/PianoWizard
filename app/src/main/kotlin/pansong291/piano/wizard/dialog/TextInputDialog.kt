package pansong291.piano.wizard.dialog

import android.app.Application
import android.widget.EditText
import android.widget.LinearLayout
import pansong291.piano.wizard.dialog.actions.DialogCommonActions

class TextInputDialog(application: Application) : BaseDialog(application) {
    private val textInput = EditText(application)
    private var listener: OnOkClickListener? = null

    init {
        val wrap = LinearLayout(application)
        wrap.setPadding(16, 0, 16, 0)
        wrap.addView(textInput)
        getMainContent().addView(wrap)
        DialogCommonActions.loadIn(this) { ok, _ ->
            ok.setOnClickListener {
                listener?.onOk(textInput.text)
            }
        }
    }

    fun setText(text: CharSequence) {
        textInput.setText(text)
    }

    fun setOnOkClickListener(listener: OnOkClickListener) {
        this.listener = listener
    }

    fun interface OnOkClickListener {
        fun onOk(text: CharSequence)
    }
}
