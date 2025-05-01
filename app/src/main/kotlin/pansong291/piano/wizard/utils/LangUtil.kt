package pansong291.piano.wizard.utils

object LangUtil {
    private val shortLineRegex = Regex("[-—–－ㄧ一─━]+")

    /**
     * 插入排序，在输入几乎是已经排好序的情况下时间复杂度最小
     */
    fun <T> insertionSort(list: MutableList<T>, comparator: (T, T) -> Boolean) {
        val len = list.size
        for (i in 1 until len) {
            val key = list[i] // 选取要插入的元素
            var j = i - 1

            /* 将大于key的元素向后移动一位 */
            while (j >= 0 && !comparator(list[j], key)) {
                list[j + 1] = list[j]
                j--
            }
            list[j + 1] = key // 插入key到正确的位置
        }
    }

    /**
     * 求最大公约数
     */
    fun gcd(a: Long, b: Long): Long {
        var big = maxOf(a, b)
        var small = minOf(a, b)
        while (small != 0L) {
            val temp = small
            small = big % small
            big = temp
        }
        return big
    }

    /**
     * 用户无法准确分辨负号，此函数尝试替换为正确的负号。
     */
    fun parseInteger(str: CharSequence): Int? {
        return str.trim().replace(shortLineRegex, "-").toIntOrNull()
    }
}
