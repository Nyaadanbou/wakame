package cc.mewcraft.wakame.item.data.impl

import cc.mewcraft.wakame.entity.attribute.Attribute
import cc.mewcraft.wakame.entity.attribute.AttributeModifier
import cc.mewcraft.wakame.item.property.impl.ItemSlot
import cc.mewcraft.wakame.item.typeId
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.adventure.withValue
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.Multimap
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.typedSet
import xyz.xenondevs.commons.collections.removeIf
import java.lang.reflect.Type

/**
 * 核心的容器.
 *
 * 该接口的所有函数都不会修改容器本身.
 *
 * @see Core
 */
sealed interface CoreContainer : Iterable<Map.Entry<String, Core>> {

    companion object {

        @JvmField
        val SERIALIZER: TypeSerializer2<CoreContainer> = SimpleCoreContainer.Serializer

        @JvmField
        val EMPTY: CoreContainer = EmptyCoreContainer

        fun of(dataMap: Map<String, Core>): CoreContainer {
            return build { dataMap.forEach(::put) }
        }

        fun build(block: Builder.() -> Unit): CoreContainer {
            return builder().apply(block).build()
        }

        fun builder(): Builder {
            return SimpleCoreContainer(copyOnWrite = false)
        }
    }

    /**
     * 返回核心的数量.
     */
    val size: Int

    /**
     * 检查 [id] 核孔对应的核心是否存在.
     *
     * 注意: 空核心也是核心, 此函数将返回 `true`.
     */
    operator fun contains(id: String): Boolean

    /**
     * 获取 [id] 核孔对应的核心.
     */
    operator fun get(id: String): Core?

    /**
     * 基于当前容器创建一个新的 [CoreContainer], 其中只包含满足 [predicate] 的核心.
     */
    fun filter(predicate: (String, Core) -> Boolean): CoreContainer

    /**
     * 基于当前容器创建一个新的 [CoreContainer], 其中 [id] 对应的核心是修改后的版本.
     */
    fun modify(id: String, block: (Core) -> Core): CoreContainer

    /**
     * 遍历所有核心, 对每个核心执行 [action].
     */
    fun forEach(action: (String, Core) -> Unit)

    /**
     * 返回所有核心上的 [AttributeModifier].
     */
    fun collectAttributeModifiers(context: ItemStack, slot: ItemSlot): Multimap<Attribute, AttributeModifier>

    /**
     * 忽略数值的前提下, 判断是否包含指定的核心.
     */
    fun containsSimilarCore(core: Core): Boolean

    /**
     * 返回一个生成器, 以基于当前状态构建一个新的 [CoreContainer].
     */
    fun toBuilder(): Builder

    /**
     * [CoreContainer] 的生成器.
     */
    sealed interface Builder {

        /**
         * 将核心 [core] 写入到 [id] 核孔. 将覆盖已有的核心.
         */
        fun put(id: String, core: Core): Builder

        /**
         * 移除 [id] 核孔里的核心.
         */
        fun remove(id: String): Builder

        /**
         * 构建.
         */
        fun build(): CoreContainer
    }
}

// ------------
// 内部实现
// ------------

private object EmptyCoreContainer : CoreContainer {
    override val size: Int = 0
    override fun contains(id: String): Boolean = false
    override fun get(id: String): Core? = null
    override fun filter(predicate: (String, Core) -> Boolean): CoreContainer = this
    override fun modify(id: String, block: (Core) -> Core): CoreContainer = this
    override fun forEach(action: (String, Core) -> Unit) = Unit
    override fun iterator(): Iterator<Map.Entry<String, Core>> = emptyMap<String, Core>().iterator()
    override fun collectAttributeModifiers(context: ItemStack, slot: ItemSlot): Multimap<Attribute, AttributeModifier> = ImmutableMultimap.of()
    override fun containsSimilarCore(core: Core): Boolean = false
    override fun toBuilder(): CoreContainer.Builder = SimpleCoreContainer(copyOnWrite = false)
}

private class SimpleCoreContainer(
    // 通常来说词条栏的数量不会大于16个, 并且绝大部分情况下都是遍历全部而不是查询单个, 所以 ArrayMap 更合适
    private var dataMap: Object2ObjectArrayMap<String, Core> = Object2ObjectArrayMap(),
    // copyOnWrite 标记用于优化性能和内存占用
    private var copyOnWrite: Boolean,
) : CoreContainer, CoreContainer.Builder {

    object Serializer : TypeSerializer2<CoreContainer> {
        override fun deserialize(type: Type, node: ConfigurationNode): CoreContainer? {
            val dataMap = node.get<Map<String, Core>>() ?: return CoreContainer.EMPTY
            val container = CoreContainer.of(dataMap)
            return container
        }

        override fun serialize(type: Type, obj: CoreContainer?, node: ConfigurationNode) {
            if (obj == null) {
                node.set(null)
                return
            }

            if (obj !is SimpleCoreContainer) {
                return
            }

            node.typedSet<Map<String, Core>>(obj.dataMap)
        }
    }

    override val size: Int
        get() = dataMap.size

    override fun contains(id: String): Boolean {
        return dataMap.containsKey(id)
    }

    override fun get(id: String): Core? {
        return dataMap[id]
    }

    // 即使全部符合 predicate, 也永远返回一个新的 CoreContainer
    override fun filter(predicate: (String, Core) -> Boolean): CoreContainer {
        val filteredMap = Object2ObjectArrayMap(dataMap)
        filteredMap.removeIf { !predicate(it.key, it.value) }
        return SimpleCoreContainer(dataMap = filteredMap, copyOnWrite = false)
    }

    // 即使没有核心被修改, 也永远返回一个新的 CoreContainer
    override fun modify(id: String, block: (Core) -> Core): CoreContainer {
        val oldVal = dataMap[id]
        val newVal = oldVal?.let(block)
        val modifiedMap = Object2ObjectArrayMap(dataMap)
        if (newVal != null) {
            modifiedMap.put(id, newVal)
        }
        return SimpleCoreContainer(dataMap = modifiedMap, copyOnWrite = false)
    }

    override fun forEach(action: (String, Core) -> Unit) {
        for (entry in dataMap.object2ObjectEntrySet().fastIterator()) {
            action(entry.key, entry.value)
        }
    }

    override fun iterator(): Iterator<Map.Entry<String, Core>> {
        return dataMap.object2ObjectEntrySet().fastIterator()
    }

    override fun collectAttributeModifiers(context: ItemStack, slot: ItemSlot): Multimap<Attribute, AttributeModifier> {
        val collected = ImmutableListMultimap.builder<Attribute, AttributeModifier>()
        for ((id, core) in iterator()) {
            val attr = (core as? AttributeCore)?.wrapped ?: continue
            val modifierId = context.typeId.withValue { "$it/${slot.index}/$id" }
            val attrModifiers = attr.createAttributeModifiers(modifierId)
            collected.putAll(attrModifiers.entries)
        }
        return collected.build()
    }

    override fun containsSimilarCore(core: Core): Boolean {
        return dataMap.object2ObjectEntrySet().any { it.value.similarTo(core) }
    }

    override fun toBuilder(): CoreContainer.Builder {
        copyOnWrite = true
        return SimpleCoreContainer(dataMap = dataMap, copyOnWrite = true)
    }

    // Builder

    override fun put(id: String, core: Core): CoreContainer.Builder {
        ensureContainerOwnership()
        dataMap.put(id, core)
        return this
    }

    override fun remove(id: String): CoreContainer.Builder {
        ensureContainerOwnership()
        dataMap.remove(id)
        return this
    }

    override fun build(): CoreContainer {
        return if (dataMap.isEmpty()) CoreContainer.EMPTY else this
    }

    private fun ensureContainerOwnership() {
        if (copyOnWrite) {
            dataMap = Object2ObjectArrayMap(dataMap)
            copyOnWrite = false
        }
    }

    // 正确实现 equals & hashCode 以支持相同物品能够堆叠

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimpleCoreContainer) return false
        if (dataMap != other.dataMap) return false
        return true
    }

    override fun hashCode(): Int {
        return dataMap.hashCode()
    }

    override fun toString(): String {
        return "{${
            dataMap.object2ObjectEntrySet().joinToString(
                separator = ", ",
                prefix = "(",
                postfix = ")"
            ) { (key, value) -> "$key=$value" }
        }}"
    }

}