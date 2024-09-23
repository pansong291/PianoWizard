package pansong291.piano.wizard.services

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Point
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hjq.window.EasyWindow
import com.hjq.window.draggable.SpringBackDraggable
import pansong291.piano.wizard.R
import pansong291.piano.wizard.consts.StringConst
import pansong291.piano.wizard.dialog.FileChooseDialog
import pansong291.piano.wizard.dialog.KeyLayoutListDialog
import pansong291.piano.wizard.dialog.TextInputDialog
import pansong291.piano.wizard.entity.KeyLayout
import pansong291.piano.wizard.toast.Toaster
import pansong291.piano.wizard.views.KeysLayoutView
import java.io.FileFilter

class MainService : Service() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var controllerWindow: EasyWindow<*>
    private lateinit var layoutWindow: EasyWindow<*>
    private lateinit var keysLayoutView: KeysLayoutView
    private lateinit var keyLayouts: List<KeyLayout>
    private var currentLayout: KeyLayout? = null

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences(StringConst.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        keyLayouts = Gson().fromJson(
            sharedPreferences.getString(StringConst.SP_DATA_KEY_KEY_LAYOUTS, null),
            TypeToken.getArray(KeyLayout::class.java).type
        ) ?: emptyList()
        keysLayoutView = KeysLayoutView(application)
        controllerWindow = EasyWindow.with(application).apply {
            setContentView(R.layout.win_controller)
            // 设置成可拖拽的
            setDraggable(SpringBackDraggable())
            // 设置靠左上
            setGravity(Gravity.TOP or Gravity.START)
            // 设置动画样式
            setAnimStyle(android.R.style.Animation_Translucent)
            // 初始设置文案
            setText(R.id.btn_collapse, R.string.collapse)
            setText(R.id.btn_controller_switch, R.string.key_layout)
            // 初始隐藏布局控制器
            setVisibility(R.id.key_layout_controller_wrapper, View.GONE)
            // 初始勾选显示序号
            (findViewById<CheckBox>(R.id.cb_display_number) as CheckBox).isChecked = true
            // 展开、收起 长按
            setOnLongClickListener(
                R.id.btn_collapse,
                EasyWindow.OnLongClickListener { _, view: Button ->
                    true
                })
            // 展开、收起
            setOnClickListener(
                R.id.btn_collapse,
                EasyWindow.OnClickListener { _, view: Button ->
                    val cs = findViewById<View>(R.id.btn_controller_switch)
                    val cw = findViewById<View>(R.id.controller_wrapper)
                    val isShow = cs.visibility == View.VISIBLE
                    val visible = if (isShow) View.GONE else View.VISIBLE
                    cs.visibility = visible
                    cw.visibility = visible
                    view.text = getString(if (isShow) R.string.expand else R.string.collapse)
                }
            )
            // 布局、乐谱
            setOnClickListener(
                R.id.btn_controller_switch,
                EasyWindow.OnClickListener { _, view: Button ->
                    val kl = findViewById<View>(R.id.key_layout_controller_wrapper)
                    val ms = findViewById<View>(R.id.music_score_controller_wrapper)
                    val isShowKL = kl.visibility == View.VISIBLE
                    if (isShowKL) {
                        kl.visibility = View.GONE
                        ms.visibility = View.VISIBLE
                        layoutWindow.windowVisibility = View.GONE
                        view.text = getString(R.string.key_layout)
                    } else {
                        kl.visibility = View.VISIBLE
                        ms.visibility = View.GONE
                        layoutWindow.windowVisibility = View.VISIBLE
                        view.text = getString(R.string.music_score)
                    }
                }
            )
        }
        setupMusicScoreController()
        setupKeysLayoutController()
        layoutWindow = EasyWindow.with(application).apply {
            setContentView(keysLayoutView)
            setOutsideTouchable(false)
            setWidth(ViewGroup.LayoutParams.MATCH_PARENT)
            setHeight(ViewGroup.LayoutParams.MATCH_PARENT)
            windowVisibility = View.GONE
        }
    }

    private fun setupMusicScoreController() {
        controllerWindow.apply {
            // 选择乐谱
            setOnClickListener(
                R.id.btn_choose_music,
                EasyWindow.OnClickListener { _, _: Button ->
                    FileChooseDialog(application).apply {
                        fileFilter = FileFilter {
                            it.isDirectory || it.name.endsWith(StringConst.MUSIC_NOTATION_FILE_EXT)
                        }
                        show()
                    }
                }
            )
            // TODO
        }
    }

    private fun setupKeysLayoutController() {
        controllerWindow.apply {
            // 选择布局
            setOnClickListener(
                R.id.btn_choose_key_layout,
                EasyWindow.OnClickListener { _, _: Button ->
                    KeyLayoutListDialog(
                        application,
                        keyLayouts,
                        keyLayouts.indexOf(currentLayout)
                    ).apply {
                        setOnActionListener { index, actionId ->
                            when (actionId) {
                                R.id.btn_create -> {
                                    TextInputDialog(application).apply {
                                        setOnOkClickListener {
                                            if (it.isEmpty()) return@setOnOkClickListener
                                            KeyLayout().apply {
                                                name = it.toString()
                                                currentLayout = this
                                                keyLayouts += this
                                                reloadData(keyLayouts, keyLayouts.size - 1)
                                            }
                                        }
                                    }.show()
                                }

                                R.id.btn_delete -> {}
                                R.id.btn_rename -> {}
                                android.R.id.primary -> {
                                    // TODO 显示名称
                                    currentLayout = keyLayouts[index]
                                    destroy()
                                }
                            }
                        }
                    }.show()
                }
            )
            // 重置指示器
            setOnClickListener(
                R.id.btn_reset_indicator,
                EasyWindow.OnClickListener { _, _: Button ->
                    keysLayoutView.resetIndicator()
                }
            )
            // 显示序号
            setOnClickListener(
                R.id.cb_display_number,
                EasyWindow.OnClickListener { _, view: CheckBox ->
                    val showNum = !keysLayoutView.isShowNum()
                    keysLayoutView.setShowNum(showNum)
                    view.isChecked = showNum
                }
            )
            // 启用半音
            setOnClickListener(
                R.id.cb_enable_semitone,
                EasyWindow.OnClickListener { _, view: CheckBox ->
                    withCurrentLayout {
                        it.semitone = view.isChecked
                    }
                }
            )
            // 移除点位
            setOnClickListener(
                R.id.btn_point_remove,
                EasyWindow.OnClickListener { _, _: Button ->
                    withCurrentLayout {
                        if (it.points.isEmpty()) return@withCurrentLayout
                        val mPoints = it.points.toMutableList()
                        val last = mPoints.removeLast()
                        it.points = mPoints
                        keysLayoutView.setIndicator(last)
                        keysLayoutView.setPoints(mPoints)
                    }
                }
            )
            // 增加点位
            setOnClickListener(
                R.id.btn_point_add,
                EasyWindow.OnClickListener { _, _: Button ->
                    withCurrentLayout {
                        val mPoints = it.points.toMutableList()
                        mPoints.add(Point(keysLayoutView.getIndicator()))
                        it.points = mPoints
                        keysLayoutView.setPoints(mPoints)
                    }
                }
            )
        }
    }

    private fun withCurrentLayout(block: (c: KeyLayout) -> Unit) {
        currentLayout?.also(block) ?: Toaster.show(R.string.layout_empty_warn)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        layoutWindow.show()
        controllerWindow.show()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        controllerWindow.recycle()
        layoutWindow.recycle()
        super.onDestroy()
    }
}
