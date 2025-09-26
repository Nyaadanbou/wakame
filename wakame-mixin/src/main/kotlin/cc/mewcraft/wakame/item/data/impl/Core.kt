package cc.mewcraft.wakame.item.data.impl

import cc.mewcraft.wakame.entity.attribute.bundle.ConstantAttributeBundle
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.Registry
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.serialization.configurate.serializer.DispatchingSerializer
import cc.mewcraft.wakame.serialization.configurate.serializer.valueByNameTypeSerializer
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.registerExact
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.lang.reflect.Type
import kotlin.reflect.KType
import kotlin.reflect.typeOf


/**
 * 代表一个核心.
 *
 * @see CoreContainer
 */
sealed interface Core {

    companion object {

        fun serializers(): TypeSerializerCollection {
            val serials = TypeSerializerCollection.builder()
            serials.register<CoreType>(BuiltInRegistries.CORE_TYPE.valueByNameTypeSerializer())
            serials.registerExact<Core>(DispatchingSerializer.create(Core::type, CoreType::kotlinType))
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

class CoreType internal constructor(internal val kotlinType: KType)

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
