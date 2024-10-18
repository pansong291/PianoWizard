package pansong291.piano.wizard.dialog.contents

import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import pansong291.piano.wizard.dialog.base.IDialog

object DialogSpinnerContent {
    fun loadIn(dialog: IDialog): ProgressBar {
        val context = dialog.getContext()
        val progressBar = ProgressBar(context)
        progressBar.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.findContentWrapper().addView(progressBar)
        return progressBar
    }
}
