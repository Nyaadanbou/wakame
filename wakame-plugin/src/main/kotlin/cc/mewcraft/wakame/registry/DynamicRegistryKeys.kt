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
    val CATALOG_ITEM_RECIPE_TYPE = createRegistryKey<CatalogItemRecipeType>("catalog_item_recipe_type")

    @JvmField
    val CATALOG_ITEM_CRAFTING_STATION_RECIPE = createRegistryKey<CatalogItemCraftingStationNode>("catalog_item_crafting_station_recipe")

    @JvmField
    val CATALOG_ITEM_CRATE_RECIPE = createRegistryKey<CatalogItemCrateNode>("catalog_item_crate_recipe")

    @JvmField
    val CATALOG_ITEM_LOOT_TABLE_RECIPE = createRegistryKey<CatalogItemLootTableNode>("catalog_item_loot_table_recipe")

    @JvmField
    val CATALOG_ITEM_MYTHIC_DROP_RECIPE = createRegistryKey<CatalogItemMythicDropNode>("catalog_item_mythic_drop_recipe")

    @JvmField
    val CATALOG_ITEM_QUEST_RECIPE = createRegistryKey<CatalogItemQuestNode>("catalog_item_quest_recipe")

    @JvmField
    val CATALOG_ITEM_SIGNUP_RECIPE = createRegistryKey<CatalogItemSignupNode>("catalog_item_signup_recipe")

    @JvmField
    val CATALOG_ITEM_STANDARD_RECIPE = createRegistryKey<CatalogItemStandardNode>("catalog_item_standard_recipe")

    ///

    private fun <T> createRegistryKey(name: String): RegistryKey<out Registry<T>> {
        return RegistryKey.of(ROOT_REGISTRY_NAME, KoishKeys.of(name))
    }
}