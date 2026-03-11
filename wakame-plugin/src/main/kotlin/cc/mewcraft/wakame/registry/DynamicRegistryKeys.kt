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
    val CATALOG_ITEM_CRATE_NODE = createRegistryKey<CatalogItemCrateNode>("catalog_item_crate_node")

    @JvmField
    val CATALOG_ITEM_LOOT_TABLE_NODE = createRegistryKey<CatalogItemLootTableNode>("catalog_item_loot_table_node")

    @JvmField
    val CATALOG_ITEM_MYTHIC_DROP_NODE = createRegistryKey<CatalogItemMythicDropNode>("catalog_item_mythic_drop_node")

    @JvmField
    val CATALOG_ITEM_QUEST_NODE = createRegistryKey<CatalogItemQuestNode>("catalog_item_quest_node")

    @JvmField
    val CATALOG_ITEM_SIGNUP_NODE = createRegistryKey<CatalogItemSignupNode>("catalog_item_signup_node")

    @JvmField
    val CATALOG_ITEM_RECIPE_NODE = createRegistryKey<CatalogItemRecipeNode>("catalog_item_recipe_node")

    ///

    private fun <T> createRegistryKey(name: String): RegistryKey<out Registry<T>> {
        return RegistryKey.of(ROOT_REGISTRY_NAME, KoishKeys.of(name))
    }
}