package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.configurate.yamlLoader
import net.kyori.adventure.text.Component
import kotlin.io.path.*

/**
 * 不涉及物品图鉴中注册内容的初始化代码, 主要用于载入物品图鉴的一些全局设置/菜单布局.
 */
@Init(InitStage.POST_WORLD)
internal object CatalogItemMenuSettings {

    private val MENU_SETTING_MAP: HashMap<String, BasicMenuSettings> = HashMap()

    fun getMenuSettings(configKey: String): BasicMenuSettings {
        return MENU_SETTING_MAP[configKey] ?: run {
            LOGGER.warn("Menu settings '$configKey' not found, using default")
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

        val layoutDir = KoishDataPaths.CONFIGS.resolve("catalog/item/layout/")

        // 1) 加载 base 菜单布局: layout/base/*.yml
        //    每个文件是一个 BasicMenuSettings, 以文件名(不含后缀)作为 key
        val baseDir = layoutDir.resolve("base/")
        if (baseDir.exists()) {
            baseDir.listDirectoryEntries("*.yml").forEach { file ->
                try {
                    val key = file.nameWithoutExtension
                    val rootNode = loader.buildAndLoadString(file.readText())
                    MENU_SETTING_MAP[key] = rootNode.require<BasicMenuSettings>()
                } catch (e: Throwable) {
                    LOGGER.error("Failed to load base menu settings from '${file.name}'", e)
                }
            }
        }

        // 2) 加载 category 菜单布局: layout/category/*.yml
        //    每个文件是一个 BasicMenuSettings, 以 "category/<文件名>" 作为 key
        val categoryDir = layoutDir.resolve("category/")
        if (categoryDir.exists()) {
            categoryDir.listDirectoryEntries("*.yml").forEach { file ->
                try {
                    val key = "category/${file.nameWithoutExtension}"
                    val rootNode = loader.buildAndLoadString(file.readText())
                    MENU_SETTING_MAP[key] = rootNode.require<BasicMenuSettings>()
                } catch (e: Throwable) {
                    LOGGER.error("Failed to load category menu settings from '${file.name}'", e)
                }
            }
        }

        // 3) 加载 node 菜单布局: layout/node/**/menus.yml
        //    每个 menus.yml 是一个 map, key 是菜单的唯一标识, value 是 BasicMenuSettings
        val nodeDir = layoutDir.resolve("node/")
        if (nodeDir.exists()) {
            nodeDir.walk()
                .filter { it.name == "menus.yml" }
                .forEach { file ->
                    try {
                        val rootNode = loader.buildAndLoadString(file.readText())
                        for ((nodeKey, mapChild) in rootNode.childrenMap()) {
                            MENU_SETTING_MAP[nodeKey.toString()] = mapChild.require<BasicMenuSettings>()
                        }
                    } catch (e: Throwable) {
                        LOGGER.error("Failed to load node menu settings from '${file.relativeTo(layoutDir)}'", e)
                    }
                }
        }

        LOGGER.info("Loaded ${MENU_SETTING_MAP.size} menu settings: ${MENU_SETTING_MAP.keys}")
    }
}