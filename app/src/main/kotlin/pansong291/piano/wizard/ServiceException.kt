package pansong291.piano.wizard

import android.content.Context
import androidx.annotation.StringRes

class ServiceException(@StringRes val id: Int, vararg args: Any) : RuntimeException() {
    private val args: Array<out Any> = args

    fun getI18NMessage(context: Context): String {
        return context.getString(id, args)
    }
}
