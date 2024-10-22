package pansong291.piano.wizard.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.widget.EditText

object ViewUtil {
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

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

    /**
     * 输入框防抖
     */
    fun debounceInputChanges(
        editText: EditText,
        interval: Long,
        consumer: (CharSequence?) -> Unit
    ) {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                runnable?.also { handler.removeCallbacks(it) }
                Runnable { consumer(s) }.also {
                    runnable = it
                    handler.postDelayed(it, interval)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }
        editText.addTextChangedListener(watcher)
    }
}
