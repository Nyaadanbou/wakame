package cc.mewcraft.wakame.catalog.enchantment

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.inventory.ItemStack

private const val ENCHANTABLE_TAG_PREFIX = "enchantable/"

/**
 * 魔咒图鉴的核心初始化器.
 *
 * 负责:
 * 1. 构建全部 [CatalogEnchantmentEntry] (委托给 [CatalogEnchantmentEntry.buildAll])
 * 2. 发现全部 `enchantable/...` 物品标签
 * 3. 提供查询方法供展示层调用
 */
@Init(
    stage = InitStage.POST_WORLD,
    runAfter = [
        CatalogEnchantmentMenuSettings::class,
        CatalogEnchantmentTagIcons::class,
    ]
)
internal object CatalogEnchantmentInitializer {

    /** 全部魔咒条目缓存. */
    private var cachedEntries: List<CatalogEnchantmentEntry> = emptyList()

    /** 全部 `enchantable/...` 物品标签缓存. */
    private var cachedEnchantableTags: List<EnchantableTagEntry> = emptyList()

    /**
     * 获取全部魔咒条目.
     */
    fun allEntries(): List<CatalogEnchantmentEntry> {
        return cachedEntries
    }

    /**
     * 获取全部 `enchantable/...` 物品标签.
     */
    fun enchantableItemTags(): List<EnchantableTagEntry> {
        return cachedEnchantableTags
    }

    /**
     * 筛选 [CatalogEnchantmentEntry.supportedItems] 与该 tag 有交集的魔咒条目.
     */
    fun entriesForTag(tagEntry: EnchantableTagEntry): List<CatalogEnchantmentEntry> {
        val itemRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM)
        val tag = itemRegistry.getTag(tagEntry.fullTagKey)
        val tagItemKeys = tag.map { it.key() }.toSet()
        return cachedEntries.filter { entry ->
            val supportedKeys = entry.supportedItems.map { it.key() }.toSet()
            supportedKeys.any { it in tagItemKeys }
        }
    }

    /**
     * 筛选可以附在指定物品上的魔咒条目.
     */
    fun entriesForItem(itemStack: ItemStack): List<CatalogEnchantmentEntry> {
        return cachedEntries.filter { entry ->
            entry.canEnchant(itemStack)
        }
    }

    @InitFun
    fun init() {
        load()
    }

    fun reload() {
        load()
    }

    private fun load() {
        // 1) 构建全部 CatalogEnchantmentEntry
        cachedEntries = CatalogEnchantmentEntry.buildAll()

        // 2) 发现全部 enchantable/* 物品标签
        val itemRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM)
        cachedEnchantableTags = itemRegistry.tags
            .filter { tag -> tag.tagKey().key().value().startsWith(ENCHANTABLE_TAG_PREFIX) }
            .map { tag ->
                val tagValue = tag.tagKey().key().value().removePrefix(ENCHANTABLE_TAG_PREFIX)
                EnchantableTagEntry(tagValue, tag.tagKey())
            }
            .toList()

        LOGGER.info("Loaded ${cachedEntries.size} enchantment catalog entries, ${cachedEnchantableTags.size} enchantable item tags")
    }
}