package pansong291.piano.wizard.dialog.contents

import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import pansong291.piano.wizard.dialog.base.IDialog

object DialogProgressContent {
    fun loadIn(dialog: IDialog): ProgressBar {
        val context = dialog.getContext()
        val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
        progressBar.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        progressBar.max = 100
        progressBar.progress = 0
        dialog.findContentWrapper().addView(progressBar)
        return progressBar
    }
}
