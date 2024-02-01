package cc.mewcraft.wakame.random

import cc.mewcraft.wakame.item.Tang

/**
 * Represents metadata of a [sample][Sample].
 *
 * By design, every [sample][Sample] in a [pool][Pool] can **optionally**
 * have a [SampleMeta] attached.
 *
 * It could be used to, for example, fine-tune the selection of
 * [tang][Tang] if regular conditions can't achieve the goal.
 */
interface SampleMeta<T> {
    companion object Factory {
        /**
         * Creates a [SampleMeta] of [String] type.
         */
        fun of(value: String? = null): SampleMeta<String> {
            return ImmutableSampleMeta(value)
        }
    }

    /**
     * The value. Represents "meta not present" if `null`.
     *
     * Side Notes: currently, this is just a string. In the future, this may be
     * expanded to a more complex data structure to suit more complex logic.
     */
    val value: T?

    /**
     * Returns `true` if the value is empty.
     *
     * In this case [isPresent] returns `false`.
     */
    val isEmpty: Boolean
        get() = value == null

    /**
     * Returns`true` if the value is present.
     *
     * In this case [isEmpty] returns `false`.
     */
    val isPresent: Boolean
        get() = value != null

    // 实现必须重写默认的 equals 和 hashCode
    // 以保证 Set/Map 等数据结构的行为是正确的
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}
