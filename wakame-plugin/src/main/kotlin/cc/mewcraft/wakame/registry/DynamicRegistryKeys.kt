package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.catalog.item.CatalogItemCategory
import cc.mewcraft.wakame.catalog.item.recipe.*
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
    val CATALOG_ITEM_CRAFTING_STATION_RECIPE = createRegistryKey<CatalogItemCraftingStationRecipe>("catalog_item_crafting_station_recipe")

    @JvmField
    val CATALOG_ITEM_CRATE_RECIPE = createRegistryKey<CatalogItemCrateRecipe>("catalog_item_crate_recipe")

    @JvmField
    val CATALOG_ITEM_LOOT_TABLE_RECIPE = createRegistryKey<CatalogItemLootTableRecipe>("catalog_item_loot_table_recipe")

    @JvmField
    val CATALOG_ITEM_MYTHIC_DROP_RECIPE = createRegistryKey<CatalogItemMythicDropRecipe>("catalog_item_mythic_drop_recipe")

    @JvmField
    val CATALOG_ITEM_QUEST_RECIPE = createRegistryKey<CatalogItemQuestRecipe>("catalog_item_quest_recipe")

    @JvmField
    val CATALOG_ITEM_SIGNUP_RECIPE = createRegistryKey<CatalogItemSignupRecipe>("catalog_item_signup_recipe")

    @JvmField
    val CATALOG_ITEM_STANDARD_RECIPE = createRegistryKey<CatalogItemStandardRecipe>("catalog_item_standard_recipe")

    ///

    private fun <T> createRegistryKey(name: String): RegistryKey<out Registry<T>> {
        return RegistryKey.of(ROOT_REGISTRY_NAME, KoishKeys.of(name))
    }
}