package cc.mewcraft.wakame.attribute

/**
 * An attribute that may present on items.
 */
interface ItemAttribute {
    /**
     * Converts this attribute to an [AttributeModifier].
     *
     * Throws [UnsupportedOperationException] if it is not possible to do so.
     */
    fun asAttributeModifier(): AttributeModifier
}