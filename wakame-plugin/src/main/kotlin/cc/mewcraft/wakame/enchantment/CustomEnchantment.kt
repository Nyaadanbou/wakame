package cc.mewcraft.wakame.enchantment

import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.enchantment.effects.EnchantmentEffect
import cc.mewcraft.wakame.item.ItemSlot
import org.bukkit.enchantments.Enchantment

/**
 * 封装了一个由萌芽数据包 新添加的/修改过的/移除掉的 魔咒.
 */
internal interface CustomEnchantment : Keyed {
    /**
     * 所封装的 [Enchantment] 实例.
     */
    val handle: Enchantment

    /**
     * 获取该魔咒指定 [level] 的 [cc.mewcraft.wakame.enchantment.effects.EnchantmentEffect].
     *
     * @param level 魔咒的等级
     * @param slot 魔咒的槽位, 例如来自头盔的魔咒, 那这个就是代表头盔的 [ItemSlot]
     */
    fun getEffects(level: Int, slot: ItemSlot): Collection<EnchantmentEffect>
}