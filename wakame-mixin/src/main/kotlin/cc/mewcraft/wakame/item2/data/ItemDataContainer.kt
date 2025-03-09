// ItemData
// ItemData.WriteOnly (可用于现有的原版代理, 以程序化生成原版的数据)
// ItemData.ReadOnly (无实际意义)
// ItemData.ReadWrite

// ItemData 需要具有 lifecycle 属性, 用来表示可以在什么时候使用

// ItemDataContainer

package cc.mewcraft.wakame.item2.data

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.util.typeTokenOf
import com.mojang.serialization.Codec
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.extra.dfu.v8.DfuSerializers
import java.lang.reflect.Type


// FIXME 这个数据类型将成为 NMS DataComponentMap 的一部分.
//  而根据 DataComponentMap 的契约, 该实例的数据必须为不可变, 所有 set 函数都不应该修改 `this` 的数据.
//  从 ItemStack 来看, 如果要修改一个 ItemData, 则大概流程为:
//  1) 先从 DataComponentMap 获取 ItemDataContainer
//  2) 使用 set 设置新的 ItemData, 返回一个新的 ItemDataContainer
//  3) 将新的 ItemDataContainer 放回 DataComponentMap
//  ---
//  等等, 似乎只需要把 DataComponentMap 的不可变契约的设计用在 ItemDataContainer 上就行.
class ItemDataContainer(
    private var data: Reference2ObjectOpenHashMap<ItemDataType<*>, Any>,
) : Iterable<Map.Entry<ItemDataType<*>, Any>> {

    companion object {

        // Codec 的创建时机必须晚于本类 static block 初始化的时机
        @JvmStatic
        fun makeCodec(): Codec<ItemDataContainer> {
            val codec = DfuSerializers.codec(typeTokenOf<ItemDataContainer>(), ItemDataTypes.serializers())
            requireNotNull(codec) { "Cannot find an appropriate TypeSerializer for ${ItemDataContainer::class}" }
            return codec
        }

        @JvmStatic
        fun makeSerializer(): TypeSerializer<ItemDataContainer> {
            return Serializer
        }

    }

    private var copyOnWrite: Boolean = false

    /**
     * 创建空的容器.
     */
    constructor() : this(Reference2ObjectOpenHashMap())

    val types: Set<ItemDataType<*>>
        get() = data.keys

    val size: Int
        get() = data.size

    fun isEmpty(): Boolean {
        return this.size == 0
    }

    fun <T> get(type: ItemDataType<out T>): T? {
        return data[type] as? T
    }

    fun has(type: ItemDataType<*>): Boolean {
        return data.containsKey(type)
    }

    fun <T> getOrDefault(type: ItemDataType<out T>, fallback: T?): T? {
        return get(type) ?: fallback
    }

    fun <T> set(type: ItemDataType<in T>, value: T): T? {
        ensureContainerOwnership()
        return data.put(type, value) as T?
    }

    fun <T> remove(type: ItemDataType<out T>): T? {
        ensureContainerOwnership()
        return data.remove(type) as T?
    }

    override fun iterator(): Iterator<Map.Entry<ItemDataType<*>, Any>> {
        return data.entries.iterator()
    }

    fun fastIterator(): Iterator<Map.Entry<ItemDataType<*>, Any>> {
        return data.reference2ObjectEntrySet().fastIterator()
    }

    fun copy(): ItemDataContainer {
        copyOnWrite = true
        return ItemDataContainer(data)
    }

    private fun ensureContainerOwnership() {
        if (copyOnWrite) {
            data = Reference2ObjectOpenHashMap(data)
            copyOnWrite = false
        }
    }

    // FIXME 需要确保 node 的 loader 加载了 ItemDataContainer 所需要的所有 TypeSerializer
    private object Serializer : TypeSerializer<ItemDataContainer> {
        override fun deserialize(type: Type, node: ConfigurationNode): ItemDataContainer {
            val data = Reference2ObjectOpenHashMap<ItemDataType<*>, Any>()
            for ((rawNodeKey, itemDataNode) in node.childrenMap()) {
                val nodeKey = rawNodeKey.toString()
                // 该 node key 所对应的 type 必须存在
                val dataType = ItemDataTypes.getType(nodeKey) ?: run {
                    LOGGER.error("Unknown ItemDataType: '$nodeKey'")
                    continue
                }
                // 该 loader 必须加载了能够 deserialize 该类型的 TypeSerializer
                val dataValue = itemDataNode.get(dataType.typeToken) ?: run {
                    LOGGER.error("Failed to deserialize $dataType")
                    continue
                }
                data[dataType] = dataValue
            }
            return ItemDataContainer(data)
        }

        override fun serialize(type: Type, obj: ItemDataContainer?, node: ConfigurationNode) {
            if (obj == null) return
            obj.data.reference2ObjectEntrySet().fastForEach { (type, value) ->
                val typeId = ItemDataTypes.getId(type)
                val dataNode = node.node(typeId)
                dataNode.set(value)
            }
        }
    }

}