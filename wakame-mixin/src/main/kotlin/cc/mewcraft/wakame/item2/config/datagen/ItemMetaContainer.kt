package cc.mewcraft.wakame.item2.config.datagen

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.configurate.TypeSerializer2
import cc.mewcraft.wakame.item2.data.ItemDataContainer
import cc.mewcraft.wakame.item2.data.ItemDataType
import cc.mewcraft.wakame.mixin.support.DataComponentsPatch
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.register
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.lang.reflect.Type

/**
 * 代表一个容器, 存放 "Item Data" 的 [配置项][ItemMetaEntry].
 * 该接口的实例会依附在物品的配置文件实例上, 作为物品生成的元数据.
 *
 * 这里的 “Item Data” 不仅包括我们自己的数据类型, 也包括 Minecraft 自带的 [物品组件](https://minecraft.wiki/w/Data_component_format).
 * 也就是说, 程序员可以为一个我们自己的数据类型编写配置文件, 也可以为 Minecraft 自带的物品组件编写配置文件.
 * 例如, 前者可以让 *物品等级* 按照特定规则生成, 后者可以让 *物品耐久度 (Minecraft 特性)* 具有随机性.
 */
interface ItemMetaContainer {

    companion object {

        @JvmField
        val EMPTY: ItemMetaContainer = EmptyItemMetaContainer

        fun makeSerializers(): TypeSerializerCollection {
            val collection = TypeSerializerCollection.builder()
            collection.register<ItemMetaContainer>(ItemMetaContainerImpl.Serializer)
            collection.registerAll(ItemMetaTypes.directSerializers())
            return collection.build()
        }

        fun build(block: Builder.() -> Unit): ItemMetaContainer {
            return builder().apply(block).build()
        }

        fun builder(): Builder {
            return ItemMetaContainerImpl()
        }

    }

    /**
     * 获取 [ItemMetaType] 对应的 [ItemMetaEntry].
     *
     * @param U 配置类型, 即 [ItemMetaEntry] 的实现类
     * @param V 数据类型, 即 [配置类型][U] 对应的 *数据类型*
     */
    operator fun <U, V> get(type: ItemMetaType<U, V>): ItemMetaEntry<V>?

    /**
     * [ItemMetaContainer] 的生成器.
     */
    interface Builder : ItemMetaContainer {

        /**
         * 设置 [ItemMetaType] 对应的 [ItemMetaEntry].
         */
        operator fun <U, V> set(type: ItemMetaType<U, V>, value: ItemMetaEntry<V>)

        /**
         * 设置 [ItemMetaType] 对应的 [ItemMetaEntry].
         */
        fun set0(type: ItemMetaType<*, *>, value: Any)

        /**
         * 以当前状态创建一个 [ItemDataContainer] 实例.
         *
         * @return 当前实例
         */
        fun build(): ItemMetaContainer

    }

}

/**
 * 代表一个 "Item Data" 的配置项.
 *
 * @param V 对应的数据类型
 */
interface ItemMetaEntry<V> {

    /**
     * 根据上下文生成数据 [V].
     */
    fun make(context: Context): ItemMetaResult<V>

    /**
     * 向物品堆叠写入数据 [V].
     *
     * ### 实现注意事项
     * 如果要写入的数据是自定义数据类型, 而不是 Minecraft 自带的数据类型,
     * 应该使用函数 [MojangStack.ensureSetData] 来确保数据可以写入成功.
     */
    fun write(value: V, itemstack: MojangStack)

    /**
     * 向该物品堆叠写入数据 [T], *无论该物品堆叠是否为合法的自定义物品*.
     *
     * @return 原有的值, 如果没有则返回 `null`
     */
    fun <T> MojangStack.ensureSetData(type: ItemDataType<in T>, value: T): T? {
        val container = getOrDefault(DataComponentsPatch.ITEM_DATA_CONTAINER, ItemDataContainer.EMPTY)
        val builder = container.toBuilder()
        val oldVal = builder.set(type, value)
        set(DataComponentsPatch.ITEM_DATA_CONTAINER, builder.build())
        return oldVal
    }

    /**
     * 向该物品堆叠写入数据 [T], *无论该物品堆叠是否为合法的自定义物品*.
     *
     * @return 原有的值, 如果没有则返回 `null`
     */
    fun <T> MojangStack.ensureRemoveData(type: ItemDataType<out T>): T? {
        val container = getOrDefault(DataComponentsPatch.ITEM_DATA_CONTAINER, ItemDataContainer.EMPTY)
        val builder = container.toBuilder()
        val oldVal = builder.remove(type)
        set(DataComponentsPatch.ITEM_DATA_CONTAINER, builder.build())
        return oldVal
    }

}

// ------------
// 内部实现
// ------------

private data object EmptyItemMetaContainer : ItemMetaContainer {
    override fun <U, V> get(type: ItemMetaType<U, V>): ItemMetaEntry<V>? = null
}

private class ItemMetaContainerImpl(
    private val metaMap: Reference2ObjectLinkedOpenHashMap<ItemMetaType<*, *>, ItemMetaEntry<*>> = Reference2ObjectLinkedOpenHashMap(),
) : ItemMetaContainer, ItemMetaContainer.Builder {

    override fun <U, V> get(type: ItemMetaType<U, V>): ItemMetaEntry<V>? {
        return metaMap[type] as ItemMetaEntry<V>?
    }

    override fun <U, V> set(type: ItemMetaType<U, V>, value: ItemMetaEntry<V>) {
        metaMap.put(type, value)
    }

    override fun set0(type: ItemMetaType<*, *>, value: Any) {
        // 警告: 这里无法对 value: Any 中的泛型参数做检查, 实现需要保证 value 的类型完全正确
        metaMap.put(type, value as ItemMetaEntry<*>)
    }

    override fun build(): ItemMetaContainer {
        return if (metaMap.isEmpty()) ItemMetaContainer.EMPTY else this
    }

    object Serializer : TypeSerializer2<ItemMetaContainer> {
        override fun deserialize(type: Type, node: ConfigurationNode): ItemMetaContainer {
            val builder = ItemMetaContainer.builder()
            for ((rawNodeKey, node) in node.childrenMap()) {
                val nodeKey = rawNodeKey.toString()
                val dataType = KoishRegistries2.ITEM_META_TYPE[nodeKey] ?: continue
                val dataValue = node.get(dataType.typeToken) ?: run {
                    LOGGER.error("Failed to deserialize $dataType. Skipped.")
                    continue
                }
                builder.set0(dataType, dataValue)
            }
            return builder.build()
        }

        override fun emptyValue(specificType: Type, options: ConfigurationOptions): ItemMetaContainer? {
            return ItemMetaContainer.EMPTY
        }
    }

}