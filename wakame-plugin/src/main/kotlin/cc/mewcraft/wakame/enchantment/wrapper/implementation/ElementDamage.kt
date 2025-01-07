@file:Suppress("PrivatePropertyName")

package cc.mewcraft.wakame.enchantment.wrapper.implementation

import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.enchantment.effects.EnchantmentEffect
import cc.mewcraft.wakame.enchantment.wrapper.ElementEnchantment
import cc.mewcraft.wakame.enchantment.wrapper.EnchantmentAttributeComponent
import cc.mewcraft.wakame.enchantment.wrapper.EnchantmentAttributeComponent.Part
import cc.mewcraft.wakame.item.ItemSlot

internal class ElementDamage(
    elementId: String,
) : ElementEnchantment(
    Identity(elementId, "damage")
) {
    private val component1: EnchantmentAttributeComponent = EnchantmentAttributeComponent(this, setOf(Part(Attributes.MIN_ATTACK_DAMAGE.of(element), 5.0, 1.0, Operation.ADD)))
    private val component2: EnchantmentAttributeComponent = EnchantmentAttributeComponent(this, setOf(Part(Attributes.MAX_ATTACK_DAMAGE.of(element), 5.0, 1.0, Operation.ADD)))
    private val component3: EnchantmentAttributeComponent = EnchantmentAttributeComponent(this, setOf(Part(Attributes.ATTACK_DAMAGE_RATE.of(element), 0.03, 0.03, Operation.ADD)))

    override fun getEffects(level: Int, slot: ItemSlot): Collection<EnchantmentEffect> {
        return component1.getEffects(level, slot) + component2.getEffects(level, slot) + component3.getEffects(level, slot)
    }
}