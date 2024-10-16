package cc.mewcraft.wakame.enchantment.wrapper.implementation

import cc.mewcraft.wakame.attribute.AttributeModifier.*
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.enchantment.Enchantments
import cc.mewcraft.wakame.enchantment.effects.EnchantmentEffect
import cc.mewcraft.wakame.enchantment.wrapper.AbstractEnchantment
import cc.mewcraft.wakame.enchantment.wrapper.EnchantmentAttributeComponent
import cc.mewcraft.wakame.enchantment.wrapper.EnchantmentAttributeComponent.*
import cc.mewcraft.wakame.item.ItemSlot

internal class CriticalHit : AbstractEnchantment(Enchantments.CRITICAL_HIT) {
    private val component1: EnchantmentAttributeComponent = EnchantmentAttributeComponent(
        handle,
        setOf(Part(Attributes.CRITICAL_STRIKE_CHANCE, 0.05, 0.05, Operation.ADD))
    )

    override fun getEffects(level: Int, slot: ItemSlot): Collection<EnchantmentEffect> {
        return component1.getEffects(level, slot)
    }
}