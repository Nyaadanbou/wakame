package cc.mewcraft.wakame.random

internal class ImmutableSampleMeta<T>(
    override val value: T?,
) : SampleMeta<T> {
    override fun equals(other: Any?): Boolean {
        if (other !is SampleMeta<*>)
            return false
        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}