package cc.mewcraft.wakame.item.binary.cell.core.attribute

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeBinaryKeys
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.CoreBinaryKeys
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.*
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import java.lang.invoke.MethodHandle
import java.util.EnumMap
import java.util.UUID

//
// 数据类，本身储存数据
//

internal data class BinaryAttributeCoreDataHolderS(
    override val key: Key,
    private val tagType: ShadowTagType,
    override val operation: Operation,
    override val value: Double,
) : BinaryAttributeCoreS() {
    override fun asTag(): ShadowTag = CompoundShadowTag {
        putId(key)
        putNumber(AttributeBinaryKeys.SINGLE_VALUE, value, tagType)
        putOperation(operation)
    }

    override fun provideAttributeModifiers(uuid: UUID): Map<Attribute, AttributeModifier> {
        return AttributeRegistry.FACADES[key].MODIFIER_FACTORY.makeAttributeModifiers(uuid, this)
    }

    override fun provideTagResolverForPlay(): TagResolver {
        throw UnsupportedOperationException()
    }

    override fun provideTagResolverForShow(): TagResolver {
        throw UnsupportedOperationException()
    }
}

internal data class BinaryAttributeCoreDataHolderR(
    override val key: Key,
    private val tagType: ShadowTagType,
    override val operation: Operation,
    override val lower: Double,
    override val upper: Double,
) : BinaryAttributeCoreR() {
    override fun asTag(): ShadowTag = CompoundShadowTag {
        putId(key)
        putNumber(AttributeBinaryKeys.RANGED_MIN_VALUE, lower, tagType)
        putNumber(AttributeBinaryKeys.RANGED_MAX_VALUE, upper, tagType)
        putOperation(operation)
    }

    override fun provideAttributeModifiers(uuid: UUID): Map<Attribute, AttributeModifier> {
        return AttributeRegistry.FACADES[key].MODIFIER_FACTORY.makeAttributeModifiers(uuid, this)
    }

    override fun provideTagResolverForPlay(): TagResolver {
        throw UnsupportedOperationException()
    }

    override fun provideTagResolverForShow(): TagResolver {
        throw UnsupportedOperationException()
    }
}

internal data class BinaryAttributeCoreDataHolderSE(
    override val key: Key,
    private val tagType: ShadowTagType,
    override val operation: Operation,
    override val value: Double,
    override val element: Element,
) : BinaryAttributeCoreSE() {
    override fun asTag(): ShadowTag = CompoundShadowTag {
        putId(key)
        putNumber(AttributeBinaryKeys.SINGLE_VALUE, value, tagType)
        putElement(element)
        putOperation(operation)
    }

    override fun provideAttributeModifiers(uuid: UUID): Map<Attribute, AttributeModifier> {
        return AttributeRegistry.FACADES[key].MODIFIER_FACTORY.makeAttributeModifiers(uuid, this)
    }

    override fun provideTagResolverForPlay(): TagResolver {
        throw UnsupportedOperationException()
    }

    override fun provideTagResolverForShow(): TagResolver {
        throw UnsupportedOperationException()
    }
}

internal data class BinaryAttributeCoreDataHolderRE(
    override val key: Key,
    private val tagType: ShadowTagType,
    override val operation: Operation,
    override val lower: Double,
    override val upper: Double,
    override val element: Element,
) : BinaryAttributeCoreRE() {
    override fun asTag(): ShadowTag = CompoundShadowTag {
        putId(key)
        putNumber(AttributeBinaryKeys.RANGED_MIN_VALUE, lower, tagType)
        putNumber(AttributeBinaryKeys.RANGED_MAX_VALUE, upper, tagType)
        putElement(element)
        putOperation(operation)
    }

    override fun provideAttributeModifiers(uuid: UUID): Map<Attribute, AttributeModifier> {
        return AttributeRegistry.FACADES[key].MODIFIER_FACTORY.makeAttributeModifiers(uuid, this)
    }

    override fun provideTagResolverForPlay(): TagResolver {
        throw UnsupportedOperationException()
    }

    override fun provideTagResolverForShow(): TagResolver {
        throw UnsupportedOperationException()
    }
}

//<editor-fold desc="Convenient extension functions">
private fun CompoundShadowTag.putId(id: Key) {
    this.putString(CoreBinaryKeys.CORE_IDENTIFIER, id.asString())
}

private fun CompoundShadowTag.putNumber(key: String, value: Double, shadowTagType: ShadowTagType) {
    val converted = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getValue(shadowTagType).invoke(value)
    TAG_TYPE_2_TAG_SETTER_MAP.getValue(shadowTagType).invoke(this, key, converted)
}

private fun CompoundShadowTag.putElement(element: Element) {
    this.putByte(AttributeBinaryKeys.ELEMENT_TYPE, element.binaryId)
}

private fun CompoundShadowTag.putOperation(operation: Operation) {
    this.putByte(AttributeBinaryKeys.OPERATION_TYPE, operation.binary)
}

// The MethodHandle's signature: CompoundShadowTag.(String, Number) -> Unit
private val TAG_TYPE_2_TAG_SETTER_MAP: Map<ShadowTagType, MethodHandle> =
    buildMap {
        this[ShadowTagType.BYTE] = CompoundShadowTag::putByte
        this[ShadowTagType.SHORT] = CompoundShadowTag::putShort
        this[ShadowTagType.INT] = CompoundShadowTag::putInt
        this[ShadowTagType.LONG] = CompoundShadowTag::putLong
        this[ShadowTagType.FLOAT] = CompoundShadowTag::putFloat
        this[ShadowTagType.DOUBLE] = CompoundShadowTag::putDouble
    }.mapValues {
        it.value.toMethodHandle()
    }.let(::EnumMap)

// The MethodHandle's signature: (Number) -> Number
private val TAG_TYPE_2_NUMBER_CONVERTER_MAP: Map<ShadowTagType, MethodHandle> =
    buildMap {
        this[ShadowTagType.BYTE] = Number::toStableByte
        this[ShadowTagType.SHORT] = Number::toStableShort
        this[ShadowTagType.INT] = Number::toStableInt
        this[ShadowTagType.LONG] = Number::toStableLong
        this[ShadowTagType.FLOAT] = Number::toStableFloat
        this[ShadowTagType.DOUBLE] = Number::toStableDouble
    }.mapValues {
        it.value.toMethodHandle()
    }.let(::EnumMap)
//</editor-fold>
