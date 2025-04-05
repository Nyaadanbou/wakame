package cc.mewcraft.wakame.registry2

import cc.mewcraft.wakame.catalog.item.CatalogItemCategory
import cc.mewcraft.wakame.catalog.item.recipe.CatalogItemLootTableRecipe
import cc.mewcraft.wakame.item.NekoItem

object KoishRegistries {
    private val ACCESS: MutableRegistryAccess = MutableRegistryAccess()

    ///

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