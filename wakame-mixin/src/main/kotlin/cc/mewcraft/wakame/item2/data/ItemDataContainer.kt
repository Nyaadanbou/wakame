// 备忘录:
// ItemData 需要具有 lifecycle 属性, 用来表示可以在什么时候使用
// ItemDataContainer

package cc.mewcraft.wakame.item2.data

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.item2.data.ItemDataContainer.Companion.build
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.util.typeTokenOf
import com.mojang.serialization.Codec
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.extra.dfu.v8.DfuSerializers
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import xyz.xenondevs.commons.reflection.rawType
import java.lang.reflect.Type


// FIXME #350: 这个数据类型将成为 NMS DataComponentMap 的一部分.
//  而根据 DataComponentMap 的契约, 该实例的数据必须为不可变, 所有 set 函数都不应该修改 `this` 的数据.
//  从 ItemStack 来看, 如果要修改一个 ItemData, 则大概流程为:
//  1) 先从 DataComponentMap 获取 ItemDataContainer
//  2) 使用 set 设置新的 ItemData, 返回一个新的 ItemDataContainer
//  3) 将新的 ItemDataContainer 放回 DataComponentMap
//  ---
//  等等, 似乎只需要把 DataComponentMap 的不可变契约的设计用在 ItemDataContainer 上就行.
//  ---
//  实现了一下, 与其自行搞另一套契约, 最稳妥地还是遵循现有的契约.
//  另一套契约需要修改函数 PatchedDataComponentMap#copy, 插入一些我们自己的逻辑.
//  “如无必要, 勿增实体”.
/**
 * 储存物品数据的容器.
 *
 * 该容器本身不可变, 容器内的数据也不可变. 违反此契约将导致此实例被克隆后, 出现数据的脏读/写.
 *
 * 如果要基于当前容器修改数据, 使用 [toBuilder] 创建一个 [Builder] 实例, 便可以修改数据.
 * 修改完后再使用 [build] 创建一个新的 [ItemDataContainer] 实例, 便获得了修改后的版本.
 */
interface ItemDataContainer : Iterable<Map.Entry<ItemDataType<*>, Any>> {

    companion object {

        @JvmStatic
        val EMPTY: ItemDataContainer = EmptyItemDataContainer

        @JvmStatic
        fun makeCodec(): Codec<ItemDataContainer> {
            val serializers = TypeSerializerCollection.builder()

            // 添加 ItemDataContainer 的 TypeSerializer
            serializers.register(typeTokenOf<ItemDataContainer>(), makeSerializer())

            // 添加每一个 “ItemData” 的 TypeSerializer
            serializers.registerAll(ItemDataTypes.serializers())

            val codec = DfuSerializers.codec(typeTokenOf<ItemDataContainer>(), serializers.build())
            requireNotNull(codec) { "Cannot find an appropriate TypeSerializer for ${ItemDataContainer::class}" }

            return codec
        }

        @JvmStatic
        fun makeSerializer(): TypeSerializer<ItemDataContainer> {
            return ItemDataContainerImpl.Serializer
        }

        fun build(block: Builder.() -> Unit): ItemDataContainer {
            return builder().apply(block).build()
        }

        fun builder(): Builder {
            return ItemDataContainerImpl(copyOnWrite = true)
        }

    }

    /**
     * 返回该容器里的所有数据类型.
     */
    val types: Set<ItemDataType<*>>

    /**
     * 返回该容器里有多少种数据类型.
     */
    val size: Int

    /**
     * 判断该容器是否为空.
     */
    fun isEmpty(): Boolean

    /**
     * 获取指定类型的数据.
     */
    operator fun <T> get(type: ItemDataType<out T>): T?

    /**
     * 判断该容器是否有指定类型的数据.
     */
    infix fun has(type: ItemDataType<*>): Boolean

    /**
     * 判断该容器是否有指定类型的数据.
     */
    operator fun contains(type: ItemDataType<*>): Boolean = has(type)

    /**
     * 获取指定类型的数据, 如果没有, 则返回默认值.
     */
    fun <T> getOrDefault(type: ItemDataType<out T>, fallback: T): T = get(type) ?: fallback

    /**
     * 获取一个可以遍历该容器内所有数据的迭代器.
     * 该迭代器是“快速迭代器”, 即同一个 [Map.Entry] 实例会在整个迭代过程中复用.
     */
    fun fastIterator(): Iterator<Map.Entry<ItemDataType<*>, Any>>

    /**
     * 快速遍历该容器内的所有数据.
     * 该迭代器是“快速迭代器”, 即同一个 [Map.Entry] 实例会在整个迭代过程中复用.
     */
    fun fastForEach(action: (Map.Entry<ItemDataType<*>, Any>) -> Unit) {
        fastIterator().forEach(action)
    }

    /**
     * 创建一个该容器的副本.
     */
    fun copy(): ItemDataContainer

    /**
     * 基于该容器创建一个 [Builder], 可用来创建一个修改后的 [ItemDataContainer].
     */
    fun toBuilder(): Builder

    /**
     * [ItemDataContainer] 的生成器, 添加了可用于修改数据的函数.
     *
     * 该生成器实例是可变的. 如果想基于当前生成器的状态来构建新的生成器, 使用 [toBuilder].
     */
    interface Builder : ItemDataContainer {

        /**
         * 设置指定类型的数据.
         *
         * @return 设置之前的数据, 如果没有则返回 `null`
         */
        operator fun <T> set(type: ItemDataType<in T>, value: T): T?

        /**
         * 设置指定类型的数据.
         *
         * @return 设置之前的数据, 如果没有则返回 `null`
         */
        fun set0(type: ItemDataType<*>, value: Any): Any?

        /**
         * 移除指定类型的数据.
         *
         * @return 移除之前的数据, 如果没有则返回 `null`
         */
        fun <T> remove(type: ItemDataType<out T>): T?

        /**
         * 移除指定类型的数据.
         */
        operator fun minusAssign(type: ItemDataType<*>) {
            remove(type)
        }

        /**
         * 创建一个 [ItemDataContainer] 实例.
         *
         * @return 当前实例
         */
        fun build(): ItemDataContainer

    }

}

private data object EmptyItemDataContainer : ItemDataContainer {
    override val types: Set<ItemDataType<*>> = emptySet()
    override val size: Int = 0
    override fun isEmpty(): Boolean = true
    override fun <T> get(type: ItemDataType<out T>): T? = null
    override fun has(type: ItemDataType<*>): Boolean = false
    override fun fastIterator(): Iterator<Map.Entry<ItemDataType<*>, Any>> = iterator()
    override fun copy(): ItemDataContainer = this
    override fun toBuilder(): ItemDataContainer.Builder = ItemDataContainerImpl(copyOnWrite = false)
    override fun iterator(): Iterator<Map.Entry<ItemDataType<*>, Any>> = emptyMap<ItemDataType<*>, Any>().iterator()
}

// 该 class 同时实现了 ItemDataContainer, ItemDataContainer.Builder.
private open class ItemDataContainerImpl(
    @JvmField
    var data: Reference2ObjectOpenHashMap<ItemDataType<*>, Any> = Reference2ObjectOpenHashMap(),
    @JvmField
    var copyOnWrite: Boolean, // 用于优化 copy 的性能
) : ItemDataContainer, ItemDataContainer.Builder {
    override val types: Set<ItemDataType<*>>
        get() = data.keys

    override val size: Int
        get() = data.size

    override fun isEmpty(): Boolean {
        return this.size == 0
    }

    override fun <T> get(type: ItemDataType<out T>): T? {
        return data[type] as? T
    }

    override fun has(type: ItemDataType<*>): Boolean {
        return get(type) != null
    }

    override fun <T> set(type: ItemDataType<in T>, value: T): T? {
        ensureContainerOwnership()
        return data.put(type, value) as T?
    }

    override fun set0(type: ItemDataType<*>, value: Any): Any? {
        require(type.typeToken.type.rawType.isInstance(value)) { "Value type mismatch: ${type.typeToken.type.rawType.name} != ${value.javaClass.name}" }
        ensureContainerOwnership()
        return data.put(type, value)
    }

    override fun <T> remove(type: ItemDataType<out T>): T? {
        ensureContainerOwnership()
        return data.remove(type) as T?
    }

    override fun iterator(): Iterator<Map.Entry<ItemDataType<*>, Any>> {
        return data.entries.iterator()
    }

    // FIXME #350: make it thread local?
    override fun fastIterator(): Iterator<Map.Entry<ItemDataType<*>, Any>> {
        return data.reference2ObjectEntrySet().fastIterator()
    }

    private fun copy0(): ItemDataContainerImpl {
        copyOnWrite = true
        return ItemDataContainerImpl(data, copyOnWrite = true)
    }

    private fun ensureContainerOwnership() {
        if (copyOnWrite) {
            data = Reference2ObjectOpenHashMap(data)
            copyOnWrite = false
        }
    }

    override fun copy(): ItemDataContainer {
        return copy0()
    }

    override fun toBuilder(): ItemDataContainer.Builder {
        return copy0()
    }

    override fun build(): ItemDataContainer {
        return if (isEmpty()) EmptyItemDataContainer else this
    }

    // FIXME #350: 需要确保 node 的 loader 加载了 ItemDataContainer 所需要的所有 TypeSerializer
    //  具体来说, 是 ItemDataContainer 里面的数据类型的 TypeSerializer, 而非 ItemDataContainer 本身
    object Serializer : TypeSerializer<ItemDataContainer> {
        override fun deserialize(type: Type, node: ConfigurationNode): ItemDataContainer {
            val builder = ItemDataContainer.builder()
            for ((rawNodeKey, itemDataNode) in node.childrenMap()) {
                val nodeKey = rawNodeKey.toString()
                // 注意: 该 node key 所对应的 type 必须存在
                val dataType = KoishRegistries2.ITEM_DATA_TYPE[nodeKey] ?: run {
                    LOGGER.error("Unknown item data type: '$nodeKey'. Skipped.")
                    continue
                }
                // 该 loader 必须加载了能够 deserialize 该类型的 TypeSerializer
                val dataValue = itemDataNode.get(dataType.typeToken) ?: run {
                    LOGGER.error("Failed to deserialize $dataType. Skipped.")
                    continue
                }
                builder.set0(dataType, dataValue)
            }
            return builder.build()
        }

        override fun serialize(type: Type, obj: ItemDataContainer?, node: ConfigurationNode) {
            if (obj == null) return
            if (obj !is ItemDataContainerImpl) {
                LOGGER.error("Only expects ${ItemDataContainerImpl::class.qualifiedName}, but got ${obj::class.qualifiedName}")
                return
            }

            val iter = obj.data.reference2ObjectEntrySet().fastIterator()
            while (iter.hasNext()) {
                val (dataType, dataValue) = iter.next()
                val dataTypeId = KoishRegistries2.ITEM_DATA_TYPE.getId(dataType) ?: run {
                    LOGGER.error("Unknown item data type: $dataType. Skipped.")
                    continue
                }
                node.node(dataTypeId.asString()).set(dataValue)
            }
        }
    }
}
