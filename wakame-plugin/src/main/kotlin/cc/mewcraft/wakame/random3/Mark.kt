package cc.mewcraft.wakame.random3

/**
 * Represents arbitrary information attached to a [sample][Sample].
 * The information is intended to be "left", by the sample, in the
 * [SelectionContext] when it is picked out of a [pool][Pool].
 *
 * By design, every [sample][Sample] in a [pool][Pool] can optionally have
 * a [mark][Mark] attached. In the case where the sample has no mark, the
 * mark is simply `null`.
 *
 * It could be used as general information to implement a selection filter.
 */
interface Mark<T> {
    /**
     * The value wrapped in `this` mark.
     *
     * Side Notes: currently, this is just a string. In the future, this may be
     * expanded to a more complex data structure to suit more complex logic.
     */
    val value: T
}

/**
 * A simple [Mark] backed by a [String].
 */
data class StringMark(
    override val value: String,
) : Mark<String> {
    override fun equals(other: Any?): Boolean {
        if (other !is Mark<*>)
            return false
        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}