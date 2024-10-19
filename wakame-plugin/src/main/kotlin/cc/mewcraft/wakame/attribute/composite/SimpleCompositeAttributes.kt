package cc.mewcraft.wakame.attribute.composite

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.BinarySerializable
import cc.mewcraft.wakame.attribute.*
import cc.mewcraft.wakame.attribute.AttributeModifier.*
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.*
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import java.util.stream.Stream

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
 * ```
 *
 * ## 对于 ConstantCompositeAttributeSE
 *
 * ```NBT
 * byte('op'): <operation>
 * byte('value'): <double>
 * byte('element'): <element>
 * ```
 *
 * ## 对于 ConstantCompositeAttributeR
 *
 * ```NBT
 * byte('op'): <operation>
 * byte('lower'): <double>
 * byte('upper'): <double>
 * ```
 *
 * ## 对于 ConstantCompositeAttributeRE
 *
 * ```NBT
 * byte('op'): <operation>
 * byte('lower'): <double>
 * byte('upper'): <double>
 * byte('element'): <element>
 * ```
 */
fun ConstantCompositeAttribute(
    id: String, tag: CompoundTag
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
    id: String, node: ConfigurationNode
): ConstantCompositeAttribute {
    return AttributeRegistry.FACADES[id].convertNode2Constant(node)
}

/**
 * 代表一个数值恒定的 [CompositeAttribute].
 */
sealed class ConstantCompositeAttribute : Examinable, BinarySerializable<CompoundTag>, CompositeAttribute, AttributeModifierSource {

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

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("id", id),
        ExaminableProperty.of("operation", operation),
    )

    override fun toString(): String = toSimpleString()
}

internal data class ConstantCompositeAttributeS(
    override val id: String,
    override val operation: Operation,
    override val value: Double,
) : ConstantCompositeAttribute(), CompositeAttributeS<Double> {
    constructor(
        id: String, compound: CompoundTag,
    ) : this(
        id,
        compound.readOperation(),
        compound.readNumber(AttributeBinaryKeys.SINGLE_VALUE)
    )

    override fun similarTo(other: ConstantCompositeAttribute): Boolean {
        return other is ConstantCompositeAttributeS &&
                other.id == id &&
                other.operation == operation
    }

    override fun serializeAsTag(): CompoundTag = CompoundTag {
        writeOperation(operation)
        writeNumber(AttributeBinaryKeys.SINGLE_VALUE, value)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.concat(
        super.examinableProperties(),
        Stream.of(
            ExaminableProperty.of("value", value),
        )
    )
}

internal data class ConstantCompositeAttributeR(
    override val id: String,
    override val operation: Operation,
    override val lower: Double,
    override val upper: Double,
) : ConstantCompositeAttribute(), CompositeAttributeR<Double> {
    constructor(
        id: String, compound: CompoundTag
    ) : this(
        id,
        compound.readOperation(),
        compound.readNumber(AttributeBinaryKeys.RANGED_MIN_VALUE),
        compound.readNumber(AttributeBinaryKeys.RANGED_MAX_VALUE)
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
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.concat(
        super.examinableProperties(),
        Stream.of(
            ExaminableProperty.of("lower", lower),
            ExaminableProperty.of("upper", upper),
        )
    )
}

internal data class ConstantCompositeAttributeSE(
    override val id: String,
    override val operation: Operation,
    override val value: Double,
    override val element: Element,
) : ConstantCompositeAttribute(), CompositeAttributeSE<Double> {
    constructor(
        id: String, compound: CompoundTag
    ) : this(
        id,
        compound.readOperation(),
        compound.readNumber(AttributeBinaryKeys.SINGLE_VALUE),
        compound.readElement()
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
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.concat(
        super.examinableProperties(),
        Stream.of(
            ExaminableProperty.of("value", value),
            ExaminableProperty.of("element", element.uniqueId),
        )
    )
}

internal data class ConstantCompositeAttributeRE(
    override val id: String,
    override val operation: Operation,
    override val lower: Double,
    override val upper: Double,
    override val element: Element,
) : ConstantCompositeAttribute(), CompositeAttributeRE<Double> {
    constructor(
        id: String, compound: CompoundTag
    ) : this(
        id,
        compound.readOperation(),
        compound.readNumber(AttributeBinaryKeys.RANGED_MIN_VALUE),
        compound.readNumber(AttributeBinaryKeys.RANGED_MAX_VALUE),
        compound.readElement()
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
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.concat(
        super.examinableProperties(),
        Stream.of(
            ExaminableProperty.of("lower", lower),
            ExaminableProperty.of("upper", upper),
            ExaminableProperty.of("element", element.uniqueId),
        )
    )
}

// private const val NBT_TYPE_ID = "id"

// private fun CompoundTag.readType(): Key {
//     return Key(this.getString(NBT_TYPE_ID))
// }

private fun CompoundTag.readElement(): Element {
    return this.getByteOrNull(AttributeBinaryKeys.ELEMENT_TYPE)?.let { ElementRegistry.getBy(it) } ?: ElementRegistry.DEFAULT
}

private fun CompoundTag.readOperation(): Operation {
    val id = this.getInt(AttributeBinaryKeys.OPERATION_TYPE)
    return Operation.byId(id) ?: error("Can't find operation with id '$id'")
}

private fun CompoundTag.readNumber(key: String): Double {
    return this.getDouble(key)
}

// private fun CompoundTag.writeId(id: Key) {
//     this.putString(NBT_TYPE_ID, id.asString())
// }

private fun CompoundTag.writeNumber(key: String, value: Double) {
    this.putDouble(key, value)
}

private fun CompoundTag.writeElement(element: Element) {
    this.putByte(AttributeBinaryKeys.ELEMENT_TYPE, element.binaryId)
}

private fun CompoundTag.writeOperation(operation: Operation) {
    this.putByte(AttributeBinaryKeys.OPERATION_TYPE, operation.binary)
}
