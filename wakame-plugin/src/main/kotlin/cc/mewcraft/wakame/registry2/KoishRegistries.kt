package cc.mewcraft.wakame.registry2

import cc.mewcraft.wakame.attribute.AttributeSupplier
import cc.mewcraft.wakame.attribute.bundle.ConstantAttributeBundle
import cc.mewcraft.wakame.attribute.bundle.VariableAttributeBundle
import cc.mewcraft.wakame.catalog.item.CatalogItemCategory
import cc.mewcraft.wakame.catalog.item.recipe.CatalogItemLootTableRecipe
import cc.mewcraft.wakame.entity.attribute.Attribute
import cc.mewcraft.wakame.entity.attribute.AttributeFacade
import cc.mewcraft.wakame.entity.attribute.ImaginaryAttributeMap
import cc.mewcraft.wakame.item.NekoItem

object KoishRegistries {
    private val ACCESS: MutableRegistryAccess = MutableRegistryAccess()

    ///

    /**
     * 属性的类型.
     */
    @JvmField
    val ATTRIBUTE: WritableRegistry<Attribute> = registerSimple(KoishRegistryKeys.ATTRIBUTE)

    /**
     * 实体的默认属性.
     *
     * 1. 包含所有原版实体(包括玩家)的默认属性.
     * 2. MythicMobs 生物的属性不由这里提供.
     *
     * ### Notes
     * Using [RegistryKey] to identify the "type" of living entities because we want the whole
     * attribute system to be compatible with 3rd party mob system such as MythicMobs,
     * in which case the enum type is not enough to express all types.
     */
    @JvmField
    val ATTRIBUTE_SUPPLIER: WritableRegistry<AttributeSupplier> = registerSimple(KoishRegistryKeys.ATTRIBUTE_SUPPLIER)

    /**
     * 属性块 [cc.mewcraft.wakame.attribute.bundle.AttributeBundle] 的外观, 用于访问属性块相关的数据和逻辑.
     */
    @JvmField
    val ATTRIBUTE_FACADE: WritableRegistry<AttributeFacade<ConstantAttributeBundle, VariableAttributeBundle>> = registerSimple(KoishRegistryKeys.ATTRIBUTE_BUNDLE_FACADE)

    /**
     * 虚构的属性映射.
     */
    @JvmField
    val IMAGINARY_ATTRIBUTE_MAP: WritableRegistry<ImaginaryAttributeMap> = registerSimple(KoishRegistryKeys.IMAGINARY_ATTRIBUTE_MAP)

    /**
     * 标准物品类型.
     *
     * 玩家可以直接获得/使用的物品类型.
     */
    @JvmField
    val ITEM: WritableDefaultedFuzzyRegistry<NekoItem> = registerDefaultedFuzzy(KoishRegistryKeys.ITEM, "internal:unknown")

    /**
     * 物品图鉴中物品的类别.
     */
    @JvmField
    val ITEM_CATEGORY: WritableRegistry<CatalogItemCategory> = registerSimple(KoishRegistryKeys.ITEM_CATEGORY)

    /**
     * 物品图鉴中的战利品表配方.
     */
    @JvmField
    val LOOT_TABLE_RECIPE: WritableRegistry<CatalogItemLootTableRecipe> = registerSimple(KoishRegistryKeys.LOOT_TABLE_RECIPE)

    ///

    fun resetRegistries() {
        ACCESS.resetRegistries()
    }

    /**
     * 创建一个 [WritableRegistry].
     */
    private fun <T> registerSimple(key: RegistryKey<out Registry<T>>, initializer: (Registry<T>) -> Unit = {}): WritableRegistry<T> {
        return ACCESS.add(key, SimpleRegistry(key).apply(initializer))
    }

    /**
     * 创建一个 [WritableDefaultedRegistry].
     */
    private fun <T> registerDefaulted(key: RegistryKey<out Registry<T>>, defaultId: String, initializer: (Registry<T>) -> Unit = {}): WritableDefaultedRegistry<T> {
        return ACCESS.add(key, SimpleDefaultedRegistry(defaultId, key).apply(initializer)) as WritableDefaultedRegistry<T>
    }

    /**
     * 创建一个 [WritableDefaultedFuzzyRegistry].
     */
    private fun <T> registerDefaultedFuzzy(key: RegistryKey<out Registry<T>>, defaultId: String, initializer: (Registry<T>) -> Unit = {}): WritableDefaultedFuzzyRegistry<T> {
        return ACCESS.add(key, SimpleDefaultedFuzzyRegistry(defaultId, key).apply(initializer)) as WritableDefaultedFuzzyRegistry<T>
    }
}