package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.attribute.Attribute

/**
 * A attribute container that only contains a single attribute.
 */
class SingleAttributeContainer<T : Attribute> : AttributeContainer<T> {
    override fun generateAttribute(): T {
        TODO("Not yet implemented")
    }
}