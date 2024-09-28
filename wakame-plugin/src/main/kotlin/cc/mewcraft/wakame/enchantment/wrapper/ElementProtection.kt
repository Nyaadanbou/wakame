package cc.mewcraft.wakame.enchantment.wrapper

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.enchantment.effects.EnchantmentEffect
import cc.mewcraft.wakame.item.ItemSlot
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment

internal class ElementProtection(
    handle: Enchantment,
    private val element: Element,
    private val baseValue: Double = -0.01,
    private val perLevelAboveFirst: Double = -0.01,
) : AbstractEnchantment(
    handle,
) {
    companion object {
        private const val ATTRIBUTE_MODIFIER_NAMESPACE = "enchantment"
    }

    private val attributeType = Attributes.element(element).INCOMING_DAMAGE_RATE
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