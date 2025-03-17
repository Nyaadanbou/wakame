package cc.mewcraft.wakame.item2.config.property

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.registry2.KoishRegistries2
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.spongepowered.configurate.ConfigurationNode
import xyz.xenondevs.commons.reflection.rawType
import java.lang.reflect.Type

// FIXME #350: 补全文档
/**
 * 代表一个容器, 存放与 *物品类型* 绑定的数据.
 */
interface ItemPropertyContainer {
    companion object {
        fun makeSerializer(): TypeSerializer<ItemPropertyContainer> {
            return ItemPropertyContainerImpl.Serializer
        }

        fun build(block: Builder.() -> Unit): ItemPropertyContainer {
            return builder().apply(block).build()
        }

        fun builder(): Builder {
            return ItemPropertyContainerImpl()
        }
    }

    operator fun <T> get(type: ItemPropertyType<out T>): T?
    fun <T> getOrDefault(type: ItemPropertyType<out T>, fallback: T): T = get(type) ?: fallback
    infix fun has(type: ItemPropertyType<*>): Boolean = get(type) != null
    operator fun contains(type: ItemPropertyType<*>): Boolean = has(type)

    interface Builder : ItemPropertyContainer {
        operator fun <T> set(type: ItemPropertyType<in T>, value: T): T?
        fun set0(type: ItemPropertyType<*>, value: Any): Any?
        fun build(): ItemPropertyContainer
    }
}

private data object EmptyItemPropertyContainer : ItemPropertyContainer {
    override fun <T> get(type: ItemPropertyType<out T>): T? = null
}

private class ItemPropertyContainerImpl(
    private val properties: Reference2ObjectOpenHashMap<ItemPropertyType<*>, Any> = Reference2ObjectOpenHashMap(),
) : ItemPropertyContainer, ItemPropertyContainer.Builder {
    override fun <T> get(type: ItemPropertyType<out T>): T? {
        return properties[type] as T?
    }

    override fun <T> set(type: ItemPropertyType<in T>, value: T): T? {
        return properties.put(type, value) as T?
    }

    override fun set0(type: ItemPropertyType<*>, value: Any): Any? {
        require(type.typeToken.type.rawType.isInstance(value)) { "Value type mismatch: ${type.typeToken.type.rawType.name} != ${value.javaClass.name}" }
        return properties.put(type, value)
    }

    override fun build(): ItemPropertyContainer {
        return if (properties.isEmpty()) EmptyItemPropertyContainer else this
    }

    object Serializer : TypeSerializer<ItemPropertyContainer> {
        override fun deserialize(type: Type, node: ConfigurationNode): ItemPropertyContainer {
            val builder = ItemPropertyContainer.builder()
            for ((rawNodeKey, node) in node.childrenMap()) {
                val nodeKey = rawNodeKey.toString()
                val dataType = KoishRegistries2.ITEM_PROPERTY_TYPE[nodeKey] ?: run {
                    LOGGER.error("Unknown item property type: '$nodeKey'. Skipped.")
                    continue
                }
                val dataTypeToken = dataType.typeToken
                val dataValue = node.get(dataTypeToken) ?: run {
                    LOGGER.error("Failed to deserialize $dataType. Skipped.")
                    continue
                }
                builder.set0(dataType, dataValue)
            }
            return builder.build()
        }
    }
}