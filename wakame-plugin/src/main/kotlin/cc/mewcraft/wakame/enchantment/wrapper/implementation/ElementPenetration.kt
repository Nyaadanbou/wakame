package cc.mewcraft.wakame.enchantment.wrapper.implementation

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.enchantment.effects.EnchantmentEffect
import cc.mewcraft.wakame.enchantment.wrapper.ElementEnchantment
import cc.mewcraft.wakame.enchantment.wrapper.EnchantmentAttributeComponent
import cc.mewcraft.wakame.enchantment.wrapper.EnchantmentAttributeComponent.Part
import cc.mewcraft.wakame.item.ItemSlot

internal class ElementPenetration(
    elementId: String,
) : ElementEnchantment(
    Identity(elementId, "penetration"),
) {
    private val component1: EnchantmentAttributeComponent = EnchantmentAttributeComponent(this, setOf(Part(Attributes.DEFENSE_PENETRATION.of(element), 5.0, 3.0, AttributeModifier.Operation.ADD)))
    private val component2: EnchantmentAttributeComponent = EnchantmentAttributeComponent(this, setOf(Part(Attributes.DEFENSE_PENETRATION_RATE.of(element), 0.0, 0.05, AttributeModifier.Operation.ADD)))

    override fun getEffects(level: Int, slot: ItemSlot): Collection<EnchantmentEffect> {
        return component1.getEffects(level, slot) + component2.getEffects(level, slot)
    }
}