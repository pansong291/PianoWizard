package pansong291.piano.wizard.dialog

import android.content.Context
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.StringRes
import pansong291.piano.wizard.dialog.actions.DialogConfirmActions
import pansong291.piano.wizard.dialog.base.BaseDialog
import pansong291.piano.wizard.utils.ViewUtil

class TextInputDialog(context: Context) : BaseDialog(context) {
    private val textInput = EditText(context)
    var onTextConfirmed: ((t: CharSequence) -> Unit)? = null

    init {
        dialog.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        val horizontalMargin = ViewUtil.dpToPx(context, 16f).toInt()
        textInput.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            marginStart = horizontalMargin
            marginEnd = horizontalMargin
        }
        findContentWrapper().addView(textInput)
        DialogConfirmActions.loadIn(this) { ok, _ ->
            ok.setOnClickListener {
                onTextConfirmed?.invoke(textInput.text ?: "")
            }
        }
    }

    fun setText(text: CharSequence) {
        textInput.setText(text)
    }

    fun setHint(text: CharSequence) {
        textInput.hint = text
    }

    fun setHint(@StringRes id: Int) {
        textInput.setHint(id)
    }
}
