package pansong291.piano.wizard.dialog

import android.app.Application
import android.view.Gravity
import android.view.ViewGroup
import com.hjq.window.EasyWindow
import pansong291.piano.wizard.R

abstract class BaseDialog(application: Application) {
    protected var dialog: EasyWindow<*> = EasyWindow.with(application).apply {
        // 设置宽高
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT)
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT)
        setGravity(Gravity.CENTER)
        // 基本视图
        setContentView(R.layout.dialog_base)
    }

    fun show() {
        dialog.show()
    }
}
