package pansong291.piano.wizard.dialog

import android.app.Application
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import pansong291.piano.wizard.ViewUtil
import pansong291.piano.wizard.dialog.actions.DialogCommonActions

class MessageDialog(application: Application) : BaseDialog(application) {
    private val textView = TextView(application)
    var onOkClick: OnOkClickListener? = null

    init {
        val scrollView = ScrollView(application)
        scrollView.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val horizontalPadding = ViewUtil.dpToPx(application, 16f).toInt()
        scrollView.setPadding(horizontalPadding, 0, horizontalPadding, 0)
        textView.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        scrollView.addView(textView)
        findContentWrapper().addView(scrollView)
        DialogCommonActions.loadIn(this) { ok, _ ->
            ok.setOnClickListener {
                onOkClick?.onOkClick()
            }
        }
    }

    fun setText(id: Int) {
        textView.setText(id)
    }

    fun setText(text: CharSequence) {
        textView.text = text
    }

    fun interface OnOkClickListener {
        fun onOkClick()
    }
}
