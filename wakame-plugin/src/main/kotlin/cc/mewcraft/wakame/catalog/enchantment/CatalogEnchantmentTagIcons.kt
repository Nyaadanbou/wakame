package cc.mewcraft.wakame.catalog.enchantment

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.item.SlotDisplay
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.util.KoishKey
import cc.mewcraft.wakame.util.KoishKeys
import cc.mewcraft.wakame.util.configurate.yamlLoader
import kotlin.io.path.exists
import kotlin.io.path.readText

/**
 * 魔咒图鉴中模式 c (按物品类型查询) 的 Tag 图标配置.
 *
 * 从 `configs/catalog/enchantment/tag_icons.yml` 加载
 * `enchantable tag value → KoishKey(萌芽物品 ID)` 映射.
 */
@Init(InitStage.POST_WORLD)
internal object CatalogEnchantmentTagIcons {

    private val TAG_ICON_MAP: HashMap<String, KoishKey> = HashMap()

    /**
     * 获取指定 [tagValue] 对应的萌芽物品 ID.
     *
     * @param tagValue tag value 去掉 `enchantable/` 前缀后的部分, 如 `sword`, `mining`
     */
    fun getIcon(tagValue: String): KoishKey? {
        return TAG_ICON_MAP[tagValue]
    }

    /**
     * 获取指定 [tagValue] 对应的 [SlotDisplay], 无配置时使用默认图标.
     *
     * @param tagValue tag value 去掉 `enchantable/` 前缀后的部分, 如 `sword`, `mining`
     */
    fun getIconOrDefault(tagValue: String): SlotDisplay {
        val iconKey = TAG_ICON_MAP[tagValue]
        if (iconKey != null) {
            return SlotDisplay.get(iconKey)
        }
        return SlotDisplay.get(BuiltInRegistries.ITEM.defaultId)
    }

    @InitFun
    fun init() {
        loadTagIcons()
    }

    fun reload() {
        loadTagIcons()
    }

    private fun loadTagIcons() {
        TAG_ICON_MAP.clear()

        val file = KoishDataPaths.CONFIGS.resolve("catalog/enchantment/tag_icons.yml")
        if (!file.exists()) return

        try {
            val loader = yamlLoader { withDefaults() }
            val rootNode = loader.buildAndLoadString(file.readText())
            for ((key, child) in rootNode.childrenMap()) {
                val tagValue = key.toString()
                val iconId = child.string ?: continue
                TAG_ICON_MAP[tagValue] = KoishKeys.of(iconId)
            }
        } catch (e: Throwable) {
            LOGGER.error("Failed to load enchantment catalog tag icons", e)
        }

        LOGGER.info("Loaded ${TAG_ICON_MAP.size} enchantment catalog tag icons: ${TAG_ICON_MAP.keys}")
    }
}

