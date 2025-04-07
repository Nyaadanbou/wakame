package cc.mewcraft.wakame.item2.data.impl

import cc.mewcraft.wakame.entity.attribute.Attribute
import cc.mewcraft.wakame.entity.attribute.AttributeModifier
import cc.mewcraft.wakame.entity.attribute.bundle.ConstantAttributeBundle
import cc.mewcraft.wakame.item2.config.property.impl.ItemSlot
import cc.mewcraft.wakame.item2.typeId
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.Registry
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.serialization.configurate.serializer.DispatchingSerializer
import cc.mewcraft.wakame.serialization.configurate.serializer.valueByNameTypeSerializer
import cc.mewcraft.wakame.util.adventure.withValue
import cc.mewcraft.wakame.util.register
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.Multimap
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.typedSet
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import xyz.xenondevs.commons.collections.removeIf
import java.lang.reflect.Type
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * 核心的容器.
 *
 * 该接口的所有函数都不会修改容器本身.
 */
interface CoreContainer : Iterable<Map.Entry<String, Core>> {

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
    fun filter(predicate: (Core) -> Boolean): CoreContainer

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
    interface Builder {

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

sealed interface Core {

    companion object {

        fun makeDirectSerializers(): TypeSerializerCollection {
            val serials = TypeSerializerCollection.builder()
            serials.register<CoreType>(BuiltInRegistries.CORE_TYPE.valueByNameTypeSerializer())
            serials.register<Core>(DispatchingSerializer.create(Core::type, CoreType::kotlinType))
            //
            serials.register<VirtualCore>(VirtualCore.SERIALIZER)
            serials.register<EmptyCore>(EmptyCore.SERIALIZER)
            serials.registerAll(AttributeCore.SERIALIZERS)
            //
            return serials.build()
        }

        fun empty(): Core {
            return EmptyCore
        }

        fun virtual(): Core {
            return VirtualCore
        }
    }

    val type: CoreType

    /**
     * 核心的显示名称, 不包含具体数值.
     */
    val displayName: Component

    /**
     * 核心的完整描述, 包含具体数值和机制说明.
     */
    val description: List<Component>

    /**
     * 检查该核心是否跟 [other] 相似. 具体结果由实现决定.
     */
    fun similarTo(other: Core): Boolean

}

class CoreType(val kotlinType: KType)

object CoreTypes {
    @JvmField
    val EMPTY: CoreType = register<EmptyCore>("empty")
    @JvmField
    val VIRTUAL: CoreType = register<VirtualCore>("virtual")
    @JvmField
    val ATTRIBUTE: CoreType = register<AttributeCore>("attribute")

    private inline fun <reified E : Core> register(id: String): CoreType {
        return Registry.register(BuiltInRegistries.CORE_TYPE, id, CoreType(typeOf<E>()))
    }
}

/**
 * [VirtualCore] 代表永远不会被写入物品持久化数据容器的核心.
 *
 * 用来引导系统在生成 Koish 物品时, 不要把当前核孔写入物品.
 * 因此这个核心永远不会出现在游戏内的物品上, 不然就是 BUG.
 */
data object VirtualCore : Core {
    @JvmField
    val SERIALIZER: TypeSerializer2<VirtualCore> = object : TypeSerializer2<VirtualCore> {
        override fun deserialize(type: Type, node: ConfigurationNode): VirtualCore? {
            return if (!node.virtual()) VirtualCore else null
        }

        override fun serialize(type: Type, obj: VirtualCore?, node: ConfigurationNode) {
            return // 虚拟核心不需要序列化 (写入 NBT/配置文件)
        }
    }

    override val type: CoreType = CoreTypes.VIRTUAL
    // TODO #373: 从配置文件读取 displayName, description
    override val displayName: Component = Component.text("Virtual Core")
    override val description: List<Component> = listOf()
    override fun similarTo(other: Core): Boolean = other === this
}

/**
 * [EmptyCore] 是一个特殊核心, 表示这个核心不存在.
 *
 * 当一个核孔里没有核心时 (但核孔本身存在), 里面实际上存放了一颗空核心.
 * 玩家概念上的“核孔没有核心”, 就是技术概念上的 “核孔里装的是空核心”.
 */
data object EmptyCore : Core {
    @JvmField
    val SERIALIZER: TypeSerializer2<EmptyCore> = object : TypeSerializer2<EmptyCore> {
        override fun deserialize(type: Type, node: ConfigurationNode): EmptyCore? {
            return if (!node.virtual()) EmptyCore else null
        }

        override fun serialize(type: Type, obj: EmptyCore?, node: ConfigurationNode) {
            if (obj == null) {
                node.set(null)
                return
            }
        }
    }

    override val type: CoreType = CoreTypes.EMPTY
    // TODO #373: 从配置文件读取 displayName, description
    override val displayName: Component = Component.text("Empty Core")
    override val description: List<Component> = listOf()
    override fun similarTo(other: Core): Boolean = other === this
}

/**
 * [AttributeCore] 是一个属性核心, 用于表示一个 [ConstantAttributeBundle].
 *
 * @property wrapped 该属性核心的属性数据
 */
@ConfigSerializable
data class AttributeCore(
    @Setting("value")
    val wrapped: ConstantAttributeBundle,
) : Core {

    companion object {
        @JvmField
        val SERIALIZERS: TypeSerializerCollection = ConstantAttributeBundle.SERIALIZERS
    }

    override val type: CoreType
        get() = CoreTypes.ATTRIBUTE
    override val displayName: Component
        get() = wrapped.displayName
    override val description: List<Component>
        get() = wrapped.description

    /**
     * 检查两个属性核心是否拥有一样的:
     * - 运算模式
     * - 数值结构
     * - 元素类型 (如果存在)
     *
     * 该函数不会检查任何数值的相等性.
     */
    override fun similarTo(other: Core): Boolean {
        return other is AttributeCore && wrapped.similarTo(other.wrapped)
    }
}

// ------------
// 内部实现
// ------------

private object EmptyCoreContainer : CoreContainer {
    override fun contains(id: String): Boolean = false
    override fun get(id: String): Core? = null
    override fun filter(predicate: (Core) -> Boolean): CoreContainer = this
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

    override fun contains(id: String): Boolean {
        return dataMap.containsKey(id)
    }

    override fun get(id: String): Core? {
        return dataMap[id]
    }

    // 即使全部符合 predicate, 也永远返回一个新的 CoreContainer
    override fun filter(predicate: (Core) -> Boolean): CoreContainer {
        val filteredMap = Object2ObjectArrayMap(dataMap)
        filteredMap.removeIf { !predicate(it.value) }
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
        return SimpleCoreContainer(dataMap, copyOnWrite = true)
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