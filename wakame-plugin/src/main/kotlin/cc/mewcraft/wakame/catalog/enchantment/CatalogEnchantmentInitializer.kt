package cc.mewcraft.wakame.catalog.enchantment

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.configurate.yamlLoader
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import kotlin.io.path.exists
import kotlin.io.path.readText

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
            entry.supportedItems.any { it.id in tagItemKeys }
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
        cachedEntries = buildEntries()

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

    /**
     * 从魔咒注册表和配置文件构建全部 [CatalogEnchantmentEntry].
     *
     * 先加载 `configs/catalog/enchantment/descriptions.yml` 的效果描述,
     * 再遍历魔咒注册表为每个魔咒注入对应描述.
     */
    private fun buildEntries(): List<CatalogEnchantmentEntry> {
        val descriptions = loadDescriptions()
        val itemIndex = EnchantmentItemIndex.build()
        val enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
        return enchantmentRegistry.stream().map { enchantment ->
            val key = enchantment.key().toString()
            CatalogEnchantmentEntry(enchantment, descriptions[key], itemIndex)
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