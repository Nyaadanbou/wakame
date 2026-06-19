package cc.mewcraft.wakame.catalog.enchantment

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

/**
 * 魔咒图鉴中一个魔咒的数据封装.
 *
 * 展示层通过该类获取魔咒在图鉴中所需的全部展示数据.
 */
class CatalogEnchantmentEntry(
    /**
     * 原始 Bukkit Enchantment 引用.
     */
    val enchantment: Enchantment,
    /**
     * 效果描述 (从配置文件读取, 可为空).
     */
    val description: Component?,
    /**
     * 附魔到 Koish 物品的反向索引, 用于解析 supported/primary 物品.
     */
    index: EnchantmentItemIndex,
) {
    /**
     * 魔咒的显示名称 (不含等级).
     */
    val displayName: Component
        get() = enchantment.description()

    /**
     * 最大附魔等级.
     */
    val maxLevel: Int
        get() = enchantment.maxLevel

    private val collector = EnchantmentItemCollector(enchantment, index)

    /**
     * 支持的物品集合, 合并原版 ItemType 和声明了该附魔的 Koish 物品.
     */
    val supportedItems: Collection<EnchantmentItemResolver>
        get() = collector.supportedItems

    /**
     * 主要的物品集合, 可能为空.
     */
    val primaryItems: Collection<EnchantmentItemResolver>
        get() = collector.primaryItems

    /**
     * 权重 (影响附魔台随机出现概率).
     */
    val weight: Int
        get() = enchantment.weight

    /**
     * 魔咒的 NamespacedKey.
     */
    val key: Key
        get() = enchantment.key()

    /**
     * 该魔咒是否可以附在指定物品上.
     */
    fun canEnchant(itemStack: ItemStack): Boolean {
        return enchantment.canEnchantItem(itemStack)
    }
}
