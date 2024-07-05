package cc.mewcraft.wakame.item.binary.cell.core.attribute

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.nbt.TagType
import cc.mewcraft.wakame.attribute.AttributeBinaryKeys
import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.CoreBinaryKeys
import cc.mewcraft.wakame.util.*
import net.kyori.adventure.key.Key
import java.lang.invoke.MethodHandle
import java.util.EnumMap

//
// 数据类，本身储存数据
//

internal data class BinaryAttributeCoreDataHolderS(
    override val key: Key,
    private val tagType: TagType,
    override val operation: Operation,
    override val value: Double,
) : BinaryAttributeCoreS() {
    override fun serializeAsTag(): Tag = CompoundTag {
        putId(key)
        putNumber(AttributeBinaryKeys.SINGLE_VALUE, value, tagType)
        putOperation(operation)
    }
}

internal data class BinaryAttributeCoreDataHolderR(
    override val key: Key,
    private val tagType: TagType,
    override val operation: Operation,
    override val lower: Double,
    override val upper: Double,
) : BinaryAttributeCoreR() {
    override fun serializeAsTag(): Tag = CompoundTag {
        putId(key)
        putNumber(AttributeBinaryKeys.RANGED_MIN_VALUE, lower, tagType)
        putNumber(AttributeBinaryKeys.RANGED_MAX_VALUE, upper, tagType)
        putOperation(operation)
    }
}

internal data class BinaryAttributeCoreDataHolderSE(
    override val key: Key,
    private val tagType: TagType,
    override val operation: Operation,
    override val value: Double,
    override val element: Element,
) : BinaryAttributeCoreSE() {
    override fun serializeAsTag(): Tag = CompoundTag {
        putId(key)
        putNumber(AttributeBinaryKeys.SINGLE_VALUE, value, tagType)
        putElement(element)
        putOperation(operation)
    }
}

internal data class BinaryAttributeCoreDataHolderRE(
    override val key: Key,
    private val tagType: TagType,
    override val operation: Operation,
    override val lower: Double,
    override val upper: Double,
    override val element: Element,
) : BinaryAttributeCoreRE() {
    override fun serializeAsTag(): Tag = CompoundTag {
        putId(key)
        putNumber(AttributeBinaryKeys.RANGED_MIN_VALUE, lower, tagType)
        putNumber(AttributeBinaryKeys.RANGED_MAX_VALUE, upper, tagType)
        putElement(element)
        putOperation(operation)
    }
}

//<editor-fold desc="Convenient extension functions">
private fun CompoundTag.putId(id: Key) {
    this.putString(CoreBinaryKeys.CORE_IDENTIFIER, id.asString())
}

private fun CompoundTag.putNumber(key: String, value: Double, TagType: TagType) {
    val converted = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getValue(TagType).invoke(value)
    TAG_TYPE_2_TAG_SETTER_MAP.getValue(TagType).invoke(this, key, converted)
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
