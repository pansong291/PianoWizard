package pansong291.piano.wizard.dialog

import android.app.Application
import android.widget.LinearLayout

interface IDialog {
    fun getAppContext(): Application

    fun getMainContent(): LinearLayout

    fun getActions(): LinearLayout

    fun setOutsideCloseable(b: Boolean)

    fun setTitle(text: CharSequence)

    fun setText(id: Int)

    fun setIcon(id: Int)

    fun show()

    fun destroy()
}
