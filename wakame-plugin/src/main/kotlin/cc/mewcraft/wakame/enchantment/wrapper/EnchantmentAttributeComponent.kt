package cc.mewcraft.wakame.enchantment.wrapper

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.enchantment.effects.EnchantmentEffect
import cc.mewcraft.wakame.enchantment.effects.implementation.AttributeEnchantmentEffect
import cc.mewcraft.wakame.item.ItemSlot
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment

internal data class EnchantmentAttributeComponent(
    private val handle: Enchantment,
    private val parts: Set<Part>,
) {

    companion object {
        private const val ATTRIBUTE_MODIFIER_NAMESPACE = "enchantment"
    }

    // (level, item_slot) -> effects
    private val cache = HashMap<Int, HashMap<ItemSlot, Collection<EnchantmentEffect>>>()

    /**
     * 获取指定等级和槽位的附魔效果.
     *
     * @param level 附魔等级
     * @param slot 物品槽位
     */
    fun getEffects(level: Int, slot: ItemSlot): Collection<EnchantmentEffect> {
        val effects = cache
            .getOrPut(level, ::HashMap)
            .getOrPut(slot) {
                parts.map { part ->
                    part.createEffect(handle.key.value(), level, slot)
                }.map { (attribute, modifier) ->
                    AttributeEnchantmentEffect(attribute, modifier)
                }
            }
        return effects
    }

    /**
     * 封装了一种单独的属性效果.
     * 如果说想让一个附魔提供3个属性,
     * 那么创建3个 [Part] 实例即可.
     *
     * @param attribute 属性
     * @param baseValue 基础值 (1级的数值)
     * @param perLevel 每级增加值 (2级开始每级增加的值)
     */
    data class Part(
        val attribute: Attribute,
        val baseValue: Double,
        val perLevel: Double,
        val operation: AttributeModifier.Operation,
    ) {
        /**
         * 用给定参数创建一个 [Attribute] 到 [AttributeModifier] 的映射.
         *
         * @param id 写附魔的 id
         * @param level 附魔等级
         * @param slot 物品槽位
         */
        fun createEffect(id: String, level: Int, slot: ItemSlot): Pair<Attribute, AttributeModifier> {
            val id2 = id + "/" + slot.slotIndex
            val amount = baseValue + perLevel * (level - 1)
            val modifier = AttributeModifier(Key.key(ATTRIBUTE_MODIFIER_NAMESPACE, id2), amount, operation)
            return attribute to modifier
        }

        override fun hashCode(): Int {
            return attribute.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Part) return false
            if (attribute != other.attribute) return false
            return true
        }
    }
}