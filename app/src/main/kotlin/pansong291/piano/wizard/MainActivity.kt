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
import pansong291.piano.wizard.consts.ColorConst
import pansong291.piano.wizard.consts.StringConst
import pansong291.piano.wizard.services.ClickAccessibilityService
import pansong291.piano.wizard.services.MainService

class MainActivity : AppCompatActivity() {
    private lateinit var btnFilePerm: Button
    private lateinit var btnWinPerm: Button
    private lateinit var btnAccessibilityPerm: Button
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnAbout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        btnFilePerm = findViewById(R.id.btn_main_file_perm)
        btnAccessibilityPerm = findViewById(R.id.btn_main_accessibility_perm)
        btnWinPerm = findViewById(R.id.btn_main_win_perm)
        btnStart = findViewById(R.id.btn_main_start)
        btnStop = findViewById(R.id.btn_main_stop)
        btnAbout = findViewById(R.id.btn_main_about)
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
        btnStart.setOnClickListener {
            startService(Intent(this, MainService::class.java))
        }
        btnStop.setOnClickListener {
            stopService(Intent(this, MainService::class.java))
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
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setData(Uri.parse(StringConst.ABOUT_REPOSITORY_LINK))
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                    }
                }
                .show()
        }
    }

    override fun onStart() {
        super.onStart()
        updatePermState(7)
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
}
