package cc.mewcraft.wakame.catalog.enchantment

import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.configurate.yamlLoader
import net.kyori.adventure.text.Component
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText

/**
 * 魔咒图鉴的菜单布局设置加载器.
 *
 * 从 `configs/catalog/enchantment/layout/` 加载各菜单的 [BasicMenuSettings].
 */
@Init(InitStage.POST_WORLD)
internal object CatalogEnchantmentMenuSettings {

    private val MENU_SETTING_MAP: HashMap<String, BasicMenuSettings> = HashMap()

    fun getMenuSettings(configKey: String): BasicMenuSettings {
        return MENU_SETTING_MAP[configKey] ?: run {
            LOGGER.warn("Enchantment catalog menu settings '$configKey' not found, using default")
            BasicMenuSettings(Component.text("Menu settings '$configKey' not found"), arrayOf(), hashMapOf())
        }
    }

    @InitFun
    fun init() {
        loadMenuSettings()
    }

    fun reload() {
        loadMenuSettings()
    }

    private fun loadMenuSettings() {
        val loader = yamlLoader { withDefaults() }
        MENU_SETTING_MAP.clear()

        val layoutDir = KoishDataPaths.CONFIGS.resolve("catalog/enchantment/layout/")
        if (!layoutDir.exists()) return

        // 加载 layout/*.yml, 以文件名(不含后缀)为 key
        layoutDir.listDirectoryEntries("*.yml").forEach { file ->
            try {
                val key = file.nameWithoutExtension
                val rootNode = loader.buildAndLoadString(file.readText())
                MENU_SETTING_MAP[key] = rootNode.require<BasicMenuSettings>()
            } catch (e: Throwable) {
                LOGGER.error("Failed to load enchantment catalog menu settings from '${file.nameWithoutExtension}'", e)
            }
        }

        LOGGER.info("Loaded ${MENU_SETTING_MAP.size} enchantment catalog menu settings: ${MENU_SETTING_MAP.keys}")
    }
}

