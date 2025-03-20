package cc.mewcraft.wakame.registry2

import cc.mewcraft.wakame.item2.ItemRefHandler
import cc.mewcraft.wakame.item2.KoishItem
import cc.mewcraft.wakame.item2.KoishItemProxy
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.behavior.ItemBehaviorTypes
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaType
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaTypes
import cc.mewcraft.wakame.item2.config.property.ItemPropertyType
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.data.ItemDataType
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.TestOnly

object KoishRegistries2 {

    private val ACCESS: MutableRegistryAccess = MutableRegistryAccess()
    private val INITIALIZERS = mutableListOf<() -> Unit>()

    // ------------
    // 注册表
    // ------------

    /**
     * 自定义物品的类型.
     */
    @JvmField
    val ITEM: WritableDefaultedFuzzyRegistry<KoishItem> = registerDefaultedFuzzy(KoishRegistryKeys2.ITEM, "internal/error")

    /**
     * 套皮物品堆叠的实例.
     */
    @JvmField
    val ITEM_PROXY: WritableRegistry<KoishItemProxy> = registerSimple(KoishRegistryKeys2.ITEM_PROXY)

    /**
     * "Item Data" 的类型.
     */
    @JvmField
    val ITEM_DATA_TYPE: WritableRegistry<ItemDataType<*>> = registerSimple(KoishRegistryKeys2.ITEM_DATA_TYPE) { ItemDataTypes }

    /**
     * "Item Meta" 的类型.
     * "Item Meta" 相当于 "Item Data" 的配置文件.
     */
    @JvmField
    val ITEM_META_TYPE: WritableRegistry<ItemMetaType<*, *>> = registerSimple(KoishRegistryKeys2.ITEM_META_TYPE) { ItemMetaTypes }

    /**
     * "Item Property" 的类型.
     */
    @JvmField
    val ITEM_PROPERTY_TYPE: WritableRegistry<ItemPropertyType<*>> = registerSimple(KoishRegistryKeys2.ITEM_PROPERTY_TYPE) { ItemPropertyTypes }

    /**
     * "Item Behavior" 的类型.
     */
    @JvmField
    val ITEM_BEHAVIOR: WritableRegistry<ItemBehavior> = registerSimple(KoishRegistryKeys2.ITEM_BEHAVIOR) { ItemBehaviorTypes }

    /**
     * [ItemRefHandler] 的实例.
     * 存放来自第三方插件的 [ItemRefHandler].
     */
    @JvmField
    val ITEM_REF_HANDLER: WritableRegistry<ItemRefHandler<*>> = registerSimple(KoishRegistryKeys2.ITEM_REF_HANDLER)

    /**
     * [ItemRefHandler] 的内置实例.
     * 只包含 Koish 和 Minecraft 两个 [ItemRefHandler].
     */
    @ApiStatus.Internal
    @JvmField
    val INTERNAL_ITEM_REF_HANDLER: WritableRegistry<ItemRefHandler<*>> = registerDefaulted(KoishRegistryKeys2.ITEM_REF_HANDLER, "minecraft")


    // 在本类型 <clinit> 最后执行所有的 INITIALIZER
    init {
        INITIALIZERS.forEach { initializer -> initializer() }
    }


    // ------------
    // 方便函数
    // ------------

    /**
     * 创建一个 [WritableRegistry].
     */
    private fun <T> registerSimple(key: RegistryKey<out Registry<T>>, initializer: (Registry<T>) -> Unit = {}): WritableRegistry<T> {
        return internalRegister(key, SimpleRegistry(key), initializer)
    }

    /**
     * 创建一个 [WritableDefaultedRegistry].
     */
    private fun <T> registerDefaulted(key: RegistryKey<out Registry<T>>, defaultId: String, initializer: (Registry<T>) -> Unit = {}): WritableDefaultedRegistry<T> {
        return internalRegister(key, SimpleDefaultedRegistry(defaultId, key), initializer)
    }

    /**
     * 创建一个 [WritableDefaultedFuzzyRegistry].
     */
    private fun <T> registerDefaultedFuzzy(key: RegistryKey<out Registry<T>>, defaultId: String, initializer: (Registry<T>) -> Unit = {}): WritableDefaultedFuzzyRegistry<T> {
        return internalRegister(key, SimpleDefaultedFuzzyRegistry(defaultId, key), initializer)
    }

    private fun <T, R : WritableRegistry<T>> internalRegister(key: RegistryKey<out Registry<T>>, registry: R, initializer: (Registry<T>) -> Unit = {}): R {
        ACCESS.add(key, registry).also { INITIALIZERS += { initializer(it) } }
        return registry
    }

    // ------------
    // 仅测试用
    // ------------

    @TestOnly
    fun resetRegistries() {
        ACCESS.resetRegistries()
    }

}