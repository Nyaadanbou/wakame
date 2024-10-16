package cc.mewcraft.wakame.enchantment.wrapper.implementation

import cc.mewcraft.commons.provider.immutable.map
import cc.mewcraft.wakame.attribute.AttributeModifier.*
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.attribute.ElementAttributes
import cc.mewcraft.wakame.enchantment.effects.EnchantmentEffect
import cc.mewcraft.wakame.enchantment.wrapper.*
import cc.mewcraft.wakame.enchantment.wrapper.EnchantmentAttributeComponent.*
import cc.mewcraft.wakame.item.ItemSlot

internal class ElementDamage(
    elementId: String,
) : ElementEnchantment(
    ElementEnchantmentId(elementId, "damage")
) {
    private val component1 by elementProvider
        .map(Attributes::element)
        .map(ElementAttributes::ATTACK_DAMAGE_RATE)
        .map { attribute ->
            EnchantmentAttributeComponent(
                handle, setOf(Part(attribute, 0.03, 0.03, Operation.ADD))
            )
        }

    override fun getEffects(level: Int, slot: ItemSlot): Collection<EnchantmentEffect> {
        return component1.getEffects(level, slot)
    }
}