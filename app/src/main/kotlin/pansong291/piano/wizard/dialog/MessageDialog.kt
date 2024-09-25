package pansong291.piano.wizard.dialog

import android.app.Application
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import pansong291.piano.wizard.dialog.actions.DialogCommonActions
import pansong291.piano.wizard.utils.ViewUtil

class MessageDialog(application: Application) : BaseDialog(application) {
    private val textView = TextView(application)
    var onOkClick: () -> Unit = {}

    init {
        val scrollView = ScrollView(application)
        scrollView.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val horizontalPadding = ViewUtil.dpToPx(application, 16f).toInt()
        scrollView.setPadding(horizontalPadding, horizontalPadding, horizontalPadding, 0)
        textView.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        scrollView.addView(textView)
        findContentWrapper().addView(scrollView)
        DialogCommonActions.loadIn(this) { ok, _ ->
            ok.setOnClickListener {
                onOkClick.invoke()
            }
        }
    }

    fun setText(id: Int) {
        textView.setText(id)
    }

    fun setText(text: CharSequence) {
        textView.text = text
    }
}
