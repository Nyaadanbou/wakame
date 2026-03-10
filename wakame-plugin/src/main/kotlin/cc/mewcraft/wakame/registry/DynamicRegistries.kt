package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.catalog.item.CatalogItemCategory
import cc.mewcraft.wakame.catalog.item.recipe.*

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
    //  signup,
    //    需要指定一个签到事件 的 id
    //    签到事件本身需要单独的配置文件, 用于指定签到的 id, 名字, 获得的物品
    //    签到事件包括但不限于: 普通每日签到, 建筑师每日签到, 连续签到3天, 连续签到7天
    //    这些签到事件全部配置好, 再构建出一个完整的 “来源&用途” 网络
    //  quest,
    //    需要指定一个 NPC 的 id
    //    NPC 本身需要单独的配置文件, 用于指定 NPC 的 id, 名字, 以及可能获得的物品
    //    这些 NPC 全部配置好, 再构建出一个完整的 “来源&用途” 网络
    //  crate,
    //    需要指定一个盲盒的 id
    //    盲盒本身需要单独的配置文件, 用于指定盲盒的 id, 名字, 以及可能获得的物品
    //    这些盲盒全部配置好, 再构建出一个完整的 “来源&用途” 网络
    //  MythicMobs drop,
    //    程序读取 MythicMobs 的掉落物并自动构建 “来源&用途” 网络
    //    这个可能需要配合修改 wakame-hooks/wakame-hook-mythicmobs 里的内容
    //    Copilot: 暂时先不要实现这个, 我需要先自己仔细看看 MythicMobs 的 API
    //  crafting station,
    //    程序读取 CraftingStation 的配方并自动构建 “来源&用途” 网络
    //  minecraft recipe,
    //    这个已经实现并投产了, 但可能需要重构
    //    程序读取 Minecraft 的配方并自动构建 “来源&用途” 网络
    //    注意, 每一种类型的配方 (例如有序, 无序, 熔炉...) 都会构建出一个单独的 CatalogItemRecipeType
    //    也就是每一种类型的配方都会构建出一个单独的 “来源&用途” 网络
    //  minecraft loot table,
    //    这个已经实现并投产了, 但可能需要重构
    //    程序读取 Minecraft 的战利品表并自动构建 “来源&用途” 网络
    /**
     * 物品图鉴中配方的类型. 例如 "合成配方" "熔炉配方" "锻造配方" 等等, 以及一些特殊的配方类型, 例如 "签到奖励" "任务奖励" "宝箱奖励" 等等.
     */
    @JvmField
    val CATALOG_ITEM_RECIPE_TYPE: WritableRegistry<CatalogItemRecipeType> = registerSimple(DynamicRegistryKeys.CATALOG_ITEM_RECIPE_TYPE)

    /**
     * 物品图鉴中的合成站配方.
     * 这个配方的输入是一系列物品, 输出是一个物品.
     */
    @JvmField
    val CATALOG_ITEM_CRAFTING_STATION_RECIPE: WritableRegistry<CatalogItemCraftingStationRecipe> = registerSimple(DynamicRegistryKeys.CATALOG_ITEM_CRAFTING_STATION_RECIPE)

    /**
     * 物品图鉴中的盲盒配方.
     * 这个配方的输入是一个盲盒, 输出是一系列物品.
     */
    @JvmField
    val CATALOG_ITEM_CRATE_RECIPE: WritableRegistry<CatalogItemCrateRecipe> = registerSimple(DynamicRegistryKeys.CATALOG_ITEM_CRATE_RECIPE)

    /**
     * 物品图鉴中的战利品表配方.
     * 这个配方的输入是一个 Minecraft 的战利品表, 输出是一系列物品.
     */
    @JvmField
    val CATALOG_ITEM_LOOT_TABLE_RECIPE: WritableRegistry<CatalogItemLootTableRecipe> = registerSimple(DynamicRegistryKeys.CATALOG_ITEM_LOOT_TABLE_RECIPE)

    /**
     * 物品图鉴中的 MythicMobs 生物掉落配方.
     * 这个配方的输入是一个 MythicMobs 的生物, 输出是一系列物品.
     */
    @JvmField
    val CATALOG_ITEM_MYTHIC_DROP_RECIPE: WritableRegistry<CatalogItemMythicDropRecipe> = registerSimple(DynamicRegistryKeys.CATALOG_ITEM_MYTHIC_DROP_RECIPE)

    /**
     * 物品图鉴中的任务奖励配方.
     * 这个配方的输入是一个 NPC, 输出是一系列物品.
     */
    @JvmField
    val CATALOG_ITEM_QUEST_RECIPE: WritableRegistry<CatalogItemQuestRecipe> = registerSimple(DynamicRegistryKeys.CATALOG_ITEM_QUEST_RECIPE)

    /**
     * 物品图鉴中的签到奖励配方.
     * 这个配方的输入是签到事件, 输出是一系列物品.
     */
    @JvmField
    val CATALOG_ITEM_SIGNUP_RECIPE: WritableRegistry<CatalogItemSignupRecipe> = registerSimple(DynamicRegistryKeys.CATALOG_ITEM_SIGNUP_RECIPE)

    /**
     * 物品图鉴中的标准合成配方.
     * 这个配方的输入是一系列物品, 输出是一个物品.
     */
    @JvmField
    val CATALOG_ITEM_STANDARD_RECIPE: WritableRegistry<CatalogItemStandardRecipe> = registerSimple(DynamicRegistryKeys.CATALOG_ITEM_STANDARD_RECIPE)

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