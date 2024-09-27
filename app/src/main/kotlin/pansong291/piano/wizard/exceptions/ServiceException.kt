package pansong291.piano.wizard.exceptions

import android.content.Context
import androidx.annotation.StringRes

class ServiceException(
    @StringRes private val id: Int,
    private vararg val args: Any
) : RuntimeException() {
    fun getI18NMessage(context: Context): String {
        return context.getString(id, *args)
    }
}
