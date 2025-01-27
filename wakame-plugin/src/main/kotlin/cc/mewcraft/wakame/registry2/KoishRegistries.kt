package cc.mewcraft.wakame.registry2

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
    val ELEMENT = registerDefaulted(KoishRegistryKeys.ELEMENT, "neutral") // = koish:neutral

    /**
     * 实体类型集合.
     */
    @JvmField
    val ENTITY_TYPE_HOLDER = registerSimple(KoishRegistryKeys.ENTITY_TYPE_HOLDER)

    /**
     * 虚构的属性映射.
     */
    @JvmField
    val IMAGINARY_ATTRIBUTE_MAP = registerSimple(KoishRegistryKeys.IMAGINARY_ATTRIBUTE_MAP)

    /**
     * 标准物品类型.
     *
     * 玩家可以直接获得/使用的物品类型.
     */
    @JvmField
    val ITEM = registerDefaultedFuzzy(KoishRegistryKeys.ITEM, "internal:unknown")

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
    val LEVEL_RARITY_MAPPING = registerDefaulted(KoishRegistryKeys.LEVEL_RARITY_MAPPING, "__default__") // = koish:__default__

    /**
     * 稀有度.
     */
    @JvmField
    val RARITY = registerDefaulted(KoishRegistryKeys.RARITY, "common") // = koish:common

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