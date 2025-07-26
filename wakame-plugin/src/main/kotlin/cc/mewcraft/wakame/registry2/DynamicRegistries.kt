package cc.mewcraft.wakame.registry2

import cc.mewcraft.wakame.catalog.item.CatalogItemCategory
import cc.mewcraft.wakame.catalog.item.recipe.CatalogItemLootTableRecipe

/**
 * 这些 [Registry] 内的注册项是 *可变的* - 可以在游戏运行时添加或删除注册项.
 */
// TODO #373: actually make it dynamic
object DynamicRegistries {
    private val ACCESS: MutableRegistryAccess = MutableRegistryAccess()

    ///

    /**
     * 物品图鉴中物品的类别.
     */
    @JvmField
    val ITEM_CATEGORY: WritableRegistry<CatalogItemCategory> = registerSimple(DynamicRegistryKeys.ITEM_CATEGORY)

    /**
     * 物品图鉴中的战利品表配方.
     */
    @JvmField
    val LOOT_TABLE_RECIPE: WritableRegistry<CatalogItemLootTableRecipe> = registerSimple(DynamicRegistryKeys.LOOT_TABLE_RECIPE)

    ///

    fun resetRegistries() {
        ACCESS.resetRegistries()
    }

    /**
     * 创建一个 [WritableRegistry].
     */
    fun <T> registerSimple(key: RegistryKey<out Registry<T>>, initializer: (Registry<T>) -> Unit = {}): WritableRegistry<T> {
        return ACCESS.add(key, SimpleRegistry(key).apply(initializer))
    }

    /**
     * 创建一个 [WritableDefaultedRegistry].
     */
    fun <T> registerDefaulted(key: RegistryKey<out Registry<T>>, defaultId: String, initializer: (Registry<T>) -> Unit = {}): WritableDefaultedRegistry<T> {
        return ACCESS.add(key, SimpleDefaultedRegistry(defaultId, key).apply(initializer)) as WritableDefaultedRegistry<T>
    }

    /**
     * 创建一个 [WritableDefaultedFuzzyRegistry].
     */
    fun <T> registerDefaultedFuzzy(key: RegistryKey<out Registry<T>>, defaultId: String, initializer: (Registry<T>) -> Unit = {}): WritableDefaultedFuzzyRegistry<T> {
        return ACCESS.add(key, SimpleDefaultedFuzzyRegistry(defaultId, key).apply(initializer)) as WritableDefaultedFuzzyRegistry<T>
    }
}