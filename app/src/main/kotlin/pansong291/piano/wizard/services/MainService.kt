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
import pansong291.piano.wizard.PlayMusicThreadManager
import pansong291.piano.wizard.R
import pansong291.piano.wizard.ServiceException
import pansong291.piano.wizard.consts.StringConst
import pansong291.piano.wizard.dialog.KeyLayoutListDialog
import pansong291.piano.wizard.dialog.MessageDialog
import pansong291.piano.wizard.dialog.MusicFileChooseDialog
import pansong291.piano.wizard.dialog.TextInputDialog
import pansong291.piano.wizard.entity.KeyLayout
import pansong291.piano.wizard.entity.MusicNotation
import pansong291.piano.wizard.toast.Toaster
import pansong291.piano.wizard.utils.MusicUtil
import pansong291.piano.wizard.views.KeysLayoutView
import java.io.File
import java.io.FileReader

class MainService : Service() {
    private lateinit var sharedPreferences: SharedPreferences

    /**
     * 控制器窗口
     */
    private lateinit var controllerWindow: EasyWindow<*>

    /**
     * 布局窗口
     */
    private lateinit var layoutWindow: EasyWindow<*>

    /**
     * 布局视图
     */
    private lateinit var keysLayoutView: KeysLayoutView

    /**
     * 收起 / 展开 按钮
     */
    private lateinit var btnCollapse: Button

    /**
     * 切换控制器按钮
     */
    private lateinit var btnControllerSwitch: Button

    /**
     * 停止播放按钮
     */
    private lateinit var btnStopMusic: Button

    /**
     * 控制器父容器
     */
    private lateinit var vgControllerWrapper: ViewGroup

    /**
     * 布局控制器父容器
     */
    private lateinit var vgKeyLayoutControllerWrapper: ViewGroup

    /**
     * 乐谱控制器父容器
     */
    private lateinit var vgMusicScoreControllerWrapper: ViewGroup

    /**
     * 选择布局按钮
     */
    private lateinit var btnChooseLayout: Button

    /**
     * 重置指示器按钮
     */
    private lateinit var btnResetIndicator: Button

    /**
     * 显示序号复选框
     */
    private lateinit var cbDisplayNumber: CheckBox

    /**
     * 半音复选框
     */
    private lateinit var cbEnableSemitone: CheckBox

    /**
     * 移除点位按钮
     */
    private lateinit var btnPointRemove: Button

    /**
     * 增加点位按钮
     */
    private lateinit var btnPointAdd: Button

    /**
     * 选择乐谱按钮
     */
    private lateinit var btnChooseMusic: Button

    /**
     * 显示变调按钮
     */
    private lateinit var btnModulation: Button

    /**
     * 变调 -1 按钮
     */
    private lateinit var btnToneMinus1: Button

    /**
     * 变调 +1 按钮
     */
    private lateinit var btnTonePlus1: Button

    /**
     * 变调 -7 按钮
     */
    private lateinit var btnToneMinus7: Button

    /**
     * 变调 -7 按钮
     */
    private lateinit var btnTonePlus7: Button

    /**
     * 开始暂停按钮
     */
    private lateinit var btnPlayPause: Button

    /**
     * 全部布局
     */
    private lateinit var keyLayouts: List<KeyLayout>

    /**
     * 当前布局
     */
    private var currentLayout: KeyLayout? = null

    /**
     * 当前乐谱
     */
    private var currentMusic: MusicNotation? = null

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences(StringConst.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        keyLayouts = Gson().fromJson(
            sharedPreferences.getString(StringConst.SP_DATA_KEY_KEY_LAYOUTS, null),
            object : TypeToken<List<KeyLayout>>() {}.type
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
            // 绑定视图组件
            btnCollapse = contentView.findViewById(R.id.btn_collapse)
            btnControllerSwitch = contentView.findViewById(R.id.btn_controller_switch)
            btnStopMusic = contentView.findViewById(R.id.btn_stop_music)
            vgControllerWrapper = contentView.findViewById(R.id.controller_wrapper)
            vgKeyLayoutControllerWrapper =
                contentView.findViewById(R.id.key_layout_controller_wrapper)
            vgMusicScoreControllerWrapper =
                contentView.findViewById(R.id.music_score_controller_wrapper)
            btnChooseLayout = contentView.findViewById(R.id.btn_choose_key_layout)
            btnResetIndicator = contentView.findViewById(R.id.btn_reset_indicator)
            cbDisplayNumber = contentView.findViewById(R.id.cb_display_number)
            cbEnableSemitone = contentView.findViewById(R.id.cb_enable_semitone)
            btnPointRemove = contentView.findViewById(R.id.btn_point_remove)
            btnPointAdd = contentView.findViewById(R.id.btn_point_add)
            btnChooseMusic = contentView.findViewById(R.id.btn_choose_music)
            btnModulation = contentView.findViewById(R.id.btn_modulation)
            btnToneMinus1 = contentView.findViewById(R.id.btn_tone_minus_1)
            btnTonePlus1 = contentView.findViewById(R.id.btn_tone_plus_1)
            btnToneMinus7 = contentView.findViewById(R.id.btn_tone_minus_7)
            btnTonePlus7 = contentView.findViewById(R.id.btn_tone_plus_7)
            btnPlayPause = contentView.findViewById(R.id.btn_play_pause)
        }

        setupBasicController()
        setupMusicScoreController()
        setupKeysLayoutController()

        // 初始化布局
        sharedPreferences.getInt(StringConst.SP_DATA_KEY_LAST_LAYOUT, 0).takeIf {
            it >= 0 && it < keyLayouts.size
        }?.let { updateCurrentLayout(keyLayouts[it]) }

        layoutWindow = EasyWindow.with(application).apply {
            setAnimStyle(android.R.style.Animation_Dialog)
            setContentView(keysLayoutView)
            setOutsideTouchable(false)
            setWidth(ViewGroup.LayoutParams.MATCH_PARENT)
            setHeight(ViewGroup.LayoutParams.MATCH_PARENT)
            windowVisibility = View.GONE
        }
    }

    private fun setupBasicController() {
        // 初始设置文案
        btnCollapse.setText(R.string.collapse)
        btnControllerSwitch.setText(R.string.key_layout)
        // 初始隐藏音乐停止按钮
        btnStopMusic.visibility = View.GONE
        // 初始隐藏布局控制器
        vgKeyLayoutControllerWrapper.visibility = View.GONE
        // 初始勾选显示序号
        cbDisplayNumber.isChecked = keysLayoutView.isShowNum()
        // 展开、收起 长按
        btnCollapse.setOnLongClickListener { true }
        // 展开、收起
        btnCollapse.setOnClickListener {
            val isShow = btnControllerSwitch.visibility == View.VISIBLE
            val visible = if (isShow) View.GONE else View.VISIBLE
            btnControllerSwitch.visibility = visible
            vgControllerWrapper.visibility = visible
            btnCollapse.text = getString(if (isShow) R.string.expand else R.string.collapse)
        }
        // 布局、乐谱
        btnControllerSwitch.setOnClickListener {
            val isShowKL = vgKeyLayoutControllerWrapper.visibility == View.VISIBLE
            if (isShowKL) {
                vgKeyLayoutControllerWrapper.visibility = View.GONE
                vgMusicScoreControllerWrapper.visibility = View.VISIBLE
                layoutWindow.windowVisibility = View.GONE
                btnControllerSwitch.text = getString(R.string.key_layout)
            } else {
                vgKeyLayoutControllerWrapper.visibility = View.VISIBLE
                vgMusicScoreControllerWrapper.visibility = View.GONE
                layoutWindow.windowVisibility = View.VISIBLE
                btnControllerSwitch.text = getString(R.string.music_score)
                if (keysLayoutView.getIndicator().x == 0 && keysLayoutView.getIndicator().y == 0) {
                    // 重置指示器
                    keysLayoutView.resetIndicator()
                }
            }
        }
    }

    private fun setupMusicScoreController() {
        // 选择乐谱
        btnChooseMusic.setOnClickListener {
            val fcd = MusicFileChooseDialog(application)
            fcd.onFileChose = { path, filename ->
                withCurrentLayout {
                    try {
                        val index = filename.lastIndexOf(StringConst.MUSIC_NOTATION_FILE_EXT)
                        updateCurrentMusic(
                            MusicUtil.parseMusicNotation(
                                if (index > 0) filename.substring(0, index) else filename,
                                FileReader(File(path, filename)).readText()
                            )
                        )
                        fcd.destroy()
                    } catch (e: ServiceException) {
                        Toaster.show(e.getI18NMessage(application))
                    } catch (e: Throwable) {
                        Toaster.show(
                            e.cause?.message ?: e.message ?: getString(R.string.unknown_error)
                        )
                    }
                }
            }
            fcd.show()
        }
        // 显示变调
        btnModulation.setOnClickListener {
            withCurrentMusic { }
        }
        // 变调 -1
        btnToneMinus1.setOnClickListener {
            withCurrentMusic { }
        }
        // 变调 +1
        btnTonePlus1.setOnClickListener {
            withCurrentMusic { }
        }
        // 变调 -7
        btnToneMinus7.setOnClickListener {
            withCurrentMusic { }
        }
        // 变调 +7
        btnTonePlus7.setOnClickListener {
            withCurrentMusic { }
        }
        // 开始暂停
        btnPlayPause.setOnClickListener {
            withCurrentMusic {
                PlayMusicThreadManager.startMusic(it, currentLayout!!, 0)
            }
        }
        PlayMusicThreadManager.onStopped = {
            // TODO
        }
    }

    private fun setupKeysLayoutController() {
        // 选择布局
        btnChooseLayout.setOnClickListener {
            val klld = KeyLayoutListDialog(
                application,
                keyLayouts,
                keyLayouts.indexOf(currentLayout)
            )
            klld.onAction = onAction@{ index, actionId ->
                // 保存
                if (actionId == R.id.btn_save) {
                    sharedPreferences.edit().putString(
                        StringConst.SP_DATA_KEY_KEY_LAYOUTS,
                        Gson().toJson(keyLayouts)
                    ).apply()
                    Toaster.show(R.string.save_all_layouts_message)
                }
                // 新建布局
                else if (actionId == R.id.btn_create) {
                    val tid = TextInputDialog(application)
                    tid.setIcon(R.drawable.outline_add_32)
                    tid.setTitle(R.string.create)
                    tid.setHint(R.string.enter_layout_name_hint)
                    tid.onTextConfirmed = onTextConfirmed@{
                        if (it.isEmpty()) {
                            Toaster.show(R.string.require_name_message)
                            return@onTextConfirmed
                        }
                        val kl = KeyLayout()
                        kl.name = it.toString()
                        keyLayouts += kl
                        klld.reloadData(keyLayouts, null)
                        tid.destroy()
                    }
                    tid.show()
                    return@onAction
                }
                val kl = if (index >= 0) keyLayouts[index]
                else return@onAction
                when (actionId) {
                    // 删除所选布局
                    R.id.btn_delete -> {
                        val md = MessageDialog(application)
                        md.setIcon(R.drawable.outline_delete_forever_32)
                        md.setTitle(R.string.delete)
                        md.setText(getString(R.string.delete_confirm_message, kl.name))
                        md.onOkClick = {
                            keyLayouts = keyLayouts.filter { it != kl }
                            // 清空所选项
                            klld.reloadData(keyLayouts, -1)
                            // 如果所选布局是当前布局
                            if (currentLayout == kl) {
                                updateCurrentLayout(null)
                                sharedPreferences.edit()
                                    .putInt(StringConst.SP_DATA_KEY_LAST_LAYOUT, 0).apply()
                            }
                            md.destroy()
                        }
                        md.show()
                    }

                    // 重命名所选布局
                    R.id.btn_rename -> {
                        val tid = TextInputDialog(application)
                        tid.setIcon(R.drawable.outline_drive_file_rename_outline_32)
                        tid.setTitle(R.string.rename)
                        tid.setHint(R.string.enter_layout_name_hint)
                        tid.setText(kl.name)
                        tid.onTextConfirmed = onTextConfirmed@{
                            if (it.isEmpty()) {
                                Toaster.show(R.string.require_name_message)
                                return@onTextConfirmed
                            }
                            kl.name = it.toString()
                            klld.reloadData(keyLayouts, null)
                            // 如果所选布局是当前布局，则更新按钮文案
                            if (currentLayout == kl) btnChooseLayout.text = kl.name
                            tid.destroy()
                        }
                        tid.show()
                    }

                    // 将所选布局设为当前布局
                    android.R.id.primary -> {
                        updateCurrentLayout(kl)
                        sharedPreferences.edit()
                            .putInt(StringConst.SP_DATA_KEY_LAST_LAYOUT, index).apply()
                        klld.destroy()
                    }
                }
            }
            klld.show()
        }
        // 重置指示器
        btnResetIndicator.setOnClickListener {
            keysLayoutView.resetIndicator()
        }
        // 显示序号
        cbDisplayNumber.setOnCheckedChangeListener { _, isChecked ->
            keysLayoutView.setShowNum(isChecked)
        }
        // 启用半音
        cbEnableSemitone.setOnCheckedChangeListener { _, isChecked ->
            withCurrentLayout {
                keysLayoutView.setSemitone(isChecked)
                it.semitone = isChecked
            }
        }
        // 移除点位
        btnPointRemove.setOnClickListener {
            withCurrentLayout {
                if (it.points.isEmpty()) return@withCurrentLayout
                val mPoints = it.points.toMutableList()
                val last = mPoints.removeLast()
                it.points = mPoints
                keysLayoutView.setIndicator(last)
                keysLayoutView.setPoints(mPoints)
            }
        }
        // 增加点位
        btnPointAdd.setOnClickListener {
            withCurrentLayout {
                it.points += Point(keysLayoutView.getIndicator())
                it.rawOffset.set(keysLayoutView.rawOffset)
                keysLayoutView.setPoints(it.points)
            }
        }
    }

    private fun updateCurrentLayout(kl: KeyLayout?) {
        currentLayout = kl
        kl?.also {
            btnChooseLayout.text = it.name
            cbEnableSemitone.isChecked = it.semitone
            keysLayoutView.setPoints(it.points)
            keysLayoutView.setSemitone(it.semitone)
        } ?: run {
            btnChooseLayout.setText(R.string.select_layout)
            keysLayoutView.setPoints(emptyList())
            updateCurrentMusic(null)
        }
    }

    private fun withCurrentLayout(block: (c: KeyLayout) -> Unit) {
        currentLayout?.also(block) ?: Toaster.show(R.string.layout_empty_warn_message)
    }

    private fun updateCurrentMusic(mn: MusicNotation?) {
        currentMusic = mn
        mn?.also { } ?: run {}
        // TODO
    }

    private fun withCurrentMusic(block: (c: MusicNotation) -> Unit) {
        currentMusic?.also(block) ?: Toaster.show(R.string.music_empty_warn_message)
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
