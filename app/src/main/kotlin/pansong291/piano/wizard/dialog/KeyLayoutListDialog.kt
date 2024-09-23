package pansong291.piano.wizard.dialog

import android.app.Application
import pansong291.piano.wizard.dialog.contents.DialogRadioListContent
import pansong291.piano.wizard.entity.KeyLayout

class KeyLayoutListDialog(
    application: Application,
    val data: List<KeyLayout>,
    val default: Int? = null
) : BaseDialog(application) {
    init {
        setOutsideCloseable(true)
        val adapter = DialogRadioListContent.loadIn(this, data.map { it.name }, default)
    }

    fun update(index: Int, keyLayout: KeyLayout) {}

    fun remove(index: Int) {}
}
