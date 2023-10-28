package cc.mewcraft.wakame.attribute

import net.kyori.adventure.key.Key

/**
 * This is the most high-level interface.
 *
 * Represents an attribute, which can be a modifier, an element type, an item level, a skill...
 */
interface Attribute {
    /**
     * Attribute identifier, where
     * - `namespace` is uniformly `waka` for any attribute
     * - `value` is the unique identifier of this attribute
     */
    val attributeKey: Key
}