package cc.mewcraft.wakame.enchantment.wrapper.implementation

import cc.mewcraft.wakame.attribute.AttributeModifier.*
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.enchantment.Enchantments
import cc.mewcraft.wakame.enchantment.effects.EnchantmentEffect
import cc.mewcraft.wakame.enchantment.wrapper.AbstractEnchantment
import cc.mewcraft.wakame.enchantment.wrapper.EnchantmentAttributeComponent
import cc.mewcraft.wakame.item.ItemSlot

internal class Agility : AbstractEnchantment(Enchantments.AGILITY) {
    private val attribute: EnchantmentAttributeComponent = EnchantmentAttributeComponent(
        this, setOf(
            EnchantmentAttributeComponent.Part(Attributes.MOVEMENT_SPEED, 0.05, 0.05, Operation.MULTIPLY_TOTAL)
        )
    )

    override fun getEffects(level: Int, slot: ItemSlot): Collection<EnchantmentEffect> {
        return attribute.getEffects(level, slot)
    }
}