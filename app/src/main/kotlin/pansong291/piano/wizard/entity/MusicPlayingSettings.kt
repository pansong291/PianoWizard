package pansong291.piano.wizard.entity

class MusicPlayingSettings(
    /** 演奏模式 */
    var playMode: PlayMode = PlayMode.Single,

    /** 变速 */
    var playbackRate: Float = 1f,

    /** 点击模式 */
    var tapMode: TapMode = TapMode.Tap,

    /**
     * 提前释放
     * when: [TapMode.TapAndHold]
     */
    var earlyRelease: Int = 100,

    /**
     * 点击间隔
     * when: [TapMode.RepeatedlyTap]
     */
    var tapInterval: Int = 100,

    /** 演奏前摇 */
    var prePlayDelay: Int = 0,

    /** 演奏后摇 */
    var postPlayDelay: Int = 0,

    /** 隐藏窗口 */
    var hideWindow: Boolean = false,
)

enum class PlayMode {
    /** 单曲 */
    Single,

    /** 列表 */
    List
}

enum class TapMode {
    /** 点触 */
    Tap,

    /** 按住 */
    TapAndHold,

    /** 连点 */
    RepeatedlyTap
}
