package cc.mewcraft.wakame.registry2

import cc.mewcraft.wakame.attribute.AttributeSupplier
import cc.mewcraft.wakame.attribute.bundle.ConstantAttributeBundle
import cc.mewcraft.wakame.attribute.bundle.VariableAttributeBundle
import cc.mewcraft.wakame.catalog.item.CatalogItemCategory
import cc.mewcraft.wakame.catalog.item.recipe.CatalogItemLootTableRecipe
import cc.mewcraft.wakame.entity.attribute.AttributeFacade
import cc.mewcraft.wakame.entity.attribute.ImaginaryAttributeMap
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.util.Identifiers

object KoishRegistryKeys {
    @JvmField
    val ROOT_REGISTRY_NAME = Identifiers.of("root")

    ///

    @JvmField
    val ATTRIBUTE_SUPPLIER = createRegistryKey<AttributeSupplier>("attribute_supplier")

    @JvmField
    val ATTRIBUTE_FACADE = createRegistryKey<AttributeFacade<ConstantAttributeBundle, VariableAttributeBundle>>("attribute_facade")

    @JvmField
    val IMG_ATTRIBUTE_MAP = createRegistryKey<ImaginaryAttributeMap>("img_attribute_map")

    @JvmField
    val ITEM = createRegistryKey<NekoItem>("item")

    @JvmField
    val ITEM_CATEGORY = createRegistryKey<CatalogItemCategory>("item_category")

    @JvmField
    val LOOT_TABLE_RECIPE = createRegistryKey<CatalogItemLootTableRecipe>("loot_table_recipe")

    ///

    private fun <T> createRegistryKey(name: String): RegistryKey<out Registry<T>> {
        return RegistryKey.of(ROOT_REGISTRY_NAME, Identifiers.of(name))
    }
}