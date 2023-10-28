package cc.mewcraft.wakame.index

import cc.mewcraft.wakame.primitive.Value

/**
 * An index, consisting of:
 *
 * - an identifying [key], used to reference this index
 * - a [value],
 */
class Index<T>(
    val key: String,
    val value: Value<T>
)