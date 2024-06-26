package cc.mewcraft.wakame.random2

/**
 * A simple [Mark] backed by a [String].
 */
internal data class StringMark(
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