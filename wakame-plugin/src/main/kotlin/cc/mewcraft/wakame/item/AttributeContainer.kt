package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.attribute.Attribute

/**
 * A container that contains an attribute.
 */
interface AttributeContainer {
    /**
     * Generates an attribute from this container.
     * The result of this function is non-deterministic.
     */
    fun generateAttribute(): Attribute
}