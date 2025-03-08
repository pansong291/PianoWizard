package pansong291.piano.wizard.entity

/**
 * 乐谱
 */
class MusicNotation(
    /**
     * 乐谱名
     */
    var name: String = "",

    /**
     * 乐谱文件路径
     */
    var filepath: String = "",

    /**
     * 基准音调
     */
    var keyNote: Int = 0,

    /**
     * 每分钟节拍数
     */
    var bpm: Int = 1,

    /**
     * 节拍
     */
    var beats: List<Beat> = emptyList(),
)
