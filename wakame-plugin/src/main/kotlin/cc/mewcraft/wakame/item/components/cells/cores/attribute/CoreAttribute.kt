package cc.mewcraft.wakame.item.components.cells.cores.attribute

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeBinaryKeys
import cc.mewcraft.wakame.attribute.AttributeComponent
import cc.mewcraft.wakame.attribute.AttributeComponentGroupR
import cc.mewcraft.wakame.attribute.AttributeComponentGroupRE
import cc.mewcraft.wakame.attribute.AttributeComponentGroupS
import cc.mewcraft.wakame.attribute.AttributeComponentGroupSE
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.AttributeModifierSource
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.NameLine
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.CoreBinaryKeys
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.CoreType
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.CompoundTag
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.getByteOrNull
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.spongepowered.configurate.ConfigurationNode
import java.util.stream.Stream

val CoreAttribute.element: Element?
    get() = (this as? AttributeComponent.Element)?.element

/**
 * 从 NBT 构建一个 [CoreAttribute].
 *
 * 给定的 [nbt] 必须是以下结构 (仅供参考):
 * ```NBT
 * string('id'): <key>
 * byte('op'): <operation>
 * ...
 * ```
 */
fun CoreAttribute(
    nbt: CompoundTag,
): CoreAttribute {
    val key = Key(nbt.getString(CoreBinaryKeys.CORE_IDENTIFIER))
    val facade = AttributeRegistry.FACADES[key]
    return facade.convertNBT2Instance(nbt)
}

/**
 * 从配置文件构建一个 [CoreAttribute].
 *
 * ## 对于 CoreAttributeS
 * 给定的 [node] 必须是以下结构:
 * ```yaml
 * key: <key>
 * operation: <operation>
 * value: <double>
 * ```
 *
 * ## 对于 CoreAttributeSE
 * 给定的 [node] 必须是以下结构:
 * ```yaml
 * key: <key>
 * operation: <operation>
 * value: <double>
 * element: <element>
 * ```
 *
 * ## 对于 CoreAttributeR
 * 给定的 [node] 必须是以下结构:
 * ```yaml
 * key: <key>
 * operation: <operation>
 * lower: <double>
 * upper: <double>
 * ```
 *
 * ## 对于 CoreAttributeRE
 * 给定的 [node] 必须是以下结构:
 * ```yaml
 * key: <key>
 * operation: <operation>
 * lower: <double>
 * upper: <double>
 * element: <element>
 * ```
 */
fun CoreAttribute(
    node: ConfigurationNode,
): CoreAttribute {
    val key = node.node("type").krequire<Key>()
    val facade = AttributeRegistry.FACADES[key]
    return facade.convertNode2Instance(node)
}

sealed class CoreAttribute : Core, AttributeComponent.Op, AttributeModifierSource {
    override val isNoop: Boolean = false
    override val isEmpty: Boolean = false
    override val type: CoreType<*> = Type

    /**
     * 检查两个属性核心是否拥有一样的:
     * - 运算模式
     * - 元素类型
     * - 数值结构
     *
     * 该函数不会检查任何数值的相等性.
     */
    abstract override fun isSimilar(other: Core): Boolean

    override fun provideAttributeModifiers(id: Key): Map<Attribute, AttributeModifier> {
        return AttributeRegistry.FACADES[key].createAttributeModifiers(id, this)
    }

    override fun provideTooltipName(): NameLine {
        val tooltipName = AttributeRegistry.FACADES[key].createTooltipName(this)
        return NameLine.simple(tooltipName)
    }

    override fun provideTooltipLore(): LoreLine {
        val tooltipKey = lineKeyFactory.get(this) ?: return LoreLine.noop()
        val tooltipLore = AttributeRegistry.FACADES[key].createTooltipLore(this)
        return LoreLine.simple(tooltipKey, tooltipLore)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key.asString()),
        ExaminableProperty.of("operation", operation),
    )

    override fun toString(): String = toSimpleString()

    internal companion object Type : CoreType<CoreAttribute>, KoinComponent {
        val lineKeyFactory: CoreAttributeTooltipKeyProvider by inject()
    }
}

internal data class CoreAttributeS(
    override val key: Key,
    override val operation: Operation,
    override val value: Double,
) : CoreAttribute(), AttributeComponentGroupS<Double> {
    constructor(
        compound: CompoundTag,
    ) : this(
        compound.getId(),
        compound.getOperation(),
        compound.getNumber(AttributeBinaryKeys.SINGLE_VALUE)
    )

    override fun isSimilar(other: Core): Boolean {
        return other is CoreAttributeS
                && other.key == key
                && other.operation == operation
    }

    override fun serializeAsTag(): Tag = CompoundTag {
        putId(key)
        putNumber(AttributeBinaryKeys.SINGLE_VALUE, value)
        putOperation(operation)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.concat(
        super.examinableProperties(),
        Stream.of(
            ExaminableProperty.of("value", value),
        )
    )
}

internal data class CoreAttributeR(
    override val key: Key,
    override val operation: Operation,
    override val lower: Double,
    override val upper: Double,
) : CoreAttribute(), AttributeComponentGroupR<Double> {
    constructor(
        compound: CompoundTag,
    ) : this(
        compound.getId(),
        compound.getOperation(),
        compound.getNumber(AttributeBinaryKeys.RANGED_MIN_VALUE),
        compound.getNumber(AttributeBinaryKeys.RANGED_MAX_VALUE)
    )

    override fun isSimilar(other: Core): Boolean {
        return other is CoreAttributeR
                && other.key == key
                && other.operation == operation
    }

    override fun serializeAsTag(): Tag = CompoundTag {
        putId(key)
        putNumber(AttributeBinaryKeys.RANGED_MIN_VALUE, lower)
        putNumber(AttributeBinaryKeys.RANGED_MAX_VALUE, upper)
        putOperation(operation)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.concat(
        super.examinableProperties(),
        Stream.of(
            ExaminableProperty.of("lower", lower),
            ExaminableProperty.of("upper", upper),
        )
    )
}

internal data class CoreAttributeSE(
    override val key: Key,
    override val operation: Operation,
    override val value: Double,
    override val element: Element,
) : CoreAttribute(), AttributeComponentGroupSE<Double> {
    constructor(
        compound: CompoundTag,
    ) : this(
        compound.getId(),
        compound.getOperation(),
        compound.getNumber(AttributeBinaryKeys.SINGLE_VALUE),
        compound.getElement()
    )

    override fun isSimilar(other: Core): Boolean {
        return other is CoreAttributeSE
                && other.key == key
                && other.operation == operation
                && other.element == element
    }

    override fun serializeAsTag(): Tag = CompoundTag {
        putId(key)
        putNumber(AttributeBinaryKeys.SINGLE_VALUE, value)
        putElement(element)
        putOperation(operation)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.concat(
        super.examinableProperties(),
        Stream.of(
            ExaminableProperty.of("value", value),
            ExaminableProperty.of("element", element.uniqueId),
        )
    )
}

internal data class CoreAttributeRE(
    override val key: Key,
    override val operation: Operation,
    override val lower: Double,
    override val upper: Double,
    override val element: Element,
) : CoreAttribute(), AttributeComponentGroupRE<Double> {
    constructor(
        compound: CompoundTag,
    ) : this(
        compound.getId(),
        compound.getOperation(),
        compound.getNumber(AttributeBinaryKeys.RANGED_MIN_VALUE),
        compound.getNumber(AttributeBinaryKeys.RANGED_MAX_VALUE),
        compound.getElement()
    )

    override fun isSimilar(other: Core): Boolean {
        return other is CoreAttributeRE
                && other.key == key
                && other.operation == operation
                && other.element == element
    }

    override fun serializeAsTag(): Tag = CompoundTag {
        putId(key)
        putNumber(AttributeBinaryKeys.RANGED_MIN_VALUE, lower)
        putNumber(AttributeBinaryKeys.RANGED_MAX_VALUE, upper)
        putElement(element)
        putOperation(operation)
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

//<editor-fold desc="Convenient extension functions">
private fun CompoundTag.getId(): Key {
    return Key(this.getString(CoreBinaryKeys.CORE_IDENTIFIER))
}

private fun CompoundTag.getElement(): Element {
    return this.getByteOrNull(AttributeBinaryKeys.ELEMENT_TYPE)?.let { ElementRegistry.getBy(it) } ?: ElementRegistry.DEFAULT
}

private fun CompoundTag.getOperation(): Operation {
    return Operation.byIdOrThrow(this.getInt(AttributeBinaryKeys.OPERATION_TYPE))
}

private fun CompoundTag.getNumber(key: String): Double {
    return this.getDouble(key)
}

private fun CompoundTag.putId(id: Key) {
    this.putString(CoreBinaryKeys.CORE_IDENTIFIER, id.asString())
}

private fun CompoundTag.putNumber(key: String, value: Double) {
    this.putDouble(key, value)
}

private fun CompoundTag.putElement(element: Element) {
    this.putByte(AttributeBinaryKeys.ELEMENT_TYPE, element.binaryId)
}

private fun CompoundTag.putOperation(operation: Operation) {
    this.putByte(AttributeBinaryKeys.OPERATION_TYPE, operation.binary)
}
