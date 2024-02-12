package cc.mewcraft.wakame.attribute.facade

import cc.mewcraft.wakame.attribute.base.Attribute
import cc.mewcraft.wakame.attribute.base.AttributeModifier
import java.util.UUID

/**
 * A factory to create [AttributeModifier] from given [UUID] and
 * [BinaryAttributeValue].
 */
fun interface AttributeModifierFactory {
    /**
     * Creates one or more [AttributeModifiers][AttributeModifier] from the
     * given [value] with the given [uuid] being the modifiers' identifier. The
     * returned `map key` is a property from the singleton [Attributes] and
     * the `map value` is the attribute modifier associated with the map key.
     *
     * @throws IllegalArgumentException if this factory can't create such
     *     attribute modifier(s) from given parameters
     */
    fun createAttributeModifiers(uuid: UUID, value: BinaryAttributeValue): Map<out Attribute, AttributeModifier>
}