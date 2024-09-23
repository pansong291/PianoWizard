package pansong291.piano.wizard.dialog

import android.app.Application
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import pansong291.piano.wizard.ViewUtil
import pansong291.piano.wizard.dialog.actions.DialogCommonActions

class TextInputDialog(application: Application) : BaseDialog(application) {
    private val textInput = EditText(application)
    private var listener: OnOkClickListener? = null

    init {
        dialog.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        val wrap = LinearLayout(application)
        val hPadding = ViewUtil.dpToPx(application, 16f).toInt()
        wrap.setPadding(hPadding, 0, hPadding, 0)
        wrap.addView(textInput)
        textInput.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        getContentWrapper().addView(wrap)
        DialogCommonActions.loadIn(this) { ok, _ ->
            ok.setOnClickListener {
                listener?.onOk(textInput.text)
                destroy()
            }
        }
    }

    fun setText(text: CharSequence) {
        textInput.setText(text)
    }

    fun setHint(text: CharSequence) {
        textInput.hint = text
    }

    fun setHint(id: Int) {
        textInput.setHint(id)
    }

    fun setOnOkClickListener(l: OnOkClickListener) {
        listener = l
    }

    fun interface OnOkClickListener {
        fun onOk(text: CharSequence)
    }
}
