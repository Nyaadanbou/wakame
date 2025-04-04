package cc.mewcraft.wakame.item2.data

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.configurate.TypeSerializer2
import cc.mewcraft.wakame.item2.data.ItemDataContainer.Companion.build
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.serialization.configurate.STANDARD_SERIALIZERS
import cc.mewcraft.wakame.serialization.configurate.serializer.IdentifierSerializer
import cc.mewcraft.wakame.util.typeTokenOf
import com.mojang.serialization.Codec
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.jetbrains.annotations.ApiStatus
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.extra.dfu.v8.DfuSerializers
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.lang.reflect.Type


/**
 * 代表一个容器, 存放需要 *持久化* 的数据. 该容器实例与 [org.bukkit.inventory.ItemStack] 绑定.
 *
 * 该容器本身不可变, 容器内的数据也不可变. 违反此契约将导致此实例被克隆后出现数据错乱的问题.
 *
 * 如果要基于当前容器修改数据, 使用 [toBuilder] 创建一个 [Builder] 实例便可开始修改数据.
 * 修改完后再使用 [build] 创建一个新的 [ItemDataContainer] 实例, 便可获得修改后的版本.
 */
sealed interface ItemDataContainer : Iterable<Map.Entry<ItemDataType<*>, Any>> {

    companion object {

        @JvmStatic
        val EMPTY: ItemDataContainer = EmptyItemDataContainer

        /**
         * 该 [Codec] 调用的时机与 ItemStack 发生序列化的时机一致, 即:
         * - 服务端从持久化媒介读取 ItemStack 的数据时, 例如 读取区块和实体, 加载玩家物品栏
         * - 服务端往持久化媒介保存 ItemStack 的数据时, 例如 执行保存任务
         *
         * 例外: 这些序列化代码不会参与发包, 因为发包时不会发送任何自定义数据 (hint: Mixin).
         */
        @JvmStatic
        fun makeCodec(): Codec<ItemDataContainer> {
            val serials = TypeSerializerCollection.builder()

            // Codec 要求所有遇到的数据类型都有序列化操作可使用,
            // 因此这里需要再明确的添加一些潜在依赖的序列化操作.

            // 添加显式声明的 TypeSerializer
            serials.registerAll(makeDirectSerializers())
            // 添加间接依赖的 TypeSerializer (注册新的物品数据类型时, 如果有间接依赖的类型, 在这里添加即可)
            serials.register(IdentifierSerializer)
            // 添加 Configurate 内置的 TypeSerializer.
            // 注意: 按照 Configurate 的实现, 查询 TypeSerializer 的顺序 是按照 注册 TypeSerializer 的顺序 进行的.
            // 因此内置的 TypeSerializeCollection 必须在我们自定义的 ObjectMapper 之后注册, 否则在反序列化时,
            // 内置的 ObjectMapper 将被优先使用, 导致无法反序列化 Kotlin 的 data class.
            serials.registerAll(STANDARD_SERIALIZERS)

            val codec = DfuSerializers.codec(typeTokenOf<ItemDataContainer>(), serials.build())
            requireNotNull(codec) { "Cannot find an appropriate TypeSerializer for ${ItemDataContainer::class}" }

            return codec
        }

        @JvmStatic
        fun makeDirectSerializers(): TypeSerializerCollection {
            val serials = TypeSerializerCollection.builder()

            // 添加 ItemDataContainer 的 TypeSerializer
            serials.register(typeTokenOf<ItemDataContainer>(), SimpleItemDataContainer.Serializer)
            // 添加每一个 “Item Data” 的 TypeSerializer
            serials.registerAll(ItemDataTypes.directSerializers())

            return serials.build()
        }

        fun build(block: Builder.() -> Unit): ItemDataContainer {
            return builder().apply(block).build()
        }

        fun builder(): Builder {
            return SimpleItemDataContainer(copyOnWrite = true)
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
    sealed interface Builder : ItemDataContainer {

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
        @ApiStatus.Internal
        fun setUnsafe(type: ItemDataType<*>, value: Any): Any?

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

// ------------
// 内部实现
// ------------

private data object EmptyItemDataContainer : ItemDataContainer {
    override val types: Set<ItemDataType<*>> = emptySet()
    override val size: Int = 0
    override fun isEmpty(): Boolean = true
    override fun <T> get(type: ItemDataType<out T>): T? = null
    override fun has(type: ItemDataType<*>): Boolean = false
    override fun fastIterator(): Iterator<Map.Entry<ItemDataType<*>, Any>> = iterator()
    override fun copy(): ItemDataContainer = this
    override fun toBuilder(): ItemDataContainer.Builder = SimpleItemDataContainer(copyOnWrite = false)
    override fun iterator(): Iterator<Map.Entry<ItemDataType<*>, Any>> = emptyMap<ItemDataType<*>, Any>().iterator()
}

// 该 class 同时实现了 ItemDataContainer, ItemDataContainer.Builder.
private open class SimpleItemDataContainer(
    @JvmField
    var dataMap: Reference2ObjectOpenHashMap<ItemDataType<*>, Any> = Reference2ObjectOpenHashMap(),
    @JvmField
    var copyOnWrite: Boolean, // 用于优化 copy() 的性能
) : ItemDataContainer, ItemDataContainer.Builder {
    override val types: Set<ItemDataType<*>>
        get() = dataMap.keys

    override val size: Int
        get() = dataMap.size

    override fun isEmpty(): Boolean {
        return this.size == 0
    }

    override fun <T> get(type: ItemDataType<out T>): T? {
        return dataMap[type] as? T
    }

    override fun has(type: ItemDataType<*>): Boolean {
        return get(type) != null
    }

    override fun <T> set(type: ItemDataType<in T>, value: T): T? {
        ensureContainerOwnership()
        return dataMap.put(type, value) as T?
    }

    override fun setUnsafe(type: ItemDataType<*>, value: Any): Any? {
        // 警告: 实现上必须确保这里传入的 value 类型一定是正确的
        ensureContainerOwnership()
        return dataMap.put(type, value)
    }

    override fun <T> remove(type: ItemDataType<out T>): T? {
        ensureContainerOwnership()
        return dataMap.remove(type) as T?
    }

    override fun iterator(): Iterator<Map.Entry<ItemDataType<*>, Any>> {
        return dataMap.entries.iterator()
    }

    override fun fastIterator(): Iterator<Map.Entry<ItemDataType<*>, Any>> {
        return dataMap.reference2ObjectEntrySet().fastIterator()
    }

    private fun copy0(): SimpleItemDataContainer {
        copyOnWrite = true
        return SimpleItemDataContainer(dataMap, copyOnWrite = true)
    }

    private fun ensureContainerOwnership() {
        if (copyOnWrite) {
            dataMap = Reference2ObjectOpenHashMap(dataMap)
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
        return if (dataMap.isEmpty()) ItemDataContainer.EMPTY else this
    }

    // 正确实现 equals & hashCode 以支持相同物品能够堆叠

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is SimpleItemDataContainer
            && this.dataMap == other.dataMap
        ) {
            return true
        }
        return false
    }

    override fun hashCode(): Int {
        return dataMap.hashCode()
    }

    override fun toString(): String {
        return "{${
            joinToString(
                separator = ", ",
                prefix = "(",
                postfix = ")",
            ) { (key, value) ->
                "${KoishRegistries2.ITEM_DATA_TYPE.getId(key)!!.value()}=$value"
            }
        }}"
    }

    object Serializer : TypeSerializer2<ItemDataContainer> {
        override fun deserialize(type: Type, node: ConfigurationNode): ItemDataContainer {
            val builder = ItemDataContainer.builder()
            for ((rawNodeKey, itemDataNode) in node.childrenMap()) {
                val nodeKey = rawNodeKey.toString()
                // 注意: 该 node key 所对应的 type 必须存在
                val dataType = KoishRegistries2.ITEM_DATA_TYPE[nodeKey] ?: continue
                // 该 loader 必须加载了能够 deserialize 该类型的 TypeSerializer
                val dataValue = try {
                    itemDataNode.get(dataType.typeToken) ?: run {
                        LOGGER.error("Decoded item data value to null for $dataType. Skipped.")
                        continue
                    }
                } catch (ex: Throwable) {
                    LOGGER.error("An exception occurred while deserializing $dataType. Skipped. Reason: ${ex.message}")
                    continue
                }
                builder.setUnsafe(dataType, dataValue)
            }
            return builder.build()
        }

        override fun serialize(type: Type, obj: ItemDataContainer?, node: ConfigurationNode) {
            if (obj == null) return
            if (obj !is SimpleItemDataContainer) {
                LOGGER.error("Only expects ${SimpleItemDataContainer::class.qualifiedName}, but got ${obj::class.qualifiedName}")
                return
            }

            val iter = obj.fastIterator()
            while (iter.hasNext()) {
                val (dataType, dataValue) = iter.next()
                val dataTypeId = KoishRegistries2.ITEM_DATA_TYPE.getId(dataType) ?: continue
                val mapKey = dataTypeId.value() // 这里写入的 map key 省略了命名空间 "koish"
                val entryNode = node.node(mapKey)
                entryNode.set(
                    dataType.typeToken.type, // 不能用 ConfigurationNode.set(Object). 必须传入该数据的 TypeToken, 否则不支持带参数的数据类型
                    dataValue
                )
            }
        }
    }
}
