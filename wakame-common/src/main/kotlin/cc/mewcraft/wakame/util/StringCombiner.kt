package cc.mewcraft.wakame.util

/**
 * A class designed to combine a base string with multiple lists of strings under specific conditions.
 *
 * Example usage:
 *
 * The following example demonstrates how to use the StringCombiner class to generate combinations of a base string
 * "foo" with two lists of strings ["a1", "a2", "a3"] and ["b1", "b2"], where the second list's inclusion is conditional
 * upon it having more than one element.
 *
 * ```
 * fun main() {
 *     val combiner = StringCombiner("foo") {
 *         addList(listOf("a1", "a2", "a3")) // 默认条件
 *         addList(listOf("b1", "b2"), true) // 自定义条件
 *     }
 *     val result = combiner.combine()
 *     result.forEach { println(it) }
 * }
 * ```
 *
 * Output:
 * foo:a1:b1
 * foo:a1:b2
 * foo:a2:b1
 * foo:a2:b2
 * foo:a3:b1
 * foo:a3:b2
 *
 * This output demonstrates the combinations of the base string "foo" with each element of the first list and each element
 * of the second list, as both lists satisfy their conditions for inclusion.
 *
 * @property base The base string that will be prefixed to each combination result.
 */
class StringCombiner(
    private val base: String,
    private val split: String = ":",
) {
    private val lists: MutableList<List<String>> = mutableListOf()
    private val conditions: MutableList<Boolean> = mutableListOf()

    /**
     * Adds a list of strings to be potentially combined with the base string, along with a condition that determines
     * whether the list should be included in the combination process.
     *
     * @param list The list of strings to be added.
     * @param condition A lambda function that takes a List<String> and returns a Boolean. This condition
     * determines if the list should be included in the combination. The default condition always returns true,
     * meaning the list is always included.
     * @return Returns the current instance of [StringCombiner] to support fluent API style.
     */
    fun addList(list: List<String>, condition: Boolean = true): StringCombiner {
        lists.add(list)
        conditions.add(condition)
        return this
    }

    /**
     * Generates all possible combinations of the base string with the added lists of strings, considering their
     * respective conditions. Each list that meets its condition contributes its strings to the possible combinations,
     * prefixed by the base string and separated by colons.
     *
     * @return A list of strings, each representing a possible combination.
     */
    fun combine(): List<String> {
        return combine0(0, base)
    }

    // Private recursive function to handle the combination logic
    private fun combine0(index: Int, current: String): List<String> {
        if (index >= lists.size) {
            return listOf(current)
        }

        val currentList = lists[index]
        if (!conditions[index]) {
            return combine0(index + 1, current)
        }

        val result = mutableListOf<String>()
        for (element in currentList) {
            result += combine0(index + 1, "$current$split$element")
        }
        return result
    }
}

/**
 * The DSL builder of [StringCombiner].
 */
fun StringCombiner(base: String, split: String = ":", block: StringCombiner.() -> Unit): StringCombiner {
    return StringCombiner(base, split).apply(block)
}