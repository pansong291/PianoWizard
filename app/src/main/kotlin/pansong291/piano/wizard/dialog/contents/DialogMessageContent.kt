package pansong291.piano.wizard.dialog.contents

import android.util.TypedValue
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import pansong291.piano.wizard.dialog.IDialog
import pansong291.piano.wizard.utils.ViewUtil

object DialogMessageContent {
    fun loadIn(dialog: IDialog): TextView {
        val textView = TextView(dialog.getAppContext())
        val scrollView = ScrollView(dialog.getAppContext())
        scrollView.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val horizontalPadding = ViewUtil.dpToPx(dialog.getAppContext(), 16f).toInt()
        scrollView.setPadding(horizontalPadding, horizontalPadding, horizontalPadding, 0)
        textView.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        scrollView.addView(textView)
        dialog.findContentWrapper().addView(scrollView)
        return textView
    }
}
