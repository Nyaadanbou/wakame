package cc.mewcraft.wakame.item.components.cells.cores

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.attribute.composite.ConstantCompositeAttribute
import cc.mewcraft.wakame.item.components.cells.*
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import java.util.stream.Stream

val Cell.attributeCore: AttributeCore?
    get() = getCore() as? AttributeCore

val Cell.attribute: ConstantCompositeAttribute?
    get() = attributeCore?.attribute

/**
 * 本函数用于直接构建 [AttributeCore].
 *
 * @param id 核心的唯一标识, 也就是 [Core.id]
 * @param attribute 该核心的属性
 *
 * @return 构建的 [AttributeCore]
 */
fun AttributeCore(
    id: Key,
    attribute: ConstantCompositeAttribute,
): AttributeCore {
    return SimpleAttributeCore(id, attribute)
}

/**
 * 本函数用于从 NBT 构建 [AttributeCore].
 *
 * 参考 [ConstantCompositeAttribute] 来了解给定的 [CompoundTag] 需要满足的结构.
 *
 * @param id 核心的唯一标识, 也就是 [Core.id]
 * @param nbt 包含该核心数据的 NBT
 *
 * @return 从 NBT 构建的 [AttributeCore]
 */
fun AttributeCore(
    id: Key,
    nbt: CompoundTag,
): AttributeCore {
    val compositeAttributeId = id.value()
    val compositeAttributeFacade = AttributeRegistry.FACADES[compositeAttributeId]
    val compositeAttribute = compositeAttributeFacade.convertNBT2Constant(nbt)
    return SimpleAttributeCore(id, compositeAttribute)
}

/**
 * 本函数用于从配置文件构建 [AttributeCore].
 *
 * 参考 [ConstantCompositeAttribute] 来了解给定的 [ConfigurationNode] 需要满足的结构.
 *
 * @param id 核心的唯一标识, 也就是 [Core.id]
 * @param node 包含该核心数据的配置节点
 *
 * @return 从配置文件构建的 [AttributeCore]
 */
fun AttributeCore(
    id: Key,
    node: ConfigurationNode,
): AttributeCore {
    val compositeAttributeId = id.value()
    val compositeAttributeFacade = AttributeRegistry.FACADES[compositeAttributeId]
    val compositeAttribute = compositeAttributeFacade.convertNode2Constant(node)
    return SimpleAttributeCore(id, compositeAttribute)
}

/**
 * [AttributeCore] 的标准实现.
 */
internal data class SimpleAttributeCore(
    override val id: Key,
    override val attribute: ConstantCompositeAttribute,
) : AttributeCore {
    override val displayName: Component
        get() = attribute.displayName
    override val description: List<Component>
        get() = attribute.description

    /**
     * 检查两个属性核心是否拥有一样的:
     * - 运算模式
     * - 数值结构
     * - 元素类型 (如果有)
     *
     * 该函数不会检查任何数值的相等性.
     */
    override fun similarTo(other: Core): Boolean {
        return other is AttributeCore && attribute.similarTo(other.attribute)
    }

    override fun serializeAsTag(): CompoundTag {
        val attributeTag = attribute.serializeAsTag()

        val baseTag = CompoundTag.create()
        baseTag.writeId(id)
        baseTag.merge(attributeTag)

        return baseTag
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("id", id.asString()),
        ExaminableProperty.of("attribute", attribute),
    )

    override fun toString(): String = toSimpleString()
}

private fun CompoundTag.writeId(id: Key) {
    putString(CoreConstants.NBT_CORE_ID, id.asString())
}