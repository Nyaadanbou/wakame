package cc.mewcraft.wakame.catalog.enchantment

import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.util.KoishKey
import org.bukkit.enchantments.Enchantment

/**
 * 附魔到 Koish 物品的反向索引.
 *
 * 一次性遍历整个物品注册表, 建立 `附魔 → 声明了该附魔的 Koish 物品 ID` 映射,
 * 供 [EnchantmentItemCollector] O(1) 查询, 避免每个附魔各自全表扫描物品注册表.
 */
class EnchantmentItemIndex private constructor(
    private val supported: Map<KoishKey, List<KoishKey>>,
    private val primary: Map<KoishKey, List<KoishKey>>,
) {
    /**
     * 声明 [enchantment] 为 supported 的全部 Koish 物品 ID.
     */
    fun supportedItemsOf(enchantment: Enchantment): List<KoishKey> {
        return supported[enchantment.key()] ?: emptyList()
    }

    /**
     * 声明 [enchantment] 为 primary 的全部 Koish 物品 ID.
     */
    fun primaryItemsOf(enchantment: Enchantment): List<KoishKey> {
        return primary[enchantment.key()] ?: emptyList()
    }

    companion object {
        /**
         * 遍历 [BuiltInRegistries.ITEM] 一次, 构建反向索引.
         */
        fun build(): EnchantmentItemIndex {
            val supported = HashMap<KoishKey, MutableList<KoishKey>>()
            val primary = HashMap<KoishKey, MutableList<KoishKey>>()
            for (item in BuiltInRegistries.ITEM) {
                item.properties[ItemPropTypes.SUPPORTED_ENCHANTMENTS]?.forEach { enchantment ->
                    supported.getOrPut(enchantment.key()) { mutableListOf() }.add(item.id)
                }
                item.properties[ItemPropTypes.PRIMARY_ENCHANTMENTS]?.forEach { enchantment ->
                    primary.getOrPut(enchantment.key()) { mutableListOf() }.add(item.id)
                }
            }
            return EnchantmentItemIndex(supported, primary)
        }
    }
}
