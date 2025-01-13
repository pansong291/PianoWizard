package pansong291.piano.wizard.utils

import com.hjq.gson.factory.GsonFactory
import pansong291.piano.wizard.R
import pansong291.piano.wizard.entity.Beat
import pansong291.piano.wizard.entity.MusicNotation
import pansong291.piano.wizard.exceptions.ServiceException

object MusicUtil {
    // 以 0 开始的十二平均律和对应的自然音
    // 0 1 2 3 4 5 6 7 8 9 10 11
    // 0   2   4 5   7   9    11
    val semitones = listOf(1, 3, 6, 8, 10)
    val naturals = listOf(0, 2, 4, 5, 7, 9, 11)
    private val blockCommentRegex = Regex("/\\*[\\d\\D]*?\\*/")
    private val lineCommentRegex = Regex("//[^\\n]*")
    private val spaceCharRegex = Regex("\\s+")
    private val musicSyntaxRegex =
        Regex("^\\[1=([A-G][#b]?),\\d+/\\d+,(\\d+)](\\d(?:[-+#b]\\d*)*(?:&\\d(?:[-+#b]\\d*)*)*(?:[*/]\\d+)*(?:,\\d(?:[-+#b]\\d*)*(?:&\\d(?:[-+#b]\\d*)*)*(?:[*/]\\d+)*)*),?$")
    private val musicBeatRegex = Regex("^([^*/]+)((?:[*/]\\d+)*)\$")
    private val startsWithNumberRegex = Regex("^(\\d+)?(.*)$")

    /**
     * 判断是否是半音（钢琴黑键）
     * @param tet12 十二平均律
     */
    fun isSemitone(tet12: Int): Boolean {
        var note = tet12 % 12
        if (note < 0) note += 12
        return semitones.contains(note)
    }

    /**
     * 判断是否是自然音（钢琴白键）
     * @param tet12 十二平均律
     */
    fun isNatural(tet12: Int): Boolean {
        var note = tet12 % 12
        if (note < 0) note += 12
        return naturals.contains(note)
    }

    /**
     * 基本音级转十二平均律
     * @param basic 基本音级 0~6
     * @return 十二平均律 0~11
     */
    fun basicNoteTo12TET(basic: Int): Int {
        // 计算八度变化
        var octaveShift = basic / 7
        // 计算基础音符在 0 到 6 的范围内的对应值
        var basicInRange = basic % 7
        // 将负数的情况处理为正数，并对应到负八度
        if (basicInRange < 0) {
            basicInRange += 7
            // 八度向下移动
            octaveShift--
        }
        basicInRange = naturals[basicInRange]
        // 返回计算的值
        return basicInRange + octaveShift * 12
    }

    /**
     * 移除乐谱中的注释
     */
    private fun removeComment(str: String): String {
        return str.replace(blockCommentRegex, "")
            .replace(lineCommentRegex, "")
            .replace(spaceCharRegex, "")
    }

    /**
     * 校验乐谱语法
     */
    private fun checkMusicSyntax(str: String): Triple<String, String, String> {
        val values = musicSyntaxRegex.find(str)?.groupValues
        if (values == null || values.size < 4) throw ServiceException(R.string.music_syntax_error_message)
        return Triple(values[1], values[2], values[3])
    }

    /**
     * 解析乐谱
     */
    fun parseMusicNotation(filepath: String, name: String, content: String): MusicNotation {
        val triple = checkMusicSyntax(removeComment(content))
        val key = triple.first
        var keyNote = key[0] - 'C'
        if (keyNote < 0) keyNote += 7 // 把 AB 移到 G 的后面
        keyNote = basicNoteTo12TET(keyNote) // 转为十二平均律
        if (key.length > 1) when (key[1]) {
            '#' -> keyNote++
            'b' -> keyNote--
        }
        return MusicNotation().also {
            it.name = name
            it.filepath = filepath
            it.keyNote = keyNote
            it.bpm = triple.second.toInt()
            it.beats = triple.third.split(",").filter(String::isNotEmpty).map(::parseBeat)
        }
    }

    /**
     * 解析节拍
     */
    private fun parseBeat(str: String): Beat {
        val values = musicBeatRegex.find(str)?.groupValues
        if (values == null || values.size < 3) throw ServiceException(R.string.music_syntax_error_message)
        return Beat().apply {
            durationRate = parseRate(values[2], 1f)
            tones = values[1].split('&').mapNotNull(::parseNote).distinct()
        }
    }

    /**
     * 解析时值倍率
     */
    private fun parseRate(str: String?, acc: Float): Float {
        if (str.isNullOrEmpty()) return acc
        // *21/5  乘除后面的数字是必有的
        val rest = startsWithNumberRegex.find(str.substring(1))?.groupValues
        val n = rest?.getOrNull(1)?.toIntOrNull()?.takeIf { it > 0 }
            ?: throw ServiceException(R.string.music_syntax_error_message)
        return parseRate(
            rest.getOrNull(2),
            if (str[0] == '/') acc / n else acc * n
        )
    }

    /**
     * 解析音符
     */
    private fun parseNote(str: String): Int? {
        val note = str[0] - '0'
        if (note < 0 || note > 7) throw ServiceException(R.string.unsupported_note_message, note)
        if (note == 0) return null
        // 这里减 1 使其变为从 0 开始，再转为十二平均律
        return basicNoteTo12TET(note - 1) + parseAccidental(str.substring(1), 0)
    }

    /**
     * 解析变音记号
     */
    private fun parseAccidental(str: String?, acc: Int): Int {
        if (str.isNullOrEmpty()) return acc
        // +2b  表示升 2 个八度，降半调
        // -#2  表示降一个八度，升 2 个半调
        // 数字省略则为 1
        val rest = startsWithNumberRegex.find(str.substring(1))?.groupValues
        var n = rest?.getOrNull(1)?.toIntOrNull() ?: 1
        // 都用十二平均律来表示，所以升降八度是以 12 作倍数
        when (str[0]) {
            '+' -> n *= 12
            '-' -> n *= -12
            'b' -> n = -n
        }
        return parseAccidental(rest?.getOrNull(2), acc + n)
    }

    /**
     * 将十二平均律编译为音符
     */
    fun compile12TETNote(tet12: Int): String {
        var octaveShift = tet12 / 12
        var tet12Rang = tet12 % 12
        if (tet12Rang < 0) {
            tet12Rang += 12
            octaveShift--
        }
        val isSemi = semitones.contains(tet12Rang)
        if (isSemi) tet12Rang--
        val note = naturals.indexOf(tet12Rang) + 1
        if (note == 0) return "0"
        val octave = when {
            octaveShift == 0 -> ""
            octaveShift == 1 -> "+"
            octaveShift == -1 -> "-"
            octaveShift > 0 -> "+$octaveShift"
            else -> octaveShift.toString()
        }
        return if (isSemi) "$note$octave#" else "$note$octave"
    }

    /**
     * 将基本音级编译为音符
     */
    fun compileBasicNote(basic: Int): String {
        var octaveShift = basic / 7
        var basicRange = basic % 7
        if (basicRange < 0) {
            basicRange += 7
            octaveShift--
        }
        val octave = when {
            octaveShift == 0 -> ""
            octaveShift == 1 -> "+"
            octaveShift == -1 -> "-"
            octaveShift > 0 -> "+$octaveShift"
            else -> octaveShift.toString()
        }
        return "${basicRange + 1}$octave"
    }

    fun appendBeat(strBuilder: StringBuilder, notes: CharSequence, duration: Long, baseTime: Long) {
        var rateA = duration
        var rateB = baseTime
        val gcd = LangUtil.gcd(rateA, rateB)
        rateA /= gcd
        rateB /= gcd
        if (rateA > 0) strBuilder.append(notes).apply {
            if (rateA != 1L) append('*').append(rateA)
            if (rateB != 1L) append('/').append(rateB)
            append(',')
        }
    }
}

fun main() {
    val message = MusicUtil.parseMusicNotation(
        "", "", """
        // 乐谱：两只老虎

        // 这是单行注释

        /*
          这是块注释
        */

        // 注释的内容会被忽略，用于对乐谱内容作说明或备注。
        // 乐谱允许在任意位置添加空白字符，可以使其看起来更舒适一些。

        [ 1=C, 4/4, 120 ]         // 基准音调，节拍，每分钟拍数。
                                  // 基准音调可为 C, D, E, F, G, A, B。
                                  // 可配合变音记号使用，如 C#, Db。

        1, 2, 3, 1,               // 1 表示 do，2 表示 re，以此类推。
        1, 2, 3, 1,               // 每个节拍使用英文逗号隔开。
        3, 4, 5*2,                // 5*2 表示 5 这个节拍的时值乘以 2。
        3, 4, 5*2,
        5/2, 6/2, 5/2, 4/2, 3, 1, // 5/2 表示 5 这个节拍的时值除以 2。
        5/2, 6/2, 5/2, 4/2, 3, 1,
        1, 5-1, 1*2,              // 5-1 表示 5 这个节拍的音符需要降 1 个八度。
                                  // 类似的，5+1 表示需要升 1 个八度。
        1, 5-, 1*2,               // 升降 1 倍的八度时，数字 1 可省略。
                                  // 此处的 5- 即为 5-1。

        0*8,                      // 0 表示休止符或者停顿。

        // 和弦使用 & 符号拼接音符，比如：
        1 & 2- & 3+2 *2,
        // 表示这个节拍有三个音符，分别是 do，降 1 个八度的 re，和升 2 个八度的 mi。
        // 最后的 *2 表示这个节拍的时值需要乘以 2。

        // 时值倍率不是整数时，则乘以一个分数，比如表示三分之一的时值：
        7 * 1/3, 7 / 3,

        // 表示三分之二的时值：
        7 * 2/3,

        // 升半调使用 # 号：
        1#,

        // 升两个半调：
        1#2,

        // 降半调使用 b 号：
        2b,

        // 降两个半调：
        2b2,

        // 升一个八度降半调：
        3+b, 3b+,

    """.trimIndent()
    )
    println(GsonFactory.getSingletonGson().toJson(message))
    println("\nbasic -> 12TET -> note")
    for (i in -14..20) {
        val tet12 = MusicUtil.basicNoteTo12TET(i)
        println("$i -> $tet12 -> ${MusicUtil.compile12TETNote(tet12)}")
    }
    println("\n12TET -> note")
    for (i in -24..35) {
        println("$i -> ${MusicUtil.compile12TETNote(i)}")
    }
}
