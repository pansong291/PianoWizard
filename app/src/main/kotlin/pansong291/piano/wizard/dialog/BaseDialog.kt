package pansong291.piano.wizard.dialog

import android.app.Application
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.hjq.window.EasyWindow
import pansong291.piano.wizard.R

abstract class BaseDialog(val application: Application) : IDialog {
    private var maskCloseable = true
    protected var dialog: EasyWindow<*> = EasyWindow.with(application).apply {
        setAnimStyle(android.R.style.Animation_Dialog)
        // 设置外层是否能被触摸
        setOutsideTouchable(false)
        // 设置窗口背景阴影强度
        setBackgroundDimAmount(.6f)
        // 设置宽高
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT)
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT)
        setGravity(Gravity.CENTER)
        // 基本视图
        setContentView(R.layout.dialog_base)
        // 点击遮罩关闭对话框
        contentView.findViewById<ViewGroup>(android.R.id.mask).setOnClickListener {
            if (maskCloseable) destroy()
        }
    }

    final override fun getAppContext(): Application {
        return application
    }

    final override fun findContentWrapper(): LinearLayout {
        return dialog.contentView.findViewById(android.R.id.content)
    }

    final override fun findActionsWrapper(): LinearLayout {
        return dialog.contentView.findViewById(android.R.id.extractArea)
    }

    final override fun setMaskCloseable(b: Boolean) {
        maskCloseable = b
    }

    final override fun setTitle(text: CharSequence) {
        dialog.setText(android.R.id.title, text)
    }

    final override fun setTitle(@StringRes id: Int) {
        dialog.setText(android.R.id.title, id)
    }

    final override fun setIcon(@DrawableRes id: Int) {
        dialog.findViewById<TextView>(android.R.id.title)
            .setCompoundDrawablesRelativeWithIntrinsicBounds(id, 0, 0, 0)
    }

    override fun show() {
        dialog.show()
    }

    override fun destroy() {
        dialog.recycle()
    }
}
