package cc.mewcraft.wakame.item2.config.property

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.register
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.jetbrains.annotations.ApiStatus
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import xyz.xenondevs.commons.reflection.rawType
import java.lang.reflect.Type

/**
 * 代表一个容器, 存放与*物品类型*绑定的数据.
 *
 * @see cc.mewcraft.wakame.item2.data.ItemDataContainer 用于存储运行时可变物品数据的容器
 */
sealed interface ItemPropertyContainer {

    companion object {

        @JvmField
        val EMPTY: ItemPropertyContainer = EmptyItemPropertyContainer

        fun makeDirectSerializers(): TypeSerializerCollection {
            val collection = TypeSerializerCollection.builder()
            collection.register<ItemPropertyContainer>(SimpleItemPropertyContainer.Serializer)
            collection.registerAll(ItemPropertyTypes.directSerializers())
            return collection.build()
        }

        fun build(block: Builder.() -> Unit): ItemPropertyContainer {
            return builder().apply(block).build()
        }

        fun builder(): Builder {
            return SimpleItemPropertyContainer()
        }
    }

    /**
     * 获取指定类型的数据.
     *
     * @param type 要查询的数据类型
     * @return 指定类型的数据, 如果不存在则返回 `null`
     */
    operator fun <T> get(type: ItemPropertyType<out T>): T?

    /**
     * 获取指定类型的数据, 如果不存在则返回默认值.
     */
    fun <T> getOrDefault(type: ItemPropertyType<out T>, fallback: T): T = get(type) ?: fallback

    /**
     * 判断是否包含指定类型的数据.
     *
     * @param type 要检查的物品属性类型
     */
    infix fun has(type: ItemPropertyType<*>): Boolean = get(type) != null

    /**
     * 判断是否包含指定类型的数据.
     */
    operator fun contains(type: ItemPropertyType<*>): Boolean = has(type)

    /**
     * [ItemPropertyContainer] 的生成器.
     */
    sealed interface Builder : ItemPropertyContainer {

        /**
         * 设置指定类型的数据.
         *
         * @return 设置之前的数据, 如果不存在则返回 `null`
         */
        operator fun <T> set(type: ItemPropertyType<in T>, value: T): T?

        /**
         * 设置指定类型的数据.
         *
         * @return 设置之前的数据, 如果不存在则返回 `null`
         */
        @ApiStatus.Internal
        fun setUnsafe(type: ItemPropertyType<*>, value: Any): Any?

        /**
         * 构建 [ItemPropertyContainer].
         *
         * @return 当前实例
         */
        fun build(): ItemPropertyContainer

    }
}

// ------------
// 内部实现
// ------------

private data object EmptyItemPropertyContainer : ItemPropertyContainer {
    override fun <T> get(type: ItemPropertyType<out T>): T? = null
}

private class SimpleItemPropertyContainer(
    private val propertyMap: Reference2ObjectOpenHashMap<ItemPropertyType<*>, Any> = Reference2ObjectOpenHashMap(),
) : ItemPropertyContainer, ItemPropertyContainer.Builder {
    override fun <T> get(type: ItemPropertyType<out T>): T? {
        return propertyMap[type] as T?
    }

    override fun <T> set(type: ItemPropertyType<in T>, value: T): T? {
        return propertyMap.put(type, value) as T?
    }

    override fun setUnsafe(type: ItemPropertyType<*>, value: Any): Any? {
        require(type.typeToken.type.rawType.isInstance(value)) { "Value type mismatch: ${type.typeToken.type.rawType.name} != ${value.javaClass.name}" }
        return propertyMap.put(type, value)
    }

    override fun build(): ItemPropertyContainer {
        return if (propertyMap.isEmpty()) ItemPropertyContainer.EMPTY else this
    }

    object Serializer : TypeSerializer2<ItemPropertyContainer> {
        override fun deserialize(type: Type, node: ConfigurationNode): ItemPropertyContainer {
            val builder = ItemPropertyContainer.builder()
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

        override fun emptyValue(specificType: Type, options: ConfigurationOptions): ItemPropertyContainer? {
            return ItemPropertyContainer.EMPTY
        }
    }
}