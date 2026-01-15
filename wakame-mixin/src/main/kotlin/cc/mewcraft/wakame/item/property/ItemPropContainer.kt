package cc.mewcraft.wakame.item.property

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.register
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.registry.BuiltInRegistries
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.jetbrains.annotations.ApiStatus
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.lang.reflect.Type

/**
 * 代表一个容器, 存放与*物品类型*绑定的数据.
 *
 * @see cc.mewcraft.wakame.item.data.ItemDataContainer 用于存储运行时可变物品数据的容器
 */
sealed interface ItemPropContainer {

    companion object {

        @JvmField
        val EMPTY: ItemPropContainer = EmptyItemPropContainer

        fun makeDirectSerializers(): TypeSerializerCollection {
            val collection = TypeSerializerCollection.builder()
            collection.register<ItemPropContainer>(SimpleItemPropContainer.Serializer)
            collection.registerAll(ItemPropTypes.directSerializers())
            return collection.build()
        }

        fun build(block: Builder.() -> Unit): ItemPropContainer {
            return builder().apply(block).build()
        }

        fun builder(): Builder {
            return SimpleItemPropContainer()
        }
    }

    /**
     * 获取指定类型的数据.
     *
     * @param type 要查询的数据类型
     * @return 指定类型的数据, 如果不存在则返回 `null`
     */
    operator fun <T> get(type: ItemPropType<out T>): T?

    /**
     * 获取指定类型的数据, 如果不存在则返回默认值.
     */
    fun <T> getOrDefault(type: ItemPropType<out T>, fallback: T): T = get(type) ?: fallback

    /**
     * 判断是否包含指定类型的数据.
     *
     * @param type 要检查的物品属性类型
     */
    infix fun has(type: ItemPropType<*>): Boolean = get(type) != null

    /**
     * 判断是否包含指定类型的数据.
     */
    operator fun contains(type: ItemPropType<*>): Boolean = has(type)

    /**
     * [ItemPropContainer] 的生成器.
     */
    sealed interface Builder : ItemPropContainer {

        /**
         * 设置指定类型的数据.
         *
         * @return 设置之前的数据, 如果不存在则返回 `null`
         */
        operator fun <T> set(type: ItemPropType<in T>, value: T): T?

        /**
         * 设置指定类型的数据.
         *
         * @return 设置之前的数据, 如果不存在则返回 `null`
         */
        @ApiStatus.Internal
        fun setUnsafe(type: ItemPropType<*>, value: Any): Any?

        /**
         * 构建 [ItemPropContainer].
         *
         * @return 当前实例
         */
        fun build(): ItemPropContainer

    }
}

// ------------
// 内部实现
// ------------

private data object EmptyItemPropContainer : ItemPropContainer {
    override fun <T> get(type: ItemPropType<out T>): T? = null
}

private class SimpleItemPropContainer(
    private val propertyMap: Reference2ObjectOpenHashMap<ItemPropType<*>, Any> = Reference2ObjectOpenHashMap(),
) : ItemPropContainer, ItemPropContainer.Builder {
    override fun <T> get(type: ItemPropType<out T>): T? {
        return propertyMap[type] as T?
    }

    override fun <T> set(type: ItemPropType<in T>, value: T): T? {
        return propertyMap.put(type, value) as T?
    }

    override fun setUnsafe(type: ItemPropType<*>, value: Any): Any? {
        // 警告: 实现上必须确保这里传入的 value 类型一定是正确的
        return propertyMap.put(type, value)
    }

    override fun build(): ItemPropContainer {
        return if (propertyMap.isEmpty()) ItemPropContainer.EMPTY else this
    }

    object Serializer : SimpleSerializer<ItemPropContainer> {
        override fun deserialize(type: Type, node: ConfigurationNode): ItemPropContainer {
            val builder = ItemPropContainer.builder()
            for ((rawNodeKey, node) in node.childrenMap()) {
                val nodeKey = rawNodeKey.toString()
                val dataType = BuiltInRegistries.ITEM_PROPERTY_TYPE[nodeKey] ?: continue
                val dataValue = node.get(dataType.typeToken) ?: run {
                    LOGGER.error("Failed to deserialize $dataType. Skipped.")
                    continue
                }
                builder.setUnsafe(dataType, dataValue)
            }
            return builder.build()
        }

        override fun emptyValue(specificType: Type, options: ConfigurationOptions): ItemPropContainer? {
            return ItemPropContainer.EMPTY
        }
    }
}