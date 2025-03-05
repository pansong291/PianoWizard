package pansong291.piano.wizard.utils

import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.widget.EditText

object ViewUtil {
    private val dp1 by lazy {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            1f,
            Resources.getSystem().displayMetrics
        )
    }
    private val sp1 by lazy {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            1f,
            Resources.getSystem().displayMetrics
        )
    }
    private var runnable: Runnable? = null

    /**
     * 将 dp 单位转换为 px 单位
     */
    fun Float.dp() = this * dp1

    /**
     * 将 sp 单位转换为 px 单位
     */
    fun Float.sp() = this * sp1

    fun Int.dp() = this.toFloat().dp()

    fun Int.sp() = this.toFloat().sp()

    fun Float.dpInt() = this.dp().toInt()

    fun Float.spInt() = this.sp().toInt()

    fun Int.dpInt() = this.dp().toInt()

    fun Int.spInt() = this.sp().toInt()

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
                runnable?.also { editText.removeCallbacks(it) }
                Runnable { consumer(s) }.also {
                    runnable = it
                    editText.postDelayed(it, interval)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }
        editText.addTextChangedListener(watcher)
    }
}
