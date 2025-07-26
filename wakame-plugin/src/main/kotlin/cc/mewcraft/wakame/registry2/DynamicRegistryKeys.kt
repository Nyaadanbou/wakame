package cc.mewcraft.wakame.registry2

import cc.mewcraft.wakame.catalog.item.CatalogItemCategory
import cc.mewcraft.wakame.catalog.item.recipe.CatalogItemLootTableRecipe
import cc.mewcraft.wakame.util.Identifiers

object DynamicRegistryKeys {
    @JvmField
    val ROOT_REGISTRY_NAME = Identifiers.of("dynamic")

    ///

    @JvmField
    val ITEM_CATEGORY = createRegistryKey<CatalogItemCategory>("item_category")

    @JvmField
    val LOOT_TABLE_RECIPE = createRegistryKey<CatalogItemLootTableRecipe>("loot_table_recipe")

    ///

    fun <T> createRegistryKey(name: String): RegistryKey<out Registry<T>> {
        return RegistryKey.of(ROOT_REGISTRY_NAME, Identifiers.of(name))
    }
}