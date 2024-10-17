package cc.mewcraft.wakame.enchantment.wrapper.implementation

import cc.mewcraft.wakame.attribute.AttributeModifier.*
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.enchantment.Enchantments
import cc.mewcraft.wakame.enchantment.effects.EnchantmentEffect
import cc.mewcraft.wakame.enchantment.wrapper.AbstractEnchantment
import cc.mewcraft.wakame.enchantment.wrapper.EnchantmentAttributeComponent
import cc.mewcraft.wakame.item.ItemSlot

internal class DeepSearch : AbstractEnchantment(Enchantments.DEEP_SEARCH) {
    private val attribute: EnchantmentAttributeComponent = EnchantmentAttributeComponent(
        this, setOf(
            EnchantmentAttributeComponent.Part(Attributes.BLOCK_INTERACTION_RANGE, 1.0, 0.5, Operation.ADD)
        )
    )

    override fun getEffects(level: Int, slot: ItemSlot): Collection<EnchantmentEffect> {
        return attribute.getEffects(level, slot)
    }
}