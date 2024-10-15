package pansong291.piano.wizard.toast

import android.app.Application
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.BadTokenException
import pansong291.piano.wizard.toast.config.IToast
import pansong291.piano.wizard.toast.style.BlackToastStyle

class GlobalToast(private val application: Application) : IToast {
    companion object {
        val HANDLER = Handler(Looper.getMainLooper())
    }

    /** 当前是否已经显示  */
    private var isShow = false

    /** Toast 显示时长  */
    private var mDuration = 2000

    private val toastView: View = BlackToastStyle.createView(application)

    private val mShowRunnable = Runnable {
        val windowManager = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
            ?: return@Runnable

        val params = WindowManager.LayoutParams()
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        params.width = WindowManager.LayoutParams.WRAP_CONTENT
        params.format = PixelFormat.TRANSLUCENT
        params.flags = (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        params.windowAnimations = android.R.style.Animation_Toast

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }

        try {
            windowManager.addView(toastView, params)
            // 添加一个移除吐司的任务
            HANDLER.postDelayed({ cancel() }, mDuration.toLong())
            // 当前已经显示
            isShow = true
        } catch (e: IllegalStateException) {
            // 如果这个 View 对象被重复添加到 WindowManager 则会抛出异常
            // java.lang.IllegalStateException: View android.widget.TextView has already been added to the window manager.
            // 如果 WindowManager 绑定的 Activity 已经销毁，则会抛出异常
            // android.view.WindowManager$BadTokenException: Unable to add window -- token android.os.BinderProxy@ef1ccb6 is not valid; is your activity running?
            e.printStackTrace()
        } catch (e: BadTokenException) {
            e.printStackTrace()
        }
    }

    private val mCancelRunnable = Runnable {
        val windowManager = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
            ?: return@Runnable
        try {
            windowManager.removeViewImmediate(toastView)
        } catch (e: IllegalArgumentException) {
            // 如果当前 WindowManager 没有附加这个 View 则会抛出异常
            // java.lang.IllegalArgumentException: View=android.widget.TextView not attached to window manager
            e.printStackTrace()
        } finally {
            // 当前没有显示
            isShow = false
        }
    }

    override fun setText(text: CharSequence?) {
        findMessageView(toastView).text = text
    }

    override fun setDuration(duration: Int) {
        mDuration = duration
    }

    override fun getDuration(): Int {
        return mDuration
    }

    /**
     * 显示吐司弹窗
     */
    override fun show() {
        if (isShow) {
            return
        }
        if (isMainThread()) {
            mShowRunnable.run()
        } else {
            HANDLER.removeCallbacks(mShowRunnable)
            HANDLER.post(mShowRunnable)
        }
    }

    /**
     * 取消吐司弹窗
     */
    override fun cancel() {
        if (!isShow) {
            return
        }
        HANDLER.removeCallbacks(mShowRunnable)
        if (isMainThread()) {
            mCancelRunnable.run()
        } else {
            HANDLER.removeCallbacks(mCancelRunnable)
            HANDLER.post(mCancelRunnable)
        }
    }

    /**
     * 判断当前是否在主线程
     */
    private fun isMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }
}
