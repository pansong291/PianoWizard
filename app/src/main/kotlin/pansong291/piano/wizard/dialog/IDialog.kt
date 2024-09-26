package pansong291.piano.wizard.dialog

import android.app.Application
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

interface IDialog {
    fun getAppContext(): Application

    fun findContentWrapper(): LinearLayout

    fun findActionsWrapper(): LinearLayout

    fun setMaskCloseable(b: Boolean)

    fun setTitle(text: CharSequence)

    fun setTitle(@StringRes id: Int)

    fun setIcon(@DrawableRes id: Int)

    fun show()

    fun destroy()
}
