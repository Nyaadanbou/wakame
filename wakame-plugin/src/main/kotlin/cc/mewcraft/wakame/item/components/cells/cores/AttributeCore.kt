package cc.mewcraft.wakame.item.components.cells.cores

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.TagType
import cc.mewcraft.wakame.attribute.AttributeBinaryKeys
import cc.mewcraft.wakame.attribute.composite.ConstantCompositeAttribute
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.Cell
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.CoreConstants
import cc.mewcraft.wakame.registry.AttributeRegistry
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.ConfigurationNode
import xyz.xenondevs.commons.collections.mapToByteArray

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
    quality: Array<AttributeCore.Quality>?,
): AttributeCore = SimpleAttributeCore(
    id = id,
    attribute = attribute,
    quality = quality
)

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
): AttributeCore = SimpleAttributeCore(
    id = id,
    attribute = AttributeRegistry.FACADES[id.value()].convertNBT2Constant(nbt),
    quality = nbt.readQuality()
)

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
): AttributeCore = SimpleAttributeCore(
    id = id,
    attribute = AttributeRegistry.FACADES[id.value()].convertNode2Constant(node),
    quality = null // 从配置文件直接创建的属性核心应该不存在数值质量
)

/**
 * [AttributeCore] 的标准实现.
 */
internal data class SimpleAttributeCore(
    override val id: Key,
    override val attribute: ConstantCompositeAttribute,
    override val quality: Array<AttributeCore.Quality>?,
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
        baseTag.writeQuality(quality)

        return baseTag
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (javaClass != other?.javaClass)
            return false
        other as SimpleAttributeCore
        if (id != other.id)
            return false
        if (attribute != other.attribute)
            return false
        if (!quality.contentEquals(other.quality))
            return false
        if (displayName != other.displayName)
            return false
        if (description != other.description)
            return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + attribute.hashCode()
        result = 31 * result + (quality?.contentHashCode() ?: 0)
        result = 31 * result + displayName.hashCode()
        result = 31 * result + description.hashCode()
        return result
    }
}

private fun CompoundTag.writeId(id: Key) {
    putString(CoreConstants.NBT_CORE_ID, id.asString())
}

private fun CompoundTag.readQuality(): Array<AttributeCore.Quality>? {
    if (!contains(AttributeBinaryKeys.QUALITY, TagType.BYTE_ARRAY))
        return null
    val byteArray = getByteArray(AttributeBinaryKeys.QUALITY)
    val objArray = Array<AttributeCore.Quality>(byteArray.size) { AttributeCore.Quality.entries[byteArray[it].toInt()] }
    return objArray
}

private fun CompoundTag.writeQuality(quality: Array<AttributeCore.Quality>?) {
    if (quality == null)
        return
    putByteArray(AttributeBinaryKeys.QUALITY, quality.mapToByteArray { it.ordinal.toByte() })
}
