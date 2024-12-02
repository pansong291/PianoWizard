package pansong291.piano.wizard.dialog

import android.content.Context
import pansong291.piano.wizard.R
import pansong291.piano.wizard.dialog.base.BaseDialog
import pansong291.piano.wizard.dialog.contents.DialogProgressContent

class ProgressDialog(context: Context) : BaseDialog(context) {
    private val progressBar = DialogProgressContent.loadIn(this)

    init {
        setTitle(R.string.processing)
        setMaskCloseable(false)
    }

    fun updateProgress(current: Int, total: Int) {
        progressBar.max = total
        progressBar.progress = current
    }
}
