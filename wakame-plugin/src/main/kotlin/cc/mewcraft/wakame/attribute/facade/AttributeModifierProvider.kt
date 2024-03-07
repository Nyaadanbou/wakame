package cc.mewcraft.wakame.attribute.facade

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import java.util.UUID

/**
 * Something that can provide [AttributeModifiers][AttributeModifier].
 *
 * The implementation should own the necessary values used to create the
 * [AttributeModifier]s. That's why the function [provideAttributeModifiers] only
 * accepts a [UUID].
 *
 * @see AttributeModifierFactory
 */
interface AttributeModifierProvider {
    /**
     * Provides one or more [AttributeModifiers][AttributeModifier] from this
     * object with the given [uuid] being the modifiers' identifier. The
     * returned `map key` is a property from the singleton [Attributes] and
     * the `map value` is the attribute modifier associated with the map key.
     *
     * @throws IllegalArgumentException if this provider can't provide such
     *     attribute modifiers
     */
    fun provideAttributeModifiers(uuid: UUID): Map<out Attribute, AttributeModifier>
}