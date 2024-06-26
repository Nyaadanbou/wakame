package cc.mewcraft.wakame.random2

import me.lucko.helper.random.RandomSelector

/**
 * Represents a filter testing whether a sample should be included upon
 * constructing a [RandomSelector]. For example, if the filter of `X` is
 * not met, then `X` will even not be present in the [RandomSelector].
 *
 * We primarily use it to implement the "all or nothing" feature, where a
 * type of something may be present, **or not at all**, in the final output.
 */
interface Filter<in C> {
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

    companion object {
        /**
         * A filter that always return `true`.
         */
        fun <C> alwaysTrue(): Filter<C> = ALWAYS_TRUE

        /**
         * A filter that always return `false`.
         */
        fun <C> alwaysFalse(): Filter<C> = ALWAYS_FALSE

        private val ALWAYS_TRUE: Filter<Any?> = object : Filter<Any?> {
            override val invert: Boolean = false
            override fun testOriginal(context: Any?): Boolean = true
        }
        private val ALWAYS_FALSE: Filter<Any?> = object : Filter<Any?> {
            override val invert: Boolean = false
            override fun testOriginal(context: Any?): Boolean = false
        }
    }
}