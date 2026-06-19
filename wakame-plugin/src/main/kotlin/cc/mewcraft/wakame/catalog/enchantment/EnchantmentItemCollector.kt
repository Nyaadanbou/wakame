package cc.mewcraft.wakame.catalog.enchantment

import org.bukkit.enchantments.Enchantment

/**
 * 收集附魔所关联的物品, 合并原版 [org.bukkit.inventory.ItemType] 和 Koish 物品.
 *
 * Koish 物品部分由 [EnchantmentItemIndex] 预先反向索引提供,
 * 避免逐个附魔全表扫描物品注册表.
 */
class EnchantmentItemCollector(
    private val enchantment: Enchantment,
    private val index: EnchantmentItemIndex,
) {
    val supportedItems: Collection<EnchantmentItemResolver> by lazy { computeSupportedItems() }

    val primaryItems: Collection<EnchantmentItemResolver> by lazy { computePrimaryItems() }

    private fun computeSupportedItems(): Collection<EnchantmentItemResolver> {
        val merged = LinkedHashSet<EnchantmentItemResolver>()
        enchantment.supportedItems.forEach { key ->
            merged.add(EnchantmentItemResolver(key))
        }
        index.supportedItemsOf(enchantment).forEach { id ->
            merged.add(EnchantmentItemResolver(id))
        }
        return merged
    }

    private fun computePrimaryItems(): Collection<EnchantmentItemResolver> {
        val merged = LinkedHashSet<EnchantmentItemResolver>()
        enchantment.primaryItems?.forEach { key ->
            merged.add(EnchantmentItemResolver(key))
        }
        index.primaryItemsOf(enchantment).forEach { id ->
            merged.add(EnchantmentItemResolver(id))
        }
        return merged
    }
}
