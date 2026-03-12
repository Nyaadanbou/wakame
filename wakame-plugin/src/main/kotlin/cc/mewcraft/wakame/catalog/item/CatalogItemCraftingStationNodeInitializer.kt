package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.catalog.item.node.CatalogItemCraftingStationNode
import cc.mewcraft.wakame.craftingstation.station.CraftingStationRegistry
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry.DynamicRegistries
import cc.mewcraft.wakame.registry.RegistryLoader
import cc.mewcraft.wakame.util.IdePauser
import cc.mewcraft.wakame.util.KoishKey
import cc.mewcraft.wakame.util.configurate.yamlLoader
import net.kyori.adventure.text.Component
import kotlin.io.path.name
import kotlin.io.path.readText

@Init(
    stage = InitStage.POST_WORLD,
    runAfter = [
        CatalogItemMenuSettings::class,
    ]
)
internal object CatalogItemCraftingStationNodeInitializer : RegistryLoader {

    @InitFun
    fun init() {
        DynamicRegistries.CATALOG_ITEM_CRAFTING_STATION_NODE.resetRegistry()
        applyDataToRegistry(DynamicRegistries.CATALOG_ITEM_CRAFTING_STATION_NODE::add)
    }

    fun reload() {
        applyDataToRegistry(DynamicRegistries.CATALOG_ITEM_CRAFTING_STATION_NODE::upsert)
    }

    private fun applyDataToRegistry(registryAction: (KoishKey, CatalogItemCraftingStationNode) -> Unit) {
        val mappingsFile = KoishDataPaths.CONFIGS.resolve("catalog/item/layout/node/crafting_station/mappings.yml")

        // 读取 stationId → menuKey 的映射
        val stationIdToMenuId = HashMap<String, String>()
        val regexCache = HashMap<String, Regex>()

        try {
            val loader = yamlLoader { withDefaults() }
            val rootNode = loader.buildAndLoadString(mappingsFile.readText())

            for ((nodeKey, node) in rootNode.node("menu_setting_mappings").childrenMap()) {
                val regex = regexCache.computeIfAbsent(nodeKey.toString(), ::Regex)
                for (stationId in CraftingStationRegistry.NAMES) {
                    if (stationId.matches(regex)) {
                        stationIdToMenuId.putIfAbsent(stationId, node.require<String>())
                    }
                }
            }
        } catch (e: Throwable) {
            IdePauser.pauseInIde(e)
            LOGGER.error("Failed to read catalog crafting station mappings from: '${mappingsFile.name}'", e)
        }

        // 遍历所有合成站及其配方, 注册节点
        var count = 0
        for (stationId in CraftingStationRegistry.NAMES) {
            val station = CraftingStationRegistry.getStation(stationId) ?: continue
            val menuKey = stationIdToMenuId[stationId]
            val catalogMenuSettings = menuKey?.let(CatalogItemMenuSettings::getMenuSettings)
                ?: BasicMenuSettings(Component.text("Untitled"), emptyArray(), hashMapOf())

            for (recipe in station) {
                try {
                    val node = CatalogItemCraftingStationNode(
                        stationId = stationId,
                        recipe = recipe,
                        catalogMenuSettings = catalogMenuSettings,
                    )
                    registryAction(
                        recipe.key,
                        node,
                    )
                    count++
                } catch (e: Throwable) {
                    LOGGER.error("Failed to register catalog crafting station node for recipe '${recipe.key}' in station '$stationId'", e)
                }
            }
        }
        LOGGER.info("Applied $count catalog item crafting station nodes to registry")
    }
}