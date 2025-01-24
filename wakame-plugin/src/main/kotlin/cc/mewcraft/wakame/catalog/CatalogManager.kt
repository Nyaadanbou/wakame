package cc.mewcraft.wakame.catalog

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.krequire
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import java.io.File

/**
 * 不涉及图鉴中注册内容的初始化代码.
 * 主要用于载入图鉴的一些全局设置.
 */
@Init(
    stage = InitStage.POST_WORLD
)
@Reload
object CatalogManager : KoinComponent {
    private const val ITEM_CATALOG_CONFIG_PATH = "catalog/item/config.yml"

    lateinit var itemCatalogMainMenuSettings: BasicMenuSettings

    @InitFun
    private fun init() {
        loadItemCatalogConfig()
    }

    @ReloadFun
    private fun reload() {
        loadItemCatalogConfig()
    }

    fun loadItemCatalogConfig() {
        val rootNode = buildYamlConfigLoader {
            withDefaults()
            source { get<File>(named(PLUGIN_DATA_DIR)).resolve(ITEM_CATALOG_CONFIG_PATH).bufferedReader() }
        }.build().load()

        itemCatalogMainMenuSettings = rootNode.node("main_menu_settings").krequire<BasicMenuSettings>()
    }
}