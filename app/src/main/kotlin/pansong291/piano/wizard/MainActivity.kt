package pansong291.piano.wizard

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.hjq.toast.Toaster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import pansong291.piano.wizard.consts.ColorConst
import pansong291.piano.wizard.consts.StringConst
import pansong291.piano.wizard.coroutine.AssetsExtractor
import pansong291.piano.wizard.coroutine.MidiConvertor
import pansong291.piano.wizard.coroutine.SkyStudioFileConvertor
import pansong291.piano.wizard.dialog.ConfirmDialog
import pansong291.piano.wizard.dialog.LoadingDialog
import pansong291.piano.wizard.dialog.MessageDialog
import pansong291.piano.wizard.dialog.MidiFileChooseDialog
import pansong291.piano.wizard.dialog.SelectChannelListDialog
import pansong291.piano.wizard.dialog.SkyStudioSheetChooseDialog
import pansong291.piano.wizard.services.ClickAccessibilityService
import pansong291.piano.wizard.services.MainService
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var btnFilePerm: Button
    private lateinit var btnWinPerm: Button
    private lateinit var btnAccessibilityPerm: Button
    private lateinit var btnAbout: Button
    private lateinit var btnConvertSkyStudio: Button
    private lateinit var btnConvertMidiFile: Button
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button

    // 创建一个与 Activity 生命周期绑定的 CoroutineScope
    private val activityScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        btnFilePerm = findViewById(R.id.btn_main_file_perm)
        btnWinPerm = findViewById(R.id.btn_main_win_perm)
        btnAccessibilityPerm = findViewById(R.id.btn_main_accessibility_perm)
        btnAbout = findViewById(R.id.btn_main_about)
        btnConvertSkyStudio = findViewById(R.id.btn_main_convert_sky_studio)
        btnConvertMidiFile = findViewById(R.id.btn_main_convert_midi)
        btnStart = findViewById(R.id.btn_main_start)
        btnStop = findViewById(R.id.btn_main_stop)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btnFilePerm.setOnClickListener {
            XXPermissions.with(this).permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .request { _, _ -> updatePermState(1, true) }
        }
        btnWinPerm.setOnClickListener {
            XXPermissions.with(this).permission(Permission.SYSTEM_ALERT_WINDOW)
                .request { _, _ -> updatePermState(2, true) }
        }
        btnAccessibilityPerm.setOnClickListener {
            if (isAccessibilitySettingsOn(this, ClickAccessibilityService::class.java)) {
                updatePermState(4, true)
            } else {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }
        btnAbout.setOnClickListener {
            AlertDialog.Builder(this)
                .setIcon(R.drawable.outline_info_32)
                .setTitle(R.string.about)
                .setMessage(
                    getString(
                        R.string.about_message,
                        StringConst.ABOUT_REPOSITORY_LINK,
                        StringConst.ABOUT_QQ_GROUP_NUMBER
                    )
                )
                .setPositiveButton(R.string.about_copy_group_number) { _, _ ->
                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(
                        ClipData.newPlainText(
                            "qq_group_number",
                            StringConst.ABOUT_QQ_GROUP_NUMBER
                        )
                    )
                }
                .setNegativeButton(R.string.about_goto_repository) { _, _ ->
                    openUrl(StringConst.ABOUT_REPOSITORY_LINK)
                }
                .setNeutralButton(R.string.demo_video) { _, _ ->
                    openUrl(getString(R.string.link_operation_demo_video))
                }
                .show()
        }
        btnConvertSkyStudio.setOnClickListener {
            val ssscd = SkyStudioSheetChooseDialog(this)
            ssscd.setOnFileChose { path, file ->
                showLoadingAndConvertSkyStudioFile(File(path, file), ssscd::reload)
            }
            ssscd.onFolderChose = { path ->
                val cd = ConfirmDialog(this)
                cd.setText(R.string.convert_current_folder_confirm_message)
                cd.onOk = {
                    showLoadingAndConvertSkyStudioFile(File(path), ssscd::reload)
                    cd.destroy()
                }
                cd.show()
            }
            ssscd.show()
        }
        btnConvertMidiFile.setOnClickListener {
            val mfcd = MidiFileChooseDialog(this)
            mfcd.setOnFileChose { path, file ->
                MidiConvertor.onParseFinished = LoadingDialog(this).apply { show() }::destroy
                MidiConvertor.onParseResult = { result, message ->
                    if (result != null) {
                        val scld = SelectChannelListDialog(this, result.keys.sorted())
                        scld.onConfirmed = { keys, merge ->
                            if (keys.isEmpty()) Toaster.show(R.string.require_channel_selected_message)
                            else {
                                MidiConvertor.onConvertFinished =
                                    LoadingDialog(this).apply { show() }::destroy
                                MidiConvertor.onConvertResult = {
                                    if (message != null)
                                        MessageDialog.showErrorMessage(this, message)
                                    else Toaster.show(R.string.convert_success)
                                }
                                MidiConvertor.convert(activityScope, result.filter {
                                    keys.contains(it.key)
                                }, file, path, merge)
                            }
                        }
                        scld.show()
                    } else if (message != null) MessageDialog.showErrorMessage(this, message)
                }
                MidiConvertor.parse(activityScope, File(path, file))
            }
            mfcd.show()
        }
        btnStart.setOnClickListener {
            startService(Intent(this, MainService::class.java))
        }
        btnStop.setOnClickListener {
            stopService(Intent(this, MainService::class.java))
        }
        checkAndExtractAssets()
    }

    override fun onStart() {
        super.onStart()
        updatePermState(7)
    }

    override fun onDestroy() {
        activityScope.cancel()
        super.onDestroy()
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(Uri.parse(url))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun showLoadingAndConvertSkyStudioFile(file: File, onSuccess: () -> Unit) {
        SkyStudioFileConvertor.onFinished = LoadingDialog(this).apply { show() }::destroy
        SkyStudioFileConvertor.onResult = {
            onSuccess()
            MessageDialog(this).apply {
                setIcon(R.drawable.outline_feedback_32)
                setTitle(R.string.convert_result)
                setText(it)
                show()
            }
        }
        SkyStudioFileConvertor.convert(application, activityScope, file)
    }

    private fun updatePermState(flags: Int, success: Boolean? = null) {
        if (flags and 1 == 1) {
            val s = success ?: XXPermissions.isGranted(this, Permission.MANAGE_EXTERNAL_STORAGE)
            btnFilePerm.text = getString(R.string.btn_req_file_perm, getOnOffString(s))
            btnFilePerm.backgroundTintList =
                ColorStateList.valueOf(if (s) ColorConst.GREEN_800 else ColorConst.RED_800)
        }
        if (flags and 2 == 2) {
            val s = success ?: XXPermissions.isGranted(this, Permission.SYSTEM_ALERT_WINDOW)
            btnWinPerm.text = getString(R.string.btn_req_win_perm, getOnOffString(s))
            btnWinPerm.backgroundTintList =
                ColorStateList.valueOf(if (s) ColorConst.GREEN_800 else ColorConst.RED_800)
        }
        if (flags and 4 == 4) {
            val s =
                success ?: isAccessibilitySettingsOn(this, ClickAccessibilityService::class.java)
            btnAccessibilityPerm.text =
                getString(R.string.btn_req_accessibility_perm, getOnOffString(s))
            btnAccessibilityPerm.backgroundTintList =
                ColorStateList.valueOf(if (s) ColorConst.GREEN_800 else ColorConst.RED_800)
        }
    }

    private fun getOnOffString(b: Boolean): String {
        if (b) return getString(R.string.common_on)
        return getString(R.string.common_off)
    }

    private fun isAccessibilitySettingsOn(
        mContext: Context, clazz: Class<out AccessibilityService>
    ): Boolean {
        try {
            if (Settings.Secure.getInt(
                    mContext.applicationContext.contentResolver,
                    Settings.Secure.ACCESSIBILITY_ENABLED
                ) == 1
            ) {
                Settings.Secure.getString(
                    mContext.applicationContext.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )?.let {
                    val service = mContext.packageName + "/" + clazz.canonicalName
                    TextUtils.SimpleStringSplitter(':').apply {
                        setString(it)
                        while (hasNext()) {
                            if (next().equals(service, ignoreCase = true)) {
                                return true
                            }
                        }
                    }
                }
            }
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }
        return false
    }

    private fun checkAndExtractAssets() {
        val ypFolder = File(getExternalFilesDir(null), "yp")
        if (ypFolder.exists() || !ypFolder.mkdirs()) return

        AssetsExtractor.onFinished = LoadingDialog(this).apply { show() }::destroy
        AssetsExtractor.onError = {
            MessageDialog(this).apply {
                setIcon(R.drawable.outline_error_problem_32)
                setTitle(R.string.error)
                setText(it)
                show()
            }
        }
        AssetsExtractor.startExtraction(this, activityScope)
    }
}
