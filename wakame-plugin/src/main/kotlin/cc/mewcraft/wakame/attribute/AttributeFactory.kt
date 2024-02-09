package cc.mewcraft.wakame.attribute

import java.util.UUID

/**
 * A factory to create related data of an [Attribute].
 *
 * @see AttributeFactoryRegistry
 */
fun interface AttributeFactory<T : BinaryAttributeValue> {
    /**
     * Creates one or more [AttributeModifiers][AttributeModifier] from the
     * given [value] with the given [uuid] being the modifiers' identifier. The
     * returned `map key` is a property from the singleton [Attributes] and
     * the `map value` is the attribute modifier associated with the map key.
     *
     * @throws IllegalArgumentException if this factory can't create such
     *     attribute modifiers from given values
     */
    fun createAttributeModifiers(uuid: UUID, value: T): Map<out Attribute, AttributeModifier>
}