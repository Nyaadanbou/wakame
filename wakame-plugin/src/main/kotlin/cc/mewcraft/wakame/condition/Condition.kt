package cc.mewcraft.wakame.condition

/**
 * Represents a condition evaluated to `true` or `false`.
 *
 * This is a high-level interface of various condition systems.
 *
 * @param C the required context for the condition to evaluate
 */
fun interface Condition<in C> {
    /**
     * Returns `true` if the condition is met in the [context].
     */
    fun test(context: C): Boolean

    companion object {
        /**
         * A condition that always return `true`.
         */
        fun <C> alwaysTrue(): Condition<C> = ALWAYS_TRUE

        /**
         * A condition that always return `false`.
         */
        fun <C> alwaysFalse(): Condition<C> = ALWAYS_FALSE

        private val ALWAYS_TRUE: Condition<Any?> = Condition { true }
        private val ALWAYS_FALSE: Condition<Any?> = Condition { false }
    }
}