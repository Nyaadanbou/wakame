package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.require
import net.kyori.adventure.text.Component
import kotlin.io.path.readText

/**
 * 不涉及物品图鉴中注册内容的初始化代码, 主要用于载入物品图鉴的一些全局设置/菜单布局.
 */
@Init(stage = InitStage.POST_WORLD)
@Reload
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

    @ReloadFun
    fun reload() {
        loadMenuSettings()
    }

    private fun loadMenuSettings() {
        val loader = buildYamlConfigLoader { withDefaults() }
        val rootNode = loader.buildAndLoadString(KoishDataPaths.CONFIGS.resolve("catalog/item/menu_settings.yml").readText())
        MENU_SETTING_MAP.clear()
        MENU_SETTING_MAP.putAll(
            rootNode.childrenMap()
                .mapKeys { (nodeKey, _) -> nodeKey.toString() }
                .mapValues { (_, mapChild) -> mapChild.require<BasicMenuSettings>() }
        )
    }

}