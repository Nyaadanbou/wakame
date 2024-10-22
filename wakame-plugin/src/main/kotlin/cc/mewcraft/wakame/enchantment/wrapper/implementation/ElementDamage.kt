package cc.mewcraft.wakame.enchantment.wrapper.implementation

import cc.mewcraft.wakame.attribute.AttributeModifier.*
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.attribute.ElementAttributes
import cc.mewcraft.wakame.enchantment.effects.EnchantmentEffect
import cc.mewcraft.wakame.enchantment.wrapper.ElementEnchantment
import cc.mewcraft.wakame.enchantment.wrapper.EnchantmentAttributeComponent
import cc.mewcraft.wakame.enchantment.wrapper.EnchantmentAttributeComponent.*
import cc.mewcraft.wakame.item.ItemSlot
import xyz.xenondevs.commons.provider.immutable.map

internal class ElementDamage(
    elementId: String,
) : ElementEnchantment(
    Identity(elementId, "damage")
) {
    private val attribute: EnchantmentAttributeComponent by element
        .map(Attributes::element)
        .map(ElementAttributes::ATTACK_DAMAGE_RATE)
        .map { attribute ->
            EnchantmentAttributeComponent(
                this, setOf(
                    Part(attribute, 0.03, 0.03, Operation.ADD)
                )
            )
        }

    override fun getEffects(level: Int, slot: ItemSlot): Collection<EnchantmentEffect> {
        return attribute.getEffects(level, slot)
    }
}