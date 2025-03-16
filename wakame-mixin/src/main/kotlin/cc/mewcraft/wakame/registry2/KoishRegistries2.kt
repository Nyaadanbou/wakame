package cc.mewcraft.wakame.registry2

import cc.mewcraft.wakame.item2.KoishItem
import cc.mewcraft.wakame.item2.KoishItemProxy
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaType
import cc.mewcraft.wakame.item2.config.property.ItemPropertyType
import cc.mewcraft.wakame.item2.data.ItemDataType
import org.jetbrains.annotations.TestOnly

object KoishRegistries2 {

    private val ACCESS: MutableRegistryAccess = MutableRegistryAccess()

    // ------------
    // 注册表
    // ------------

    /**
     * 自定义物品的类型.
     */
    @JvmField
    val ITEM: WritableDefaultedFuzzyRegistry<KoishItem> = registerDefaultedFuzzy(KoishRegistryKeys2.ITEM, "internal:unknown")

    /**
     * 原版套皮物品的实例.
     */
    @JvmField
    val ITEM_PROXY: WritableRegistry<KoishItemProxy> = registerSimple(KoishRegistryKeys2.ITEM_PROXY)

    /**
     * "Item Data" 的类型.
     */
    @JvmField
    val ITEM_DATA_TYPE: WritableRegistry<ItemDataType<*>> = registerSimple(KoishRegistryKeys2.ITEM_DATA_TYPE)

    /**
     * "Item Meta" 的类型.
     * "Item Meta" 相当于 "Item Data" 的配置文件.
     */
    @JvmField
    val ITEM_META_TYPE: WritableRegistry<ItemMetaType<*>> = registerSimple(KoishRegistryKeys2.ITEM_META_TYPE)

    /**
     * "Item Property" 的类型.
     */
    @JvmField
    val ITEM_PROPERTY_TYPE: WritableRegistry<ItemPropertyType<*>> = registerSimple(KoishRegistryKeys2.ITEM_PROPERTY_TYPE)

    /**
     * "Item Behavior" 的类型.
     */
    @JvmField
    val ITEM_BEHAVIOR: WritableRegistry<ItemBehavior> = registerSimple(KoishRegistryKeys2.ITEM_BEHAVIOR)

    // ------------
    // 方便函数
    // ------------

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

    // ------------
    // 仅测试用
    // ------------

    @TestOnly
    fun resetRegistries() {
        ACCESS.resetRegistries()
    }

}