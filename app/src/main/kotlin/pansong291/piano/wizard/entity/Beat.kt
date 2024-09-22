package pansong291.piano.wizard.entity

/**
 * 节拍
 */
class Beat {
    /**
     * 时值倍率；例如 0.5 表示为基础时值的一半
     */
    var durationRate: Float = 1f

    /**
     * 音调，支持和弦
     */
    var tones: List<Int> = emptyList()
}
