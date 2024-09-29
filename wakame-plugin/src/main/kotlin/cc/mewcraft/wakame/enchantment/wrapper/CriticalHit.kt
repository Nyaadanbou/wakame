package cc.mewcraft.wakame.enchantment.wrapper

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.enchantment.Enchantments
import cc.mewcraft.wakame.enchantment.effects.EnchantmentEffect
import cc.mewcraft.wakame.item.ItemSlot
import net.kyori.adventure.key.Key

internal class CriticalHit : AbstractEnchantment(
    Enchantments.CRITICAL_HIT
) {
    companion object {
        private const val ATTRIBUTE_MODIFIER_NAMESPACE = "enchantment"
    }

    private val baseValue = 0.05
    private val perLevelAboveFirst = 0.05

    private val attributeType = Attributes.CRITICAL_STRIKE_CHANCE
    private val attributeModifierLookup = HashMap<Int, HashMap<ItemSlot, Map<Attribute, AttributeModifier>>>()

    override fun getEffects(level: Int, slot: ItemSlot): Collection<EnchantmentEffect> {
        val attributeModifiers = attributeModifierLookup
            .getOrPut(level, ::HashMap)
            .getOrPut(slot) {
                val id = handle.key.value() + "/" + slot.slotIndex
                val amount = baseValue + perLevelAboveFirst * (level - 1)
                val operation = AttributeModifier.Operation.ADD
                val modifier = AttributeModifier(
                    id = Key.key(ATTRIBUTE_MODIFIER_NAMESPACE, id),
                    amount = amount,
                    operation = operation
                )
                mapOf(
                    attributeType to modifier
                )
            }
        val attributeEffect = EnchantmentEffect.attribute(
            attributeModifiers
        )
        return listOf(
            attributeEffect
        )
    }
}