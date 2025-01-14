package cc.mewcraft.wakame.enchantment.wrapper.implementation

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.enchantment.Enchantments
import cc.mewcraft.wakame.enchantment.effects.EnchantmentEffect
import cc.mewcraft.wakame.enchantment.wrapper.AbstractEnchantment
import cc.mewcraft.wakame.enchantment.wrapper.EnchantmentAttributeComponent
import cc.mewcraft.wakame.enchantment.wrapper.EnchantmentAttributeComponent.Part
import cc.mewcraft.wakame.item.ItemSlot

internal class SweepingEdge : AbstractEnchantment(Enchantments.SWEEPING_EDGE) {
    private val component1: EnchantmentAttributeComponent = EnchantmentAttributeComponent(this, setOf(Part(Attributes.SWEEPING_DAMAGE_RATIO, 0.25, 0.15, AttributeModifier.Operation.ADD)))

    override fun getEffects(level: Int, slot: ItemSlot): Collection<EnchantmentEffect> {
        return component1.getEffects(level, slot)
    }
}