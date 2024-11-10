package cc.mewcraft.wakame.enchantment.wrapper.implementation

import cc.mewcraft.wakame.attribute.AttributeModifier.*
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.enchantment.effects.EnchantmentEffect
import cc.mewcraft.wakame.enchantment.wrapper.ElementEnchantment
import cc.mewcraft.wakame.enchantment.wrapper.EnchantmentAttributeComponent
import cc.mewcraft.wakame.enchantment.wrapper.EnchantmentAttributeComponent.*
import cc.mewcraft.wakame.item.ItemSlot
import xyz.xenondevs.commons.provider.immutable.map

internal class ElementProtection(
    elementId: String,
) : ElementEnchantment(
    Identity(elementId, "protection")
) {
    private val attribute: EnchantmentAttributeComponent by element
        .map { Attributes.DEFENSE.of(elementId) ?: error("invalid element id: $elementId") }
        .map { attribute ->
            EnchantmentAttributeComponent(
                this, setOf(
                    Part(attribute, -0.01, -0.01, Operation.MULTIPLY_TOTAL)
                )
            )
        }

    override fun getEffects(level: Int, slot: ItemSlot): Collection<EnchantmentEffect> {
        return attribute.getEffects(level, slot)
    }
}