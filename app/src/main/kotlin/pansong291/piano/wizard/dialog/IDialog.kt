package pansong291.piano.wizard.dialog

import android.app.Application
import android.widget.LinearLayout

interface IDialog {
    fun getAppContext(): Application

    fun findContentWrapper(): LinearLayout

    fun findActionsWrapper(): LinearLayout

    fun setMaskCloseable(b: Boolean)

    fun setTitle(text: CharSequence)

    fun setTitle(id: Int)

    fun setIcon(id: Int)

    fun show()

    fun destroy()
}
