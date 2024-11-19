package cc.mewcraft.wakame.enchantment.wrapper.implementation

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.enchantment.effects.EnchantmentEffect
import cc.mewcraft.wakame.enchantment.wrapper.ElementEnchantment
import cc.mewcraft.wakame.enchantment.wrapper.EnchantmentAttributeComponent
import cc.mewcraft.wakame.enchantment.wrapper.EnchantmentAttributeComponent.*
import cc.mewcraft.wakame.item.ItemSlot
import xyz.xenondevs.commons.provider.immutable.map

internal class ElementPenetration(
    elementId: String,
) : ElementEnchantment(
    Identity(elementId, "penetrate"),
) {
    private val defensePenetrationAdd: EnchantmentAttributeComponent by element
        .map { element -> Attributes.DEFENSE_PENETRATION.of(element) }
        .map { attribute ->
            EnchantmentAttributeComponent(
                this, setOf(
                    Part(attribute, 5.0, 3.0, AttributeModifier.Operation.ADD)
                )
            )
        }
    private val defensePenetrationRateAdd: EnchantmentAttributeComponent by element
        .map { element -> Attributes.DEFENSE_PENETRATION_RATE.of(element) }
        .map { attribute ->
            EnchantmentAttributeComponent(
                this, setOf(
                    Part(attribute, 0.0, 0.05, AttributeModifier.Operation.ADD)
                )
            )
        }

    override fun getEffects(level: Int, slot: ItemSlot): Collection<EnchantmentEffect> {
        return defensePenetrationAdd.getEffects(level, slot) +
                defensePenetrationRateAdd.getEffects(level, slot)
    }
}