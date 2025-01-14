package cc.mewcraft.wakame.enchantment.wrapper.implementation

import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.enchantment.effects.EnchantmentEffect
import cc.mewcraft.wakame.enchantment.wrapper.ElementEnchantment
import cc.mewcraft.wakame.enchantment.wrapper.EnchantmentAttributeComponent
import cc.mewcraft.wakame.enchantment.wrapper.EnchantmentAttributeComponent.Part
import cc.mewcraft.wakame.item.ItemSlot

internal class ElementProtection(
    elementId: String,
) : ElementEnchantment(
    Identity(elementId, "protection")
) {
    private val component1: EnchantmentAttributeComponent = EnchantmentAttributeComponent(this, setOf(Part(Attributes.DEFENSE.of(elementId) ?: error("invalid element id: $elementId"), 3.0, 2.0, Operation.ADD)))
    private val component2: EnchantmentAttributeComponent = EnchantmentAttributeComponent(this, setOf(Part(Attributes.DEFENSE.of(elementId) ?: error("invalid element id: $elementId"), -0.01, -0.01, Operation.MULTIPLY_TOTAL)))

    override fun getEffects(level: Int, slot: ItemSlot): Collection<EnchantmentEffect> {
        return component1.getEffects(level, slot) + component2.getEffects(level, slot)
    }
}