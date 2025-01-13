package cc.mewcraft.wakame.registry2

import cc.mewcraft.wakame.item.ItemRegistryConfigStorage
import cc.mewcraft.wakame.rarity.LevelRarityMappingRegistryConfigStorage

object KoishRegistries {
    private val ACCESS = MutableRegistryAccess()

    ///

    /**
     * 机制.
     */
    @JvmField
    val ABILITY = registerSimple(KoishRegistryKeys.ABILITY)

    /**
     * 属性.
     */
    @JvmField
    val ATTRIBUTE = registerSimple(KoishRegistryKeys.ATTRIBUTE)

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
    val ATTRIBUTE_SUPPLIER = registerSimple(KoishRegistryKeys.ATTRIBUTE_SUPPLIER)

    /**
     * 属性块的 Facade.
     */
    @JvmField
    val ATTRIBUTE_BUNDLE_FACADE = registerSimple(KoishRegistryKeys.ATTRIBUTE_BUNDLE_FACADE)

    /**
     * 元素.
     */
    @JvmField
    val ELEMENT = registerDefaulted(KoishRegistryKeys.ELEMENT, "neutral")

    /**
     * 实体类型集合.
     */
    @JvmField
    val ENTITY_TYPE_HOLDER = registerSimple(KoishRegistryKeys.ENTITY_TYPE_HOLDER)

    /**
     * 标准物品类型.
     *
     * 玩家可以直接获得/使用的物品类型.
     */
    @JvmField
    val ITEM = registerDefaultedFuzzy(KoishRegistryKeys.ITEM, ItemRegistryConfigStorage.UNKNOWN_ITEM_ID)

    // /**
    //  * 原版套皮物品.
    //  *
    //  * 玩家无法直接获得/使用, 仅用于给纯原版物品套一层皮 (i.e., 给原版物品添加内容)
    //  */
    // @JvmField
    // val VANILLA_WRAPPER_ITEM = registerSimple(KoishRegistryKeys.VANILLA_PROXY_ITEM)

    /**
     * 物品皮肤.
     */
    @JvmField
    val ITEM_SKIN = registerSimple(KoishRegistryKeys.ITEM_SKIN)

    /**
     * 铭刻.
     */
    @JvmField
    val KIZAMI = registerSimple(KoishRegistryKeys.KIZAMI)

    /**
     * 等级>稀有度映射.
     */
    @JvmField
    val LEVEL_RARITY_MAPPING = registerDefaulted(KoishRegistryKeys.LEVEL_RARITY_MAPPING, LevelRarityMappingRegistryConfigStorage.DEFAULT_ENTRY_NAME)

    /**
     * 稀有度.
     */
    @JvmField
    val RARITY = registerDefaulted(KoishRegistryKeys.RARITY, "common")

    ///

    fun resetRegistries() {
        ACCESS.resetRegistries()
    }

    private fun <T> registerSimple(key: RegistryKey<out Registry<T>>, initializer: (Registry<T>) -> Unit = {}): WritableRegistry<T> {
        return ACCESS.add(key, SimpleRegistry(key).apply(initializer))
    }

    private fun <T> registerDefaulted(key: RegistryKey<out Registry<T>>, defaultId: String, initializer: (Registry<T>) -> Unit = {}): WritableDefaultedRegistry<T> {
        return ACCESS.add(key, SimpleDefaultedRegistry(defaultId, key).apply(initializer)) as WritableDefaultedRegistry<T>
    }

    private fun <T> registerDefaultedFuzzy(key: RegistryKey<out Registry<T>>, defaultId: String, initializer: (Registry<T>) -> Unit = {}): WritableDefaultedFuzzyRegistry<T> {
        return ACCESS.add(key, SimpleDefaultedFuzzyRegistry(defaultId, key).apply(initializer)) as WritableDefaultedFuzzyRegistry<T>
    }
}