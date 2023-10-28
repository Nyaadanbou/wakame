package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.attribute.Attribute

/**
 * An attribute container that is backed by a pool.
 */
class PoolAttributeContainer<T : Attribute> : AttributeContainer<T> {
    override fun generateAttribute(): T {
        TODO("Not yet implemented")
    }
}