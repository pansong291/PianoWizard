package pansong291.piano.wizard

import android.content.Context
import android.util.TypedValue


object ViewUtil {
    /**
     * 将 dp 单位转换为 px 单位
     *
     * @param context 上下文，用于获取资源和设备显示信息
     * @param dpValue 要转换的 dp 值
     * @return 转换后的 px 值
     */
    fun dpToPx(context: Context, dpValue: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpValue,
            context.resources.displayMetrics
        )
    }

    /**
     * 将 sp 单位转换为 px 单位
     *
     * @param context 上下文，用于获取资源和设备显示信息
     * @param spValue 要转换的 sp 值
     * @return 转换后的 px 值
     */
    fun spToPx(context: Context, spValue: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            spValue,
            context.resources.displayMetrics
        )
    }
}
