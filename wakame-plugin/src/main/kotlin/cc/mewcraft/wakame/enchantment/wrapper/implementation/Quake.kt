package cc.mewcraft.wakame.enchantment.wrapper.implementation

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.enchantment.Enchantments
import cc.mewcraft.wakame.enchantment.effects.EnchantmentEffect
import cc.mewcraft.wakame.enchantment.wrapper.AbstractEnchantment
import cc.mewcraft.wakame.enchantment.wrapper.EnchantmentAttributeComponent
import cc.mewcraft.wakame.enchantment.wrapper.EnchantmentAttributeComponent.*
import cc.mewcraft.wakame.item.ItemSlot

internal class Quake : AbstractEnchantment(Enchantments.QUAKE) {
    private val hammerDamageRange: EnchantmentAttributeComponent = EnchantmentAttributeComponent(
        this, setOf(
            Part(
                Attributes.HAMMER_DAMAGE_RANGE, 0.5, 0.3, AttributeModifier.Operation.ADD
            )
        )
    )

    override fun getEffects(level: Int, slot: ItemSlot): Collection<EnchantmentEffect> {
        return hammerDamageRange.getEffects(level, slot)
    }
}