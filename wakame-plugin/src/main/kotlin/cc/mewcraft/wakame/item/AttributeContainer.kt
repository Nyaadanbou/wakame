package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.attribute.Attribute

/**
 * An attribute container that can output an attribute.
 */
interface AttributeContainer<T : Attribute> {
    /**
     * Generates an attribute from this container.
     * The returned attribute is non-deterministic.
     */
    fun generateAttribute(): T
}