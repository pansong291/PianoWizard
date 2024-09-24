package pansong291.piano.wizard.dialog

import android.app.Application
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.hjq.window.EasyWindow
import pansong291.piano.wizard.R

abstract class BaseDialog(val application: Application) : IDialog {
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
    }

    init {
        // 点击遮罩关闭对话框
        setOutsideCloseable(true)
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

    final override fun setOutsideCloseable(b: Boolean) {
        if (!b) return
        dialog.contentView.findViewById<ViewGroup>(android.R.id.mask).setOnClickListener {
            destroy()
        }
    }

    final override fun setTitle(text: CharSequence) {
        dialog.setText(android.R.id.title, text)
    }

    final override fun setTitle(id: Int) {
        dialog.setText(android.R.id.title, id)
    }

    final override fun setIcon(id: Int) {
        dialog.findViewById<TextView>(android.R.id.title)
            .setCompoundDrawablesRelativeWithIntrinsicBounds(id, 0, 0, 0)
    }

    final override fun show() {
        dialog.show()
    }

    final override fun destroy() {
        dialog.recycle()
    }
}
