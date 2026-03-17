package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.catalog.item.CatalogItemCategory
import cc.mewcraft.wakame.catalog.item.node.*

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
    val CATALOG_ITEM_CATEGORY: WritableRegistry<CatalogItemCategory> = registerSimple(DynamicRegistryKeys.CATALOG_ITEM_CATEGORY)

    // TODO #532: CatalogRecipeType:
    //  koish 合成站配方,
    //    程序读取 Koish 合成站的配方并自动构建 “来源&用途” 网络
    //  minecraft loot table,
    //    程序读取 Minecraft 的战利品表并自动构建 “来源&用途” 网络
    //  minecraft recipe,
    //    程序读取 Minecraft 的合成配方并自动构建 “来源&用途” 网络
    //    注意, 每一种类型的配方 (例如有序, 无序, 熔炉...) 都会构建出一个单独的 CatalogItemRecipeType
    //  single source,
    //    程序读取手动创建的单源节点并自动构建 “来源&用途” 网络
    /**
     * 物品图鉴中配方的类型. 例如 "合成配方" "熔炉配方" "锻造配方" 等等, 以及一些特殊的配方类型, 例如 "签到奖励" "任务奖励" "宝箱奖励" 等等.
     */
    @JvmField
    val CATALOG_ITEM_NODE_TYPE: WritableRegistry<CatalogItemNodeType> = registerSimple(DynamicRegistryKeys.CATALOG_ITEM_NODE_TYPE)

    /**
     * 物品图鉴中的 Koish 合成站配方节点.
     * 这个配方的输入是一系列物品, 输出是一个物品.
     */
    @JvmField
    val CATALOG_ITEM_CRAFTING_STATION_NODE: WritableRegistry<CatalogItemCraftingStationNode> = registerSimple(DynamicRegistryKeys.CATALOG_ITEM_CRAFTING_STATION_NODE)

    /**
     * 物品图鉴中的 Minecraft 战利品表节点.
     * 这个配方的输入是一个 Minecraft 战利品表, 输出 Minecraft 的战利品表展开后的物品.
     */
    @JvmField
    val CATALOG_ITEM_MINECRAFT_LOOT_TABLE_NODE: WritableRegistry<CatalogItemLootTableNode> = registerSimple(DynamicRegistryKeys.CATALOG_ITEM_MINECRAFT_LOOT_TABLE_NODE)

    /**
     * 物品图鉴中的 Minecraft 合成配方节点.
     * 这个配方的输入是一系列物品, 输出是一个物品.
     */
    @JvmField
    val CATALOG_ITEM_MINECRAFT_RECIPE_NODE: WritableRegistry<CatalogItemRecipeNode> = registerSimple(DynamicRegistryKeys.CATALOG_ITEM_MINECRAFT_RECIPE_NODE)

    /**
     * 物品图鉴中的单源节点.
     * 这个配方的输入是一个任意对象, 输出是一系列物品.
     * 专门用于灵活创建那些通过活动/任务/自定义生物掉落获得的物品.
     */
    @JvmField
    val CATALOG_ITEM_SINGLE_SOURCE_NODE: WritableRegistry<CatalogItemSingleSourceNode> = registerSimple(DynamicRegistryKeys.CATALOG_ITEM_SINGLE_SOURCE_NODE)

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