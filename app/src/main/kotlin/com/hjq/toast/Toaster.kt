package com.hjq.toast

import android.app.Application
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.annotation.StringRes
import com.hjq.toast.config.IToast
import java.lang.ref.WeakReference

/**
 * [getActivity/Toaster](https://github.com/getActivity/Toaster)
 */
object Toaster {
    const val LENGTH_LONG = 3000
    const val LENGTH_SHORT = 2000
    private lateinit var sApplication: Application
    private val mShowMessageToken = Any()
    private val HANDLER = Handler(Looper.getMainLooper())
    private var mToastReference: WeakReference<IToast>? = null

    fun initialize(application: Application) {
        sApplication = application
    }

    /**
     * 显示 Toast
     */
    fun show(@StringRes id: Int, duration: Int = LENGTH_SHORT) {
        show(stringIdToCharSequence(id), duration)
    }

    fun show(text: CharSequence, duration: Int = LENGTH_SHORT) {
        // 移除之前未显示的 Toast 消息
        HANDLER.removeCallbacksAndMessages(mShowMessageToken)
        HANDLER.postAtTime({
            // 取消上一个 Toast 的显示，避免出现重叠的效果
            mToastReference?.get()?.cancel()
            val toast = GlobalToast(sApplication)

            // 为什么用 WeakReference，而不用 SoftReference ？
            // https://github.com/getActivity/Toaster/issues/79
            mToastReference = WeakReference(toast)
            toast.setText(text)
            toast.setDuration(duration)
            toast.show()
        }, mShowMessageToken, SystemClock.uptimeMillis())
    }

    private fun stringIdToCharSequence(id: Int): CharSequence {
        return try {
            // 如果这是一个资源 id
            sApplication.resources.getText(id)
        } catch (ignored: Resources.NotFoundException) {
            // 如果这是一个 int 整数
            id.toString()
        }
    }
}
