package pansong291.piano.wizard.utils

import android.content.res.Resources
import android.util.TypedValue

object ViewUtil {
    /**
     * 将 dp 单位转换为 px 单位
     */
    fun Float.dp() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

    /**
     * 将 sp 单位转换为 px 单位
     */
    fun Float.sp() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Resources.getSystem().displayMetrics
    )

    fun Int.dp() = this.toFloat().dp()

    fun Int.sp() = this.toFloat().sp()

    fun Float.dpInt() = this.dp().toInt()

    fun Float.spInt() = this.sp().toInt()

    fun Int.dpInt() = this.dp().toInt()

    fun Int.spInt() = this.sp().toInt()
}
