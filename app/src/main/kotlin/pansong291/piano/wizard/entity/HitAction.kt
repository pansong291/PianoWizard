package pansong291.piano.wizard.entity

/**
 * 触发操作
 */
class HitAction(
    /**
     * 要触发的位置
     */
    var locations: List<Int> = emptyList(),

    /**
     * 触发后的延时
     */
    var postDelay: Int = 0,
)
