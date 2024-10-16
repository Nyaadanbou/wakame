package cc.mewcraft.wakame.random3

/**
 * Represents arbitrary information attached to a [Sample].
 *
 * By design, every [Sample] in a [Pool] can optionally have
 * a [Mark] attached. In the case where the sample has no mark,
 * the mark is simply `null`.
 *
 * It could be used to implement a general filter.
 */
data class Mark(
    /**
     * The value wrapped in `this` mark.
     */
    val value: String
)
