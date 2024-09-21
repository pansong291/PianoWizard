package pansong291.piano.wizard

import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.hjq.toast.Toaster
import com.hjq.window.EasyWindow
import com.hjq.window.draggable.SpringBackDraggable

class MainActivity : AppCompatActivity() {
    private lateinit var btnFilePerm: Button
    private lateinit var btnShowWin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        btnFilePerm = findViewById(R.id.btn_main_file_perm)
        btnShowWin = findViewById(R.id.btn_main_show_win)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btnFilePerm.setOnClickListener {
            XXPermissions.with(this)
                .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .request(object : OnPermissionCallback {
                    override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                        Toaster.show("获取文件读写权限成功")
                    }

                    override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                        Toaster.show("获取文件读写权限失败")
                    }
                })
        }
        btnShowWin.setOnClickListener {
            EasyWindow.with(this).apply {
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
                    EasyWindow.OnClickListener<ImageView?> { easyWindow: EasyWindow<*>, view: ImageView? ->
                        // 点击这个 View 后消失
                        easyWindow.cancel()
                        // 跳转到某个Activity
                        // easyWindow.startActivity(intent);
                    })
            }.show()
        }
    }
}
