package cc.mewcraft.wakame.attribute.composite

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.BinarySerializable
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeBinaryKeys
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.AttributeModifierSource
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.CompoundTag
import cc.mewcraft.wakame.util.getByteOrNull
import cc.mewcraft.wakame.util.getIntOrNull
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.ConfigurationNode

/**
 * 该属性核心的元素种类. 如果该属性核心没有元素, 则返回 `null`.
 */
val ConstantCompositeAttribute.element: Element?
    get() = (this as? CompositeAttributeComponent.Element)?.element

/**
 * 从 NBT 构建一个 [ConstantCompositeAttribute].
 *
 * 给定的 [CompoundTag] 必须是以下结构之一:
 *
 * ## 对于 ConstantCompositeAttributeS
 *
 * ```NBT
 * byte('op'): <operation>
 * byte('value'): <double>
 * byte('quality'): <quality>
 * ```
 *
 * ## 对于 ConstantCompositeAttributeSE
 *
 * ```NBT
 * byte('op'): <operation>
 * byte('value'): <double>
 * byte('element'): <element>
 * byte('quality'): <quality>
 * ```
 *
 * ## 对于 ConstantCompositeAttributeR
 *
 * ```NBT
 * byte('op'): <operation>
 * byte('lower'): <double>
 * byte('upper'): <double>
 * byte('quality'): <quality>
 * ```
 *
 * ## 对于 ConstantCompositeAttributeRE
 *
 * ```NBT
 * byte('op'): <operation>
 * byte('lower'): <double>
 * byte('upper'): <double>
 * byte('element'): <element>
 * byte('quality'): <quality>
 * ```
 */
fun ConstantCompositeAttribute(
    id: String, tag: CompoundTag,
): ConstantCompositeAttribute {
    return AttributeRegistry.FACADES[id].convertNBT2Constant(tag)
}

/**
 * 从配置文件构建一个 [ConstantCompositeAttribute].
 *
 * 给定的 [ConfigurationNode] 必须是以下结构之一:
 *
 * ## 对于 ConstantCompositeAttributeS
 *
 * 给定的 [node] 必须是以下结构:
 *
 * ```yaml
 * operation: <operation>
 * value: <double>
 * ```
 *
 * ## 对于 ConstantCompositeAttributeSE
 *
 * 给定的 [node] 必须是以下结构:
 *
 * ```yaml
 * operation: <operation>
 * value: <double>
 * element: <element>
 * ```
 *
 * ## 对于 ConstantCompositeAttributeR
 *
 * 给定的 [node] 必须是以下结构:
 *
 * ```yaml
 * operation: <operation>
 * lower: <double>
 * upper: <double>
 * ```
 *
 * ## 对于 ConstantCompositeAttributeRE
 *
 * 给定的 [node] 必须是以下结构:
 *
 * ```yaml
 * operation: <operation>
 * lower: <double>
 * upper: <double>
 * element: <element>
 * ```
 */
fun ConstantCompositeAttribute(
    id: String, node: ConfigurationNode,
): ConstantCompositeAttribute {
    return AttributeRegistry.FACADES[id].convertNode2Constant(node)
}

/**
 * 代表一个数值恒定的 [CompositeAttribute].
 */
sealed class ConstantCompositeAttribute : BinarySerializable<CompoundTag>, CompositeAttribute, AttributeModifierSource {

    /**
     * 数值的质量, 通常以正态分布的 Z-score 转换而来.
     *
     * 并不是每个复合属性都有数值的质量,
     * 例如用于铭刻的复合属性就没有数值质量,
     * 因为它们都已经在配置文件中固定好的.
     *
     * 如果该复合属性有多个数值, 例如 [CompositeAttributeR],
     * 那么只会储存 [CompositeAttributeR.upper]
     * 的数值质量.
     */
    abstract val quality: Quality?

    val displayName: Component
        get() = AttributeRegistry.FACADES[id].createTooltipName(this)
    val description: List<Component>
        get() = AttributeRegistry.FACADES[id].createTooltipLore(this)

    /**
     * 属性核心的“数值质量”.
     * [Quality.ordinal] 越小则数值质量越差, 反之越好.
     */
    enum class Quality {
        L3, L2, L1, MU, U1, U2, U3;

        companion object {
            /**
             * 从正态分布的 Z-score 转换为 [Quality].
             */
            fun fromZScore(score: Double): Quality {
                return when {
                    score < -3.0 -> L3
                    score < -2.0 -> L2
                    score < -1.0 -> L1
                    score < 1.0 -> MU
                    score < 2.0 -> U1
                    score < 3.0 -> U2
                    else -> U3
                }
            }
        }
    }

    /**
     * 检查两个 [ConstantCompositeAttribute] 是否拥有一样的:
     * - 运算模式
     * - 数值结构
     * - 元素类型 (如果有)
     *
     * 该函数不会检查任何数值的相等性.
     */
    abstract fun similarTo(other: ConstantCompositeAttribute): Boolean

    /**
     * 序列化为 NBT 标签. 请注意这并不包含 [id] 的信息.
     */
    abstract override fun serializeAsTag(): CompoundTag

    override fun provideAttributeModifiers(sourceId: Key): Map<Attribute, AttributeModifier> {
        return AttributeRegistry.FACADES[id].createAttributeModifiers(sourceId, this)
    }
}

internal data class ConstantCompositeAttributeS(
    override val id: String,
    override val operation: Operation,
    override val value: Double,
    override val quality: Quality? = null,
) : ConstantCompositeAttribute(), CompositeAttributeS<Double> {
    constructor(
        id: String, compound: CompoundTag,
    ) : this(
        id,
        compound.readOperation(),
        compound.readNumber(AttributeBinaryKeys.SINGLE_VALUE),
        compound.readQuality(),
    )

    override fun similarTo(other: ConstantCompositeAttribute): Boolean {
        return other is ConstantCompositeAttributeS &&
                other.id == id &&
                other.operation == operation
    }

    override fun serializeAsTag(): CompoundTag = CompoundTag {
        writeOperation(operation)
        writeNumber(AttributeBinaryKeys.SINGLE_VALUE, value)
        writeQuality(quality)
    }
}

internal data class ConstantCompositeAttributeR(
    override val id: String,
    override val operation: Operation,
    override val lower: Double,
    override val upper: Double,
    override val quality: Quality? = null,
) : ConstantCompositeAttribute(), CompositeAttributeR<Double> {
    constructor(
        id: String, compound: CompoundTag,
    ) : this(
        id,
        compound.readOperation(),
        compound.readNumber(AttributeBinaryKeys.RANGED_MIN_VALUE),
        compound.readNumber(AttributeBinaryKeys.RANGED_MAX_VALUE),
        compound.readQuality(),
    )

    override fun similarTo(other: ConstantCompositeAttribute): Boolean {
        return other is ConstantCompositeAttributeR &&
                other.id == id &&
                other.operation == operation
    }

    override fun serializeAsTag(): CompoundTag = CompoundTag {
        writeOperation(operation)
        writeNumber(AttributeBinaryKeys.RANGED_MIN_VALUE, lower)
        writeNumber(AttributeBinaryKeys.RANGED_MAX_VALUE, upper)
        writeQuality(quality)
    }
}

internal data class ConstantCompositeAttributeSE(
    override val id: String,
    override val operation: Operation,
    override val value: Double,
    override val element: Element,
    override val quality: Quality? = null,
) : ConstantCompositeAttribute(), CompositeAttributeSE<Double> {
    constructor(
        id: String, compound: CompoundTag,
    ) : this(
        id,
        compound.readOperation(),
        compound.readNumber(AttributeBinaryKeys.SINGLE_VALUE),
        compound.readElement(),
        compound.readQuality(),
    )

    override fun similarTo(other: ConstantCompositeAttribute): Boolean {
        return other is ConstantCompositeAttributeSE &&
                other.id == id &&
                other.operation == operation &&
                other.element == element
    }

    override fun serializeAsTag(): CompoundTag = CompoundTag {
        writeOperation(operation)
        writeNumber(AttributeBinaryKeys.SINGLE_VALUE, value)
        writeElement(element)
        writeQuality(quality)
    }
}

internal data class ConstantCompositeAttributeRE(
    override val id: String,
    override val operation: Operation,
    override val lower: Double,
    override val upper: Double,
    override val element: Element,
    override val quality: Quality? = null,
) : ConstantCompositeAttribute(), CompositeAttributeRE<Double> {
    constructor(
        id: String, compound: CompoundTag,
    ) : this(
        id,
        compound.readOperation(),
        compound.readNumber(AttributeBinaryKeys.RANGED_MIN_VALUE),
        compound.readNumber(AttributeBinaryKeys.RANGED_MAX_VALUE),
        compound.readElement(),
        compound.readQuality(),
    )

    override fun similarTo(other: ConstantCompositeAttribute): Boolean {
        return other is ConstantCompositeAttributeRE
                && other.id == id
                && other.operation == operation
                && other.element == element
    }

    override fun serializeAsTag(): CompoundTag = CompoundTag {
        writeOperation(operation)
        writeNumber(AttributeBinaryKeys.RANGED_MIN_VALUE, lower)
        writeNumber(AttributeBinaryKeys.RANGED_MAX_VALUE, upper)
        writeElement(element)
        writeQuality(quality)
    }
}

private fun CompoundTag.readElement(): Element {
    return getByteOrNull(AttributeBinaryKeys.ELEMENT_TYPE)?.let { ElementRegistry.getBy(it) } ?: ElementRegistry.DEFAULT
}

private fun CompoundTag.readOperation(): Operation {
    val id = getInt(AttributeBinaryKeys.OPERATION_TYPE)
    return Operation.byId(id) ?: error("No such operation with id: $id")
}

private fun CompoundTag.readNumber(key: String): Double {
    return getDouble(key)
}

private fun CompoundTag.readQuality(): ConstantCompositeAttribute.Quality? {
    return getIntOrNull(AttributeBinaryKeys.QUALITY)?.let(ConstantCompositeAttribute.Quality.entries::get)
}

private fun CompoundTag.writeNumber(key: String, value: Double) {
    putDouble(key, value)
}

private fun CompoundTag.writeElement(element: Element) {
    putByte(AttributeBinaryKeys.ELEMENT_TYPE, element.binaryId)
}

private fun CompoundTag.writeOperation(operation: Operation) {
    putByte(AttributeBinaryKeys.OPERATION_TYPE, operation.binary)
}

private fun CompoundTag.writeQuality(quality: ConstantCompositeAttribute.Quality?) {
    quality?.run { putByte(AttributeBinaryKeys.QUALITY, ordinal.toByte()) }
}
