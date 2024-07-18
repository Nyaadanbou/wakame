package cc.mewcraft.wakame.random3

import cc.mewcraft.wakame.GenericKeys
import me.lucko.helper.random.RandomSelector
import net.kyori.adventure.key.Key

/**
 * Represents a filter testing whether a sample should be included upon
 * constructing a [RandomSelector]. For example, if the filter of `X` is
 * not met, then `X` will even not be present in the [RandomSelector].
 *
 * We primarily use it to implement the "all or nothing" feature, where a
 * type of something may be present, **or not at all**, in the final output.
 */
interface Filter<in C> {

    companion object {
        /**
         * A filter that always return `true`.
         */
        fun <C> alwaysTrue(): Filter<C> = AlwaysTrue

        /**
         * A filter that always return `false`.
         */
        fun <C> alwaysFalse(): Filter<C> = AlwaysFalse
    }

    /**
     * 类型标识. 每个 [Filter] 的实现必须保证该标识的唯一性.
     */
    val type: Key

    /**
     * Should this filter invert its original output?
     */
    val invert: Boolean

    /**
     * Returns the original test result before [invert] is applied.
     *
     * The semantics of the returned `boolean` is **implementation-defined**.
     */
    fun testOriginal(context: C): Boolean

    /**
     * Returns the test result after [invert] is applied.
     */
    fun test(context: C): Boolean {
        return testOriginal(context) xor invert
    }

    private object AlwaysTrue : Filter<Any?> {
        override val type: Key = GenericKeys.TRUE
        override val invert: Boolean = false
        override fun testOriginal(context: Any?): Boolean = true
    }

    private object AlwaysFalse : Filter<Any?> {
        override val type: Key = GenericKeys.FALSE
        override val invert: Boolean = false
        override fun testOriginal(context: Any?): Boolean = false
    }
}