package pansong291.piano.wizard.consts

import android.os.Environment

object StringConst {
    /**
     * 外部存储路径
     */
    val EXTERNAL_PATH: String by lazy { Environment.getExternalStorageDirectory().path }

    /**
     * 乐谱后缀
     */
    const val MUSIC_NOTATION_FILE_EXT = ".yp.txt"

    /**
     * 默认数据存储文件名
     */
    const val SHARED_PREFERENCES_NAME = "piano_wizard"

    /**
     * 全部键盘布局数据
     */
    const val SP_DATA_KEY_KEY_LAYOUTS = "key_layouts"

    /**
     * 上次使用的布局。Int
     */
    const val SP_DATA_KEY_LAST_LAYOUT = "last_layout"

    /**
     * 默认目录
     */
    const val SP_DATA_KEY_DEFAULT_FOLDER = "default_folder"

    /**
     * SkyStudio 乐谱的上次选择目录
     */
    const val SP_DATA_KEY_SKY_STUDIO_SHEET_LAST_FOLDER = "sky_studio_sheet_last_folder"

    /**
     * Midi 文件的上次选择目录
     */
    const val SP_DATA_KEY_MIDI_FILE_LAST_FOLDER = "midi_file_last_folder"

    /**
     * 演奏设置
     */
    const val SP_DATA_KEY_MUSIC_PLAYING_SETTINGS = "music_playing_settings"

    /**
     * 打开过教程
     */
    const val SP_DATA_KEY_TUTORIAL_OPENED = "tutorial_opened"

    /**
     * SkyStudio 乐谱的文件后缀
     */
    const val SKY_STUDIO_SHEET_FILE_EXT = ".txt"

    /**
     * Midi 文件的后缀
     */
    const val MIDI_FILE_EXT = ".mid"

    /**
     * 备份文件的后缀
     */
    const val BAK_FILE_EXT = ".bak"

    const val ABOUT_QQ_GROUP_NUMBER = "906654380"
    const val ABOUT_REPOSITORY_LINK = "https://github.com/pansong291/PianoWizard"
}
