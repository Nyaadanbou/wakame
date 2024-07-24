package cc.mewcraft.wakame.item.components.cells.cores.attribute

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.nbt.TagType
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeBinaryKeys
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.facade.AttributeComponent
import cc.mewcraft.wakame.attribute.facade.AttributeDataR
import cc.mewcraft.wakame.attribute.facade.AttributeDataRE
import cc.mewcraft.wakame.attribute.facade.AttributeDataS
import cc.mewcraft.wakame.attribute.facade.AttributeDataSE
import cc.mewcraft.wakame.attribute.facade.AttributeModifierProvider
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
import cc.mewcraft.wakame.util.toMethodHandle
import cc.mewcraft.wakame.util.toSimpleString
import cc.mewcraft.wakame.util.toStableByte
import cc.mewcraft.wakame.util.toStableDouble
import cc.mewcraft.wakame.util.toStableFloat
import cc.mewcraft.wakame.util.toStableInt
import cc.mewcraft.wakame.util.toStableLong
import cc.mewcraft.wakame.util.toStableShort
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.spongepowered.configurate.ConfigurationNode
import java.lang.invoke.MethodHandle
import java.util.EnumMap
import java.util.UUID
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
    return facade.codecTagToInstance(nbt)
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
    return facade.codecNodeToInstance(node)
}

sealed class CoreAttribute : Core, AttributeComponent.Op, AttributeModifierProvider {
    override val isNoop: Boolean = false
    override val isEmpty: Boolean = false
    override val type: CoreType<*> = Type

    override fun provideAttributeModifiers(uuid: UUID): Map<Attribute, AttributeModifier> {
        return AttributeRegistry.FACADES[key].modifierCreator(uuid, this)
    }

    override fun provideTooltipName(): NameLine {
        val tooltipName = AttributeRegistry.FACADES[key].tooltipNameCreator(this)
        return NameLine.simple(tooltipName)
    }

    override fun provideTooltipLore(): LoreLine {
        val tooltipKey = lineKeyFactory.get(this) ?: return LoreLine.noop()
        val tooltipLore = AttributeRegistry.FACADES[key].tooltipLoreCreator(this)
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
    private val tagType: TagType,
    override val key: Key,
    override val operation: Operation,
    override val value: Double,
) : CoreAttribute(), AttributeDataS<Double> {
    constructor(
        tagType: TagType,
        compound: CompoundTag,
    ) : this(
        tagType,
        compound.getId(),
        compound.getOperation(),
        compound.getNumber(AttributeBinaryKeys.SINGLE_VALUE)
    )

    override fun serializeAsTag(): Tag = CompoundTag {
        putId(key)
        putNumber(AttributeBinaryKeys.SINGLE_VALUE, value, tagType)
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
    private val tagType: TagType,
    override val key: Key,
    override val operation: Operation,
    override val lower: Double,
    override val upper: Double,
) : CoreAttribute(), AttributeDataR<Double> {
    constructor(
        tagType: TagType,
        compound: CompoundTag,
    ) : this(
        tagType,
        compound.getId(),
        compound.getOperation(),
        compound.getNumber(AttributeBinaryKeys.RANGED_MIN_VALUE),
        compound.getNumber(AttributeBinaryKeys.RANGED_MAX_VALUE)
    )

    override fun serializeAsTag(): Tag = CompoundTag {
        putId(key)
        putNumber(AttributeBinaryKeys.RANGED_MIN_VALUE, lower, tagType)
        putNumber(AttributeBinaryKeys.RANGED_MAX_VALUE, upper, tagType)
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
    private val tagType: TagType,
    override val key: Key,
    override val operation: Operation,
    override val value: Double,
    override val element: Element,
) : CoreAttribute(), AttributeDataSE<Double> {
    constructor(
        tagType: TagType,
        compound: CompoundTag,
    ) : this(
        tagType,
        compound.getId(),
        compound.getOperation(),
        compound.getNumber(AttributeBinaryKeys.SINGLE_VALUE),
        compound.getElement()
    )

    override fun serializeAsTag(): Tag = CompoundTag {
        putId(key)
        putNumber(AttributeBinaryKeys.SINGLE_VALUE, value, tagType)
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
    private val tagType: TagType,
    override val key: Key,
    override val operation: Operation,
    override val lower: Double,
    override val upper: Double,
    override val element: Element,
) : CoreAttribute(), AttributeDataRE<Double> {
    constructor(
        tagType: TagType,
        compound: CompoundTag,
    ) : this(
        tagType,
        compound.getId(),
        compound.getOperation(),
        compound.getNumber(AttributeBinaryKeys.RANGED_MIN_VALUE),
        compound.getNumber(AttributeBinaryKeys.RANGED_MAX_VALUE),
        compound.getElement()
    )

    override fun serializeAsTag(): Tag = CompoundTag {
        putId(key)
        putNumber(AttributeBinaryKeys.RANGED_MIN_VALUE, lower, tagType)
        putNumber(AttributeBinaryKeys.RANGED_MAX_VALUE, upper, tagType)
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
    return Operation.byId(this.getInt(AttributeBinaryKeys.OPERATION_TYPE))
}

private fun CompoundTag.getNumber(key: String): Double {
    return this.getDouble(key)
}

private fun CompoundTag.putId(id: Key) {
    this.putString(CoreBinaryKeys.CORE_IDENTIFIER, id.asString())
}

private fun CompoundTag.putNumber(key: String, value: Double, tagType: TagType) {
    val converted = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getValue(tagType).invoke(value)
    TAG_TYPE_2_TAG_SETTER_MAP.getValue(tagType).invoke(this, key, converted)
}

private fun CompoundTag.putElement(element: Element) {
    this.putByte(AttributeBinaryKeys.ELEMENT_TYPE, element.binaryId)
}

private fun CompoundTag.putOperation(operation: Operation) {
    this.putByte(AttributeBinaryKeys.OPERATION_TYPE, operation.binary)
}

// The MethodHandle's signature: CompoundTag.(String, Number) -> Unit
private val TAG_TYPE_2_TAG_SETTER_MAP: Map<TagType, MethodHandle> =
    buildMap {
        this[TagType.BYTE] = CompoundTag::putByte
        this[TagType.SHORT] = CompoundTag::putShort
        this[TagType.INT] = CompoundTag::putInt
        this[TagType.LONG] = CompoundTag::putLong
        this[TagType.FLOAT] = CompoundTag::putFloat
        this[TagType.DOUBLE] = CompoundTag::putDouble
    }.mapValues {
        it.value.toMethodHandle()
    }.let(::EnumMap)

// The MethodHandle's signature: (Number) -> Number
private val TAG_TYPE_2_NUMBER_CONVERTER_MAP: Map<TagType, MethodHandle> =
    buildMap {
        this[TagType.BYTE] = Number::toStableByte
        this[TagType.SHORT] = Number::toStableShort
        this[TagType.INT] = Number::toStableInt
        this[TagType.LONG] = Number::toStableLong
        this[TagType.FLOAT] = Number::toStableFloat
        this[TagType.DOUBLE] = Number::toStableDouble
    }.mapValues {
        it.value.toMethodHandle()
    }.let(::EnumMap)
//</editor-fold>
