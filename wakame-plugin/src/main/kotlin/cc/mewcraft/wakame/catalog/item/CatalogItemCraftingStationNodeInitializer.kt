package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.catalog.item.node.CatalogItemCraftingStationNode
import cc.mewcraft.wakame.craftingstation.station.CraftingStationRegistry
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry.DynamicRegistries
import cc.mewcraft.wakame.registry.RegistryLoader
import cc.mewcraft.wakame.util.KoishKey

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
        var count = 0
        for (stationId in CraftingStationRegistry.NAMES) {
            val station = CraftingStationRegistry.getStation(stationId) ?: continue
            val catalogMenuSettings = station.catalogMenuSettings

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