package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.catalog.item.CatalogItemCategory
import cc.mewcraft.wakame.catalog.item.node.*
import cc.mewcraft.wakame.util.KoishKeys

object DynamicRegistryKeys {
    @JvmField
    val ROOT_REGISTRY_NAME = KoishKeys.of("dynamic")

    ///

    @JvmField
    val CATALOG_ITEM_CATEGORY = createRegistryKey<CatalogItemCategory>("catalog_item_category")

    @JvmField
    val CATALOG_ITEM_NODE_TYPE = createRegistryKey<CatalogItemNodeType>("catalog_item_node_type")

    @JvmField
    val CATALOG_ITEM_CRAFTING_STATION_NODE = createRegistryKey<CatalogItemCraftingStationNode>("catalog_item_crafting_station_node")

    @JvmField
    val CATALOG_ITEM_MINECRAFT_LOOT_TABLE_NODE = createRegistryKey<CatalogItemLootTableNode>("catalog_item_minecraft_loot_table_node")

    @JvmField
    val CATALOG_ITEM_MINECRAFT_RECIPE_NODE = createRegistryKey<CatalogItemRecipeNode>("catalog_item_minecraft_recipe_node")

    @JvmField
    val CATALOG_ITEM_SINGLE_SOURCE_NODE = createRegistryKey<CatalogItemSingleSourceNode>("catalog_item_single_source_node")

    ///

    private fun <T> createRegistryKey(name: String): RegistryKey<out Registry<T>> {
        return RegistryKey.of(ROOT_REGISTRY_NAME, KoishKeys.of(name))
    }
}