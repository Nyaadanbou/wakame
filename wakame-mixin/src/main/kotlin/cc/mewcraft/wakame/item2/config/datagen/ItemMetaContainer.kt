package cc.mewcraft.wakame.item2.config.datagen

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.configurate.TypeSerializer2
import cc.mewcraft.wakame.registry2.KoishRegistries2
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

/**
 * 代表一个 "Item Data" 的[配置项][ItemMetaEntry]的容器.
 */
interface ItemMetaContainer {

    companion object {

        fun makeSerializer(): TypeSerializer<ItemMetaContainer> {
            return ItemMetaContainerImpl.Serializer
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
     */
    operator fun <T> get(type: ItemMetaType<T>): ItemMetaEntry<T>?

    interface Builder : ItemMetaContainer {

        operator fun <T> set(type: ItemMetaType<T>, value: ItemMetaEntry<T>)

        fun set0(type: ItemMetaType<*>, value: Any)

        fun build(): ItemMetaContainer

    }

}

/**
 * 代表一个 "Item Data" 的配置项.
 *
 * @param T 对应的数据类型
 */
interface ItemMetaEntry<T> {

    /**
     * 生成数据 [T].
     */
    fun generate(context: Context): ItemMetaResult<T>

}

// ------------
// 内部实现
// ------------

private class ItemMetaContainerImpl(
    private val metaMap: Reference2ObjectLinkedOpenHashMap<ItemMetaType<*>, ItemMetaEntry<*>> = Reference2ObjectLinkedOpenHashMap(),
) : ItemMetaContainer, ItemMetaContainer.Builder {

    override fun <T> get(type: ItemMetaType<T>): ItemMetaEntry<T>? {
        return metaMap[type] as ItemMetaEntry<T>?
    }

    override fun <T> set(type: ItemMetaType<T>, value: ItemMetaEntry<T>) {
        metaMap.put(type, value)
    }

    override fun set0(type: ItemMetaType<*>, value: Any) {
        require(value is ItemMetaEntry<*>) { "The type of value must be ${ItemMetaEntry::class}" }
        metaMap.put(type, value)
    }

    override fun build(): ItemMetaContainer {
        return this
    }

    object Serializer : TypeSerializer2<ItemMetaContainer> {
        override fun deserialize(type: Type, node: ConfigurationNode): ItemMetaContainer {
            val builder = ItemMetaContainer.builder()
            for ((rawNodeKey, node) in node.childrenMap()) {
                val nodeKey = rawNodeKey.toString()
                val dataType = KoishRegistries2.ITEM_META_TYPE[nodeKey] ?: run {
                    LOGGER.error("Unknown item meta type: '$nodeKey'. Skipped.")
                    continue
                }
                val dataValue = node.get(dataType.typeToken) ?: run {
                    LOGGER.error("Failed to deserialize $dataType. Skipped.")
                    continue
                }
                builder.set0(dataType, dataValue)
            }
            return builder.build()
        }
    }

}