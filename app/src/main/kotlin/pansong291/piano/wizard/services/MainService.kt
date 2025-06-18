package pansong291.piano.wizard.services

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Point
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import com.hjq.gson.factory.GsonFactory
import com.hjq.toast.Toaster
import com.hjq.window.EasyWindow
import com.hjq.window.draggable.SpringBackDraggable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import pansong291.piano.wizard.R
import pansong291.piano.wizard.consts.StringConst
import pansong291.piano.wizard.consts.TypeConst
import pansong291.piano.wizard.coroutine.MusicPlayer
import pansong291.piano.wizard.dialog.ConfirmDialog
import pansong291.piano.wizard.dialog.KeyLayoutListDialog
import pansong291.piano.wizard.dialog.MessageDialog
import pansong291.piano.wizard.dialog.MusicFileChooseDialog
import pansong291.piano.wizard.dialog.MusicFolderChooseDialog
import pansong291.piano.wizard.dialog.MusicPlayingSettingsDialog
import pansong291.piano.wizard.dialog.TextInputDialog
import pansong291.piano.wizard.entity.KeyLayout
import pansong291.piano.wizard.entity.MusicNotation
import pansong291.piano.wizard.entity.MusicPlayingSettings
import pansong291.piano.wizard.entity.PlayMode
import pansong291.piano.wizard.exceptions.MissingKeyException
import pansong291.piano.wizard.exceptions.ServiceException
import pansong291.piano.wizard.utils.FileUtil
import pansong291.piano.wizard.utils.LangUtil
import pansong291.piano.wizard.utils.MusicUtil
import pansong291.piano.wizard.views.KeysLayoutView
import java.io.File

class MainService : Service() {
    private lateinit var sharedPreferences: SharedPreferences

    /** 控制器窗口 */
    private lateinit var controllerWindow: EasyWindow<*>

    /** 布局窗口 */
    private lateinit var layoutWindow: EasyWindow<*>

    /** 布局视图 */
    private lateinit var keysLayoutView: KeysLayoutView

    /** 收起 / 展开 按钮 */
    private lateinit var btnCollapse: Button

    /** 切换控制器按钮 */
    private lateinit var btnControllerSwitch: Button

    /** 停止播放按钮 */
    private lateinit var btnStopMusic: Button

    /** 控制器父容器 */
    private lateinit var vgControllerWrapper: ViewGroup

    /** 乐谱控制器父容器 */
    private lateinit var vgMusicScoreControllerWrapper: ViewGroup

    /** 切换歌曲按钮 */
    private lateinit var btnSkipMusic: Button

    /** 暂停弹奏按钮 */
    private lateinit var btnPauseMusic: Button

    /** 布局控制器父容器 */
    private lateinit var vgKeyLayoutControllerWrapper: ViewGroup

    /** 选择布局按钮 */
    private lateinit var btnChooseLayout: Button

    /** 重置定位线按钮 */
    private lateinit var btnResetMarker: Button

    /** 显示序号复选框 */
    private lateinit var cbDisplayNumber: CheckBox

    /** 半音复选框 */
    private lateinit var cbEnableSemitone: CheckBox

    /** 按键偏移按钮 */
    private lateinit var btnKeyLayoutOffset: Button

    /** 移除点位按钮 */
    private lateinit var btnPointRemove: Button

    /** 增加点位按钮 */
    private lateinit var btnPointAdd: Button

    /** 选择乐谱按钮 */
    private lateinit var btnChooseMusic: Button

    /** 选择目录按钮 */
    private lateinit var btnChooseFolder: Button

    /** 其他设置按钮 */
    private lateinit var btnOtherSettings: Button

    /** 显示变调按钮 */
    private lateinit var btnModulation: Button

    /** 变调 1 父容器 */
    private lateinit var vgToneWrapper1: ViewGroup

    /** 变调 12 父容器 */
    private lateinit var vgToneWrapper12: ViewGroup

    /** 变调 -1 按钮 */
    private lateinit var btnToneMinus1: Button

    /** 变调 +1 按钮 */
    private lateinit var btnTonePlus1: Button

    /** 变调 -12 按钮 */
    private lateinit var btnToneMinus12: Button

    /** 变调 -12 按钮 */
    private lateinit var btnTonePlus12: Button

    /** 循环复选框 */
    private lateinit var cbRepeatAll: CheckBox

    /** 随机复选框 */
    private lateinit var cbRandom: CheckBox

    /** 开始弹奏按钮 */
    private lateinit var btnStartMusic: Button

    /** 全部布局 */
    private lateinit var keyLayouts: List<KeyLayout>

    /** 当前布局 */
    private var currentLayout: KeyLayout? = null

    /** 当前乐谱 */
    private var currentMusic: MusicNotation? = null

    /** 当前目录 */
    private var currentFolder: File? = null

    /** 音乐弹奏设置 */
    private var musicPlayingSettings = MusicPlayingSettings()

    /** 变调值 */
    private var toneModulation = -1

    private val gson = GsonFactory.getSingletonGson()

    // 创建一个与 Service 生命周期绑定的 CoroutineScope
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences(StringConst.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        keyLayouts = gson.fromJson(
            sharedPreferences.getString(StringConst.SP_DATA_KEY_KEY_LAYOUTS, null),
            TypeConst.listOfKeyLayout.type
        ) ?: emptyList()
        sharedPreferences.getString(StringConst.SP_DATA_KEY_MUSIC_PLAYING_SETTINGS, null)?.let {
            musicPlayingSettings = gson.fromJson(it, MusicPlayingSettings::class.java)
        }

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
            vgMusicScoreControllerWrapper =
                contentView.findViewById(R.id.music_score_controller_wrapper)
            btnSkipMusic = contentView.findViewById(R.id.btn_skip_music)
            btnPauseMusic = contentView.findViewById(R.id.btn_pause_music)
            vgKeyLayoutControllerWrapper =
                contentView.findViewById(R.id.key_layout_controller_wrapper)
            btnChooseLayout = contentView.findViewById(R.id.btn_choose_key_layout)
            btnResetMarker = contentView.findViewById(R.id.btn_reset_marker)
            cbDisplayNumber = contentView.findViewById(R.id.cb_display_number)
            cbEnableSemitone = contentView.findViewById(R.id.cb_enable_semitone)
            btnKeyLayoutOffset = contentView.findViewById(R.id.btn_key_layout_offset)
            btnPointRemove = contentView.findViewById(R.id.btn_point_remove)
            btnPointAdd = contentView.findViewById(R.id.btn_point_add)
            btnChooseMusic = contentView.findViewById(R.id.btn_choose_music)
            btnChooseFolder = contentView.findViewById(R.id.btn_choose_folder)
            btnOtherSettings = contentView.findViewById(R.id.btn_other_settings)
            btnModulation = contentView.findViewById(R.id.btn_modulation)
            vgToneWrapper1 = contentView.findViewById(R.id.tone_wrapper_1)
            vgToneWrapper12 = contentView.findViewById(R.id.tone_wrapper_12)
            btnToneMinus1 = contentView.findViewById(R.id.btn_tone_minus_1)
            btnTonePlus1 = contentView.findViewById(R.id.btn_tone_plus_1)
            btnToneMinus12 = contentView.findViewById(R.id.btn_tone_minus_12)
            btnTonePlus12 = contentView.findViewById(R.id.btn_tone_plus_12)
            cbRepeatAll = contentView.findViewById(R.id.cb_repeat_all)
            cbRandom = contentView.findViewById(R.id.cb_random)
            btnStartMusic = contentView.findViewById(R.id.btn_start_music)
        }
        layoutWindow = EasyWindow.with(application).apply {
            setAnimStyle(android.R.style.Animation_Dialog)
            setContentView(keysLayoutView)
            setOutsideTouchable(false)
            setWidth(ViewGroup.LayoutParams.MATCH_PARENT)
            setHeight(ViewGroup.LayoutParams.MATCH_PARENT)
            setWinVisible(this, View.GONE)
        }

        setupBasicController()
        setupMusicScoreController()
        setupKeysLayoutController()

        // 初始化布局
        sharedPreferences.getInt(StringConst.SP_DATA_KEY_LAST_LAYOUT, 0).takeIf {
            it >= 0 && it < keyLayouts.size
        }?.let { updateCurrentLayout(keyLayouts[it]) }
        // 初始化目录
        updateCurrentFolder(
            sharedPreferences.getString(
                StringConst.SP_DATA_KEY_DEFAULT_FOLDER,
                null
            )
        )
    }

    private fun setupBasicController() {
        // 初始展开
        updateCollapse(false)
        // 初始切换到乐谱控制器
        updateSwitchMusic(true)
        // 初始化演奏模式
        updatePlayModeState(musicPlayingSettings.playMode)
        // 初始化为未弹奏状态
        updatePlayingState(false)
        // 初始化变调为 0
        updateToneModulation(0)
        // 初始勾选显示序号
        cbDisplayNumber.isChecked = keysLayoutView.showNum
        // 初始勾选循环
        cbRepeatAll.isChecked = true
        // 初始勾选随机
        cbRandom.isChecked = true
        // 展开、收起
        btnCollapse.setOnClickListener {
            updateCollapse(vgControllerWrapper.visibility == View.VISIBLE)
        }
        // 布局、乐谱
        btnControllerSwitch.setOnClickListener {
            updateSwitchMusic(vgKeyLayoutControllerWrapper.visibility == View.VISIBLE)
        }
        // 停止弹奏
        btnStopMusic.setOnClickListener {
            MusicPlayer.stop()
        }
        // 切换歌曲
        btnSkipMusic.setOnClickListener {
            MusicPlayer.skip()
        }
        // 暂停弹奏
        btnPauseMusic.setOnClickListener {
            if (MusicPlayer.isPlaying) {
                if (MusicPlayer.isPaused) MusicPlayer.resume()
                else MusicPlayer.pause()
            }
        }
    }

    private fun setupMusicScoreController() {
        // 选择乐谱
        btnChooseMusic.setOnClickListener {
            withCurrentLayout {
                val mfcd = MusicFileChooseDialog(application, serviceScope)
                currentFolder?.let { mfcd.setFolder(it.path) }
                mfcd.setOnFileChose { path, filename ->
                    updateCurrentFolder(path)
                    tryAlert {
                        val index = filename.lastIndexOf(
                            StringConst.MUSIC_NOTATION_FILE_EXT,
                            ignoreCase = true
                        )
                        val file = File(path, filename)
                        // 解析乐谱并置为当前
                        updateCurrentMusic(
                            MusicUtil.parseMusicNotation(
                                file.path,
                                if (index > 0) filename.substring(0, index) else filename,
                                FileUtil.readNoBOMText(file)
                            )
                        )
                        // 尝试找到可完整演奏的最小变调值
                        try {
                            updateToneModulation(MusicUtil.findSuitableOffset(currentMusic!!, it))
                            btnStartMusic.setTextColor(Color.WHITE)
                        } catch (e: MissingKeyException) {
                            updateToneModulation(0)
                            btnStartMusic.setTextColor(Color.RED)
                            MessageDialog(application).apply {
                                setIcon(R.drawable.outline_error_problem_32)
                                setText(R.string.layout_lack_key_message)
                            }.show()
                        } finally {
                            mfcd.destroy()
                        }
                    }
                }
                mfcd.setHighlight(currentMusic?.filepath)
                mfcd.show()
            }
        }
        // 选择目录
        btnChooseFolder.setOnClickListener {
            withCurrentLayout {
                val mfcd = MusicFolderChooseDialog(application, serviceScope)
                currentFolder?.let { mfcd.setFolder(it.path) }
                mfcd.onFolderChose = {
                    updateCurrentFolder(it)
                    mfcd.destroy()
                }
                mfcd.show()
            }
        }
        // 其他设置
        btnOtherSettings.setOnClickListener {
            val mpsd = MusicPlayingSettingsDialog(application)
            mpsd.setSettings(musicPlayingSettings)
            mpsd.onOk = {
                musicPlayingSettings = mpsd.getSettings()
                sharedPreferences.edit().putString(
                    StringConst.SP_DATA_KEY_MUSIC_PLAYING_SETTINGS,
                    gson.toJson(musicPlayingSettings)
                ).apply()
                updatePlayModeState(musicPlayingSettings.playMode)
                mpsd.destroy()
            }
            mpsd.show()
        }
        // 显示变调
        btnModulation.setOnClickListener {
            withCurrentMusic {
                playToneModulation(true)
            }
        }
        // 变调 -1
        btnToneMinus1.setOnClickListener {
            withCurrentMusic {
                updateToneModulation(toneModulation - 1)
                playToneModulation()
            }
        }
        // 变调 +1
        btnTonePlus1.setOnClickListener {
            withCurrentMusic {
                updateToneModulation(toneModulation + 1)
                playToneModulation()
            }
        }
        // 变调 -12
        btnToneMinus12.setOnClickListener {
            withCurrentMusic {
                updateToneModulation(toneModulation - 12)
                playToneModulation()
            }
        }
        // 变调 +12
        btnTonePlus12.setOnClickListener {
            withCurrentMusic {
                updateToneModulation(toneModulation + 12)
                playToneModulation()
            }
        }
        // 开始弹奏
        btnStartMusic.setOnClickListener {
            if (musicPlayingSettings.playMode == PlayMode.List) withCurrentFolder {
                if (!MusicPlayer.isPlaying) {
                    MusicPlayer.startList(
                        serviceScope,
                        it,
                        currentLayout!!,
                        musicPlayingSettings,
                        cbRepeatAll.isChecked,
                        cbRandom.isChecked
                    )
                    updatePlayingState(MusicPlayer.isPlaying)
                }
            } else withCurrentMusic {
                if (!MusicPlayer.isPlaying) try {
                    MusicPlayer.startSingle(
                        serviceScope,
                        it,
                        currentLayout!!,
                        musicPlayingSettings,
                        toneModulation
                    )
                    updatePlayingState(MusicPlayer.isPlaying)
                } catch (e: MissingKeyException) {
                    val cd = ConfirmDialog(application)
                    cd.setIcon(R.drawable.outline_error_problem_32)
                    cd.setText(R.string.missing_key_note_confirm_message)
                    cd.onOk = {
                        cd.destroy()
                        MusicPlayer.startSingle(
                            serviceScope,
                            it,
                            currentLayout!!,
                            musicPlayingSettings,
                            toneModulation,
                            true
                        )
                        updatePlayingState(MusicPlayer.isPlaying)
                    }
                    cd.show()
                }
            }
        }
        MusicPlayer.onStopped = {
            updatePlayingState(false)
        }
        MusicPlayer.onPaused = {
            updatePauseState(true)
        }
        MusicPlayer.onResume = {
            updatePauseState(false)
        }
        MusicPlayer.onMusicSkip = {
            btnSkipMusic.text = it
        }
        ClickAccessibilityService.onVolumeKeyDown = {
            MusicPlayer.pause()
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
                        gson.toJson(keyLayouts)
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
                        keyLayouts += KeyLayout(name = it.toString())
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
                        val cd = ConfirmDialog(application)
                        cd.setIcon(R.drawable.outline_delete_forever_32)
                        cd.setTitle(R.string.delete)
                        cd.setText(getString(R.string.delete_confirm_message, kl.name))
                        cd.onOk = {
                            keyLayouts = keyLayouts.filter { it != kl }
                            // 清空所选项
                            klld.reloadData(keyLayouts, -1)
                            // 如果所选布局是当前布局
                            if (currentLayout == kl) {
                                updateCurrentLayout(null)
                                sharedPreferences.edit()
                                    .putInt(StringConst.SP_DATA_KEY_LAST_LAYOUT, 0).apply()
                            }
                            cd.destroy()
                        }
                        cd.show()
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
        // 重置定位线
        btnResetMarker.setOnClickListener {
            keysLayoutView.resetMarker()
        }
        // 显示序号
        cbDisplayNumber.setOnCheckedChangeListener { _, isChecked ->
            keysLayoutView.showNum = isChecked
        }
        // 启用半音
        cbEnableSemitone.setOnCheckedChangeListener { _, isChecked ->
            withCurrentLayout {
                keysLayoutView.semitone = isChecked
                it.semitone = isChecked
                updateCurrentMusic(null)
            }
        }
        // 按键偏移
        btnKeyLayoutOffset.setOnClickListener {
            withCurrentLayout { kl ->
                val tid = TextInputDialog(application)
                tid.setIcon(R.drawable.outline_plus_one_32)
                tid.setTitle(R.string.key_offset)
                tid.setHint(R.string.enter_key_offset_hint)
                tid.onTextConfirmed = onTextConfirmed@{
                    val o = LangUtil.parseInteger(it)
                    if (o == null) {
                        Toaster.show(R.string.require_correct_integer_message)
                        return@onTextConfirmed
                    }
                    keysLayoutView.pointOffset = o
                    btnKeyLayoutOffset.text = application.getString(R.string.key_layout_offset, o)
                    kl.keyOffset = o
                    updateCurrentMusic(null)
                    tid.destroy()
                }
                tid.show()
            }
        }
        // 移除点位
        btnPointRemove.setOnClickListener {
            withCurrentLayout {
                if (it.points.isEmpty()) return@withCurrentLayout
                val mPoints = it.points.toMutableList()
                val last = mPoints.removeLast()
                it.points = mPoints
                keysLayoutView.setMarker(last)
                keysLayoutView.points = mPoints
                updateCurrentMusic(null)
            }
        }
        // 增加点位
        btnPointAdd.setOnClickListener {
            withCurrentLayout {
                it.points += Point(keysLayoutView.getMarker())
                it.rawOffset.set(keysLayoutView.rawOffset)
                keysLayoutView.points = it.points
                updateCurrentMusic(null)
            }
        }
    }

    private fun updateCurrentLayout(kl: KeyLayout?) {
        if (currentLayout == kl) return
        val oldKl = currentLayout
        currentLayout = kl
        kl?.also {
            btnChooseLayout.text = it.name
            cbEnableSemitone.isChecked = it.semitone
            btnKeyLayoutOffset.text =
                application.getString(R.string.key_layout_offset, it.keyOffset)
            keysLayoutView.points = it.points
            keysLayoutView.semitone = it.semitone
            keysLayoutView.pointOffset = it.keyOffset
            oldKl?.apply {
                // 以下情况置空当前乐谱：
                //  1. 半音不一样；
                //  2. 按键偏移不一样；
                //  3. 按键数变少；
                if (it.semitone != this.semitone || it.keyOffset != this.keyOffset || it.points.size < this.points.size)
                    updateCurrentMusic(null)
            } ?: run {
                updateCurrentMusic(null)
            }
        } ?: run {
            btnChooseLayout.setText(R.string.select_layout)
            keysLayoutView.points = emptyList()
            updateCurrentMusic(null)
        }
    }

    private inline fun withCurrentLayout(block: (c: KeyLayout) -> Unit) {
        currentLayout?.also(block) ?: Toaster.show(R.string.layout_empty_warn_message)
    }

    private fun updateCurrentMusic(mn: MusicNotation?) {
        if (currentMusic == mn) return
        currentMusic = mn
        mn?.also {
            btnChooseMusic.text = it.name
        } ?: run {
            btnChooseMusic.setText(R.string.select_music)
        }
    }

    private fun updateToneModulation(t: Int) {
        if (toneModulation == t) return
        toneModulation = t
        btnModulation.text = getString(R.string.modulation, t)
    }

    private fun updateCurrentFolder(p: String?) {
        if (p != null) {
            val f = File(p)
            if (currentFolder?.path != f.path) sharedPreferences.edit()
                .putString(StringConst.SP_DATA_KEY_DEFAULT_FOLDER, f.path).apply()
            currentFolder = f
            btnChooseFolder.text = f.name
        } else {
            btnChooseFolder.setText(R.string.select_folder)
        }
    }

    private fun updatePlayModeState(playMode: PlayMode) {
        if (playMode == PlayMode.List) {
            btnChooseMusic.visibility = View.GONE
            btnChooseFolder.visibility = View.VISIBLE
            btnModulation.visibility = View.GONE
            vgToneWrapper1.visibility = View.GONE
            vgToneWrapper12.visibility = View.GONE
            cbRepeatAll.visibility = View.VISIBLE
            cbRandom.visibility = View.VISIBLE
        } else {
            btnChooseMusic.visibility = View.VISIBLE
            btnChooseFolder.visibility = View.GONE
            btnModulation.visibility = View.VISIBLE
            vgToneWrapper1.visibility = View.VISIBLE
            vgToneWrapper12.visibility = View.VISIBLE
            cbRepeatAll.visibility = View.GONE
            cbRandom.visibility = View.GONE
        }
    }

    private fun updatePlayingState(p: Boolean) {
        if (p) {
            btnControllerSwitch.visibility = View.GONE
            btnStopMusic.visibility = View.VISIBLE
            vgMusicScoreControllerWrapper.visibility = View.GONE
            btnSkipMusic.visibility =
                if (musicPlayingSettings.playMode == PlayMode.List) View.VISIBLE else View.GONE
            btnPauseMusic.visibility = View.VISIBLE
            updatePauseState(false) // 强制取消暂停，里面已经有设置悬浮窗显隐了
            if (musicPlayingSettings.playMode == PlayMode.List) btnSkipMusic.text = ""
        } else {
            btnControllerSwitch.visibility = vgControllerWrapper.visibility
            btnStopMusic.visibility = View.GONE
            vgMusicScoreControllerWrapper.visibility = View.VISIBLE
            btnSkipMusic.visibility = View.GONE
            btnPauseMusic.visibility = View.GONE
            setWinVisible(controllerWindow, View.VISIBLE)
        }
    }

    private fun updatePauseState(p: Boolean) {
        if (p) {
            btnPauseMusic.setText(R.string.resume)
            setWinVisible(controllerWindow, View.VISIBLE)
        } else {
            btnPauseMusic.setText(R.string.pause)
            setWinVisible(
                controllerWindow,
                if (musicPlayingSettings.hideWindow) View.GONE else View.VISIBLE
            )
        }
    }

    private fun updateCollapse(c: Boolean) {
        if (c) {
            vgControllerWrapper.visibility = View.GONE
            btnControllerSwitch.visibility = View.GONE
            btnStopMusic.visibility = View.GONE
            btnCollapse.setText(R.string.expand)
        } else {
            vgControllerWrapper.visibility = View.VISIBLE
            if (MusicPlayer.isPlaying) {
                btnControllerSwitch.visibility = View.GONE
                btnStopMusic.visibility = View.VISIBLE
            } else {
                btnControllerSwitch.visibility = View.VISIBLE
                btnStopMusic.visibility = View.GONE
            }
            btnCollapse.setText(R.string.collapse)
        }
    }

    private fun updateSwitchMusic(m: Boolean) {
        if (m) {
            vgKeyLayoutControllerWrapper.visibility = View.GONE
            vgMusicScoreControllerWrapper.visibility = View.VISIBLE
            setWinVisible(layoutWindow, View.GONE)
            btnControllerSwitch.setText(R.string.key_layout)
        } else {
            vgKeyLayoutControllerWrapper.visibility = View.VISIBLE
            vgMusicScoreControllerWrapper.visibility = View.GONE
            setWinVisible(layoutWindow, View.VISIBLE)
            btnControllerSwitch.setText(R.string.music_score)
            keysLayoutView.post {
                if (keysLayoutView.isMarkerOutOfView()) {
                    // 重置定位线
                    keysLayoutView.resetMarker()
                }
            }
        }
    }

    private inline fun withCurrentMusic(block: (c: MusicNotation) -> Unit) {
        currentMusic?.also(block) ?: Toaster.show(R.string.music_empty_warn_message)
    }

    private fun playToneModulation(showError: Boolean = false) {
        try {
            MusicPlayer.playKeyNote(currentMusic!!.keyNote, currentLayout!!, toneModulation)
        } catch (e: MissingKeyException) {
            if (showError) Toaster.show(R.string.key_note_absent_message)
        }
    }

    private inline fun withCurrentFolder(block: (c: File) -> Unit) {
        currentFolder?.also(block) ?: Toaster.show(R.string.folder_empty_warn_message)
    }

    private fun setWinVisible(win: EasyWindow<*>, visibility: Int) {
        win.windowVisibility = visibility
        win.update()
    }

    private inline fun tryAlert(func: () -> Unit) {
        try {
            func()
        } catch (e: Throwable) {
            val msg = if (e is ServiceException) e.getI18NMessage(this)
            else e.cause?.message ?: e.message ?: getString(R.string.unknown_error)
            MessageDialog.showErrorMessage(application, msg)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        layoutWindow.show()
        controllerWindow.show()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        ClickAccessibilityService.onVolumeKeyDown = null
        MusicPlayer.onStopped = null
        MusicPlayer.onResume = null
        MusicPlayer.onPaused = null
        controllerWindow.recycle()
        layoutWindow.recycle()
        serviceScope.cancel()
        super.onDestroy()
    }
}
