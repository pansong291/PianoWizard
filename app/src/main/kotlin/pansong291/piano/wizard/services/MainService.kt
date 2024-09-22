package pansong291.piano.wizard.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import com.hjq.window.EasyWindow
import com.hjq.window.draggable.SpringBackDraggable
import pansong291.piano.wizard.R

class MainService : Service() {
    private lateinit var controllerWindow: EasyWindow<*>
    private lateinit var layoutWindow: EasyWindow<*>

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        controllerWindow = EasyWindow.with(application).apply {
            setContentView(R.layout.win_sample)
            // 设置成可拖拽的
            setDraggable(SpringBackDraggable())
            setGravity(Gravity.CENTER_VERTICAL or Gravity.START)
            // 设置动画样式
            setAnimStyle(android.R.style.Animation_Translucent)
            // 设置外层是否能被触摸
            // setOutsideTouchable(false)
            // 设置窗口背景阴影强度
            setBackgroundDimAmount(0.5f)
            setImageDrawable(android.R.id.icon, R.mipmap.ic_launcher)
            setOnClickListener(
                android.R.id.icon,
                EasyWindow.OnClickListener { easyWindow, view: ImageView? ->
                    stopSelf()
                })
        }
        layoutWindow = EasyWindow.with(application).apply {
            setWidth(ViewGroup.LayoutParams.MATCH_PARENT)
            setHeight(ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        controllerWindow.show()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        controllerWindow.recycle()
        super.onDestroy()
    }
}
