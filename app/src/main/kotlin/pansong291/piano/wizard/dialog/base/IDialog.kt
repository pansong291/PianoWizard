package pansong291.piano.wizard.dialog.base

import android.content.Context
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView

interface IDialog {
    fun getContext(): Context

    fun findTitle(): AppCompatTextView

    fun findContentWrapper(): LinearLayout

    fun findActionsWrapper(): LinearLayout

    fun setMaskCloseable(b: Boolean)

    fun setTitle(text: CharSequence)

    fun setTitle(@StringRes id: Int)

    fun setIcon(@DrawableRes id: Int)

    fun show()

    fun destroy()
}
