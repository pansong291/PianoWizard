package pansong291.piano.wizard.dialog

import android.content.Context
import pansong291.piano.wizard.R
import pansong291.piano.wizard.dialog.base.BaseDialog
import pansong291.piano.wizard.dialog.contents.DialogSpinnerContent

class LoadingDialog(context: Context) : BaseDialog(context) {
    init {
        DialogSpinnerContent.loadIn(this)
        setTitle(R.string.processing)
        setMaskCloseable(false)
    }
}
