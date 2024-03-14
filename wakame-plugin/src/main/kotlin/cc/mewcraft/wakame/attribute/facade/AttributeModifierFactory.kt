package cc.mewcraft.wakame.attribute.facade

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import java.util.UUID

/**
 * A factory to create [AttributeModifier] from given [UUID] and
 * [PlainAttributeData].
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
    fun createAttributeModifiers(uuid: UUID, value: PlainAttributeData): Map<Attribute, AttributeModifier>
}