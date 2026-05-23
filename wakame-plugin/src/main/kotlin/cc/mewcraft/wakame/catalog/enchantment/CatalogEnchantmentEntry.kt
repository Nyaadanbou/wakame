package cc.mewcraft.wakame.catalog.enchantment

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.util.configurate.yamlLoader
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.set.RegistryKeySet
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType
import kotlin.io.path.exists
import kotlin.io.path.readText

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

    /**
     * 支持的物品集合.
     */
    val supportedItems: RegistryKeySet<ItemType>
        get() = enchantment.supportedItems

    /**
     * 主要的物品集合, 可能为空.
     */
    val primaryItems: RegistryKeySet<ItemType>?
        get() = enchantment.primaryItems

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

    companion object {
        /**
         * 从注册表和配置文件构建全部 [CatalogEnchantmentEntry].
         *
         * 该方法会:
         * 1. 从 `configs/catalog/enchantment/descriptions.yml` 加载魔咒效果描述
         * 2. 遍历魔咒注册表, 为每个魔咒创建 [CatalogEnchantmentEntry]
         */
        fun buildAll(): List<CatalogEnchantmentEntry> {
            val descriptions = loadDescriptions()
            val enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
            return enchantmentRegistry.stream().map { enchantment ->
                val key = enchantment.key().toString()
                CatalogEnchantmentEntry(enchantment, descriptions[key])
            }.toList()
        }

        /**
         * 从 `configs/catalog/enchantment/descriptions.yml` 加载魔咒效果描述.
         *
         * YAML 格式:
         * ```yaml
         * "koish:veinminer": "<gray>连锁采掘同种矿物方块"
         * "minecraft:sharpness": "<gray>增加近战攻击伤害"
         * ```
         */
        private fun loadDescriptions(): Map<String, Component> {
            val file = KoishDataPaths.CONFIGS.resolve("catalog/enchantment/descriptions.yml")
            if (!file.exists()) return emptyMap()

            val result = HashMap<String, Component>()
            try {
                val loader = yamlLoader { withDefaults() }
                val rootNode = loader.buildAndLoadString(file.readText())
                for ((key, child) in rootNode.childrenMap()) {
                    val enchantmentKey = key.toString()
                    val description = child.get(Component::class.java) ?: continue
                    result[enchantmentKey] = description
                }
            } catch (e: Throwable) {
                LOGGER.error("Failed to load enchantment catalog descriptions", e)
            }

            LOGGER.info("Loaded ${result.size} enchantment descriptions")
            return result
        }
    }
}
