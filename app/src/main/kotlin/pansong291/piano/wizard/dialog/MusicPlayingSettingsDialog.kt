package pansong291.piano.wizard.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.ScrollView
import com.xw.repo.BubbleSeekBar
import pansong291.piano.wizard.R
import pansong291.piano.wizard.dialog.actions.DialogConfirmActions
import pansong291.piano.wizard.dialog.base.BaseDialog
import pansong291.piano.wizard.entity.MusicPlayingSettings
import pansong291.piano.wizard.entity.TapMode
import pansong291.piano.wizard.utils.ViewUtil.dpInt

class MusicPlayingSettingsDialog(context: Context) : BaseDialog(context) {
    private val bsbTempoRate: BubbleSeekBar
    private val rgTapMode: RadioGroup
    private val bsbEarlyRelease: BubbleSeekBar
    private val bsbTapInterval: BubbleSeekBar
    private val bsbPrePlayDelay: BubbleSeekBar
    private val bsbPostPlayDelay: BubbleSeekBar
    private val cbHideWindow: CheckBox
    var onOk: (() -> Unit)? = null

    init {
        setIcon(R.drawable.outline_settings_applications_32)
        setTitle(R.string.playing_settings)
        DialogConfirmActions.loadIn(this) { ok, cancel ->
            ok.setOnClickListener { onOk?.invoke() }
        }
        val scrollView = ScrollView(context)
        scrollView.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val horizontalPadding = 16.dpInt()
        scrollView.setPadding(horizontalPadding, horizontalPadding, horizontalPadding, 0)
        findContentWrapper().addView(scrollView)
        val root = LayoutInflater.from(context)
            .inflate(R.layout.dialog_content_music_playing_settings, scrollView)
        bsbTempoRate = root.findViewById(R.id.bsb_tempo_rate)
        rgTapMode = root.findViewById(R.id.rg_tap_mode)
        bsbEarlyRelease = root.findViewById(R.id.bsb_early_release)
        bsbTapInterval = root.findViewById(R.id.bsb_tap_interval)
        bsbPrePlayDelay = root.findViewById(R.id.bsb_pre_play_delay)
        bsbPostPlayDelay = root.findViewById(R.id.bsb_post_play_delay)
        cbHideWindow = root.findViewById(R.id.cb_hide_window)

        scrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            bsbTempoRate.correctOffsetWhenContainerOnScrolling()
            bsbEarlyRelease.correctOffsetWhenContainerOnScrolling()
            bsbTapInterval.correctOffsetWhenContainerOnScrolling()
            bsbPrePlayDelay.correctOffsetWhenContainerOnScrolling()
            bsbPostPlayDelay.correctOffsetWhenContainerOnScrolling()
        }
        rgTapMode.setOnCheckedChangeListener { _, id ->
            setBubbleSeekBarEnabled(bsbEarlyRelease, id == android.R.id.button2)
            setBubbleSeekBarEnabled(bsbTapInterval, id == android.R.id.button3)
        }
    }

    fun setSettings(settings: MusicPlayingSettings) {
        bsbTempoRate.setProgress(settings.tempoRate)
        rgTapMode.check(
            when (settings.tapMode) {
                TapMode.Tap -> android.R.id.button1
                TapMode.TapAndHold -> android.R.id.button2
                TapMode.RepeatedlyTap -> android.R.id.button3
            }
        )
        setBubbleSeekBarEnabled(bsbEarlyRelease, settings.tapMode == TapMode.TapAndHold)
        setBubbleSeekBarEnabled(bsbTapInterval, settings.tapMode == TapMode.RepeatedlyTap)
        bsbEarlyRelease.setProgress(settings.earlyRelease.toFloat())
        bsbTapInterval.setProgress(settings.tapInterval.toFloat())
        bsbPrePlayDelay.setProgress(settings.prePlayDelay.toFloat())
        bsbPostPlayDelay.setProgress(settings.postPlayDelay.toFloat())
        cbHideWindow.isChecked = settings.hideWindow
    }

    fun getSettings(): MusicPlayingSettings {
        return MusicPlayingSettings().apply {
            tempoRate = bsbTempoRate.progressFloat
            tapMode = when (rgTapMode.checkedRadioButtonId) {
                android.R.id.button2 -> TapMode.TapAndHold
                android.R.id.button3 -> TapMode.RepeatedlyTap
                else -> TapMode.Tap
            }
            earlyRelease = bsbEarlyRelease.progress
            tapInterval = bsbTapInterval.progress
            prePlayDelay = bsbPrePlayDelay.progress
            postPlayDelay = bsbPostPlayDelay.progress
            hideWindow = cbHideWindow.isChecked
        }
    }

    private fun setBubbleSeekBarEnabled(bsb: BubbleSeekBar, enabled: Boolean) {
        bsb.isEnabled = enabled
        bsb.alpha = if (enabled) 1f else .2f
    }
}
