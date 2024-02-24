package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.attribute.base.Attribute
import cc.mewcraft.wakame.attribute.base.AttributeModifier
import cc.mewcraft.wakame.attribute.base.ElementAttribute
import cc.mewcraft.wakame.attribute.facade.*
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.*
import cc.mewcraft.wakame.util.*
import com.google.common.collect.ImmutableMap
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import java.util.EnumMap
import kotlin.reflect.KFunction

// TODO use MethodHandle for better reflection performance

// Map Value's KFunction Type: Map<ShadowTagType, CompoundShadowTag.(String, Number) -> Unit>
private val TAG_TYPE_2_TAG_SETTER_MAP: Map<ShadowTagType, KFunction<Unit>> = @Suppress("DuplicatedCode") buildMap {
    this[ShadowTagType.BYTE] = CompoundShadowTag::putByte
    this[ShadowTagType.SHORT] = CompoundShadowTag::putShort
    this[ShadowTagType.INT] = CompoundShadowTag::putInt
    this[ShadowTagType.LONG] = CompoundShadowTag::putLong
    this[ShadowTagType.FLOAT] = CompoundShadowTag::putFloat
    this[ShadowTagType.DOUBLE] = CompoundShadowTag::putDouble
}.let {
    EnumMap(it)
}

// Map Value's KFunction Type: CompoundShadowTag.(String) -> Number
private val TAG_TYPE_2_TAG_GETTER_MAP: Map<ShadowTagType, KFunction<Number>> = @Suppress("DuplicatedCode") buildMap {
    this[ShadowTagType.BYTE] = CompoundShadowTag::getByte
    this[ShadowTagType.SHORT] = CompoundShadowTag::getShort
    this[ShadowTagType.INT] = CompoundShadowTag::getInt
    this[ShadowTagType.LONG] = CompoundShadowTag::getLong
    this[ShadowTagType.FLOAT] = CompoundShadowTag::getFloat
    this[ShadowTagType.DOUBLE] = CompoundShadowTag::getDouble
}.let {
    EnumMap(it)
}

// Map Value's KFunction Type: (Number) -> Number
private val TAG_TYPE_2_NUMBER_CONVERTER_MAP: Map<ShadowTagType, KFunction<Number>> = buildMap {
    this[ShadowTagType.BYTE] = Number::toStableByte
    this[ShadowTagType.SHORT] = Number::toStableShort
    this[ShadowTagType.INT] = Number::toStableInt
    this[ShadowTagType.LONG] = Number::toStableLong
    this[ShadowTagType.FLOAT] = Number::toStableFloat
    this[ShadowTagType.DOUBLE] = Number::toStableDouble
}.let {
    EnumMap(it)
}

//<editor-fold desc="Specialized Compound Operations">
private fun CompoundShadowTag.getElement(): Element {
    val byte = this.getByteOrNull(NekoTags.Attribute.ELEMENT) ?: return ElementRegistry.DEFAULT
    return ElementRegistry.getByOrThrow(byte)
}

private fun CompoundShadowTag.putElement(element: Element) {
    this.putByte(NekoTags.Attribute.ELEMENT, element.binary)
}

private fun CompoundShadowTag.getOperation(): AttributeModifier.Operation {
    return AttributeModifier.Operation.byId(this.getInt(NekoTags.Attribute.OPERATION))
}

private fun CompoundShadowTag.putOperation(operation: AttributeModifier.Operation) {
    this.putByte(NekoTags.Attribute.OPERATION, operation.binary)
}

private fun CompoundShadowTag.getNumber(key: String): Double {
    return this.getDouble(key)
}

private fun CompoundShadowTag.putNumber(key: String, value: Number, shadowTagType: ShadowTagType) {
    val converted = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getOrThrow(shadowTagType).call(value)
    TAG_TYPE_2_TAG_SETTER_MAP.getOrThrow(shadowTagType).call(this, key, converted)
}

private fun CompoundShadowTag.putId(id: Key) {
    this.putString(NekoTags.Cell.CORE_ID, id.asString())
}
//</editor-fold>

@InternalApi
internal class FormatSelectionImpl(
    private val key: Key,
    private val shadowTagType: ShadowTagType,
) : FormatSelection {
    override fun single(): SingleSelectionImpl {
        return SingleSelectionImpl(key, shadowTagType)
    }

    override fun ranged(): RangedSelectionImpl {
        return RangedSelectionImpl(key, shadowTagType)
    }
}

@InternalApi
internal class SingleSelectionImpl(
    private val key: Key,
    private val shadowTagType: ShadowTagType,
) : SingleSelection {
    override fun element(): SingleElementAttributeBinderImpl {
        return SingleElementAttributeBinderImpl(key, shadowTagType)
    }

    /**
     * Format: S
     */
    override fun bind(component: Attribute) {
        // register scheme builder
        AttributeRegistry.schemeBuilderRegistry[key] = SchemeBuilder(SchemeAttributeValueSerializerS::deserialize)

        // register shadow tag encoder
        AttributeRegistry.shadowTagEncoder[key] = ShadowTagEncoder { binaryValue ->
            val compound = CompoundShadowTag.create()
            binaryValue as BinaryAttributeValueS
            compound.putId(key)
            compound.putNumber(NekoTags.Attribute.VAL, binaryValue.value, shadowTagType)
            compound.putOperation(binaryValue.operation)
            compound
        }

        // register shadow tag decoder
        AttributeRegistry.shadowTagDecoder[key] = ShadowTagDecoder { shadowTag ->
            val value = shadowTag.getNumber(NekoTags.Attribute.VAL)
            val operation = shadowTag.getOperation()
            BinaryAttributeValueS(value, operation)
        }

        // register attribute factory
        AttributeRegistry.attributeFactoryRegistry[key] = AttributeModifierFactory { uuid, value ->
            value as BinaryAttributeValueS
            val modifier = AttributeModifier(uuid, value.value.toStableDouble(), value.operation)
            ImmutableMap.of(component, modifier)
        }

        // register attribute struct meta
        AttributeRegistry.attributeStructRegistry[key] = AttributeStructMeta(AttributeStructMeta.Format.SINGLE, false)
    }
}

@InternalApi
internal class RangedSelectionImpl(
    private val key: Key,
    private val shadowTagType: ShadowTagType,
) : RangedSelection {
    override fun element(): RangedElementAttributeBinderImpl {
        return RangedElementAttributeBinderImpl(key, shadowTagType)
    }

    /**
     * Format: LU
     */
    override fun bind(
        component1: Attribute,
        component2: Attribute,
    ) {
        // register scheme builder
        AttributeRegistry.schemeBuilderRegistry[key] = SchemeBuilder(SchemeAttributeValueSerializerLU::deserialize)

        // register shadow tag encoder
        AttributeRegistry.shadowTagEncoder[key] = ShadowTagEncoder { binaryValue ->
            val compound = CompoundShadowTag.create()
            binaryValue as BinaryAttributeValueLU
            compound.putId(key)
            compound.putNumber(NekoTags.Attribute.MIN, binaryValue.lower, shadowTagType)
            compound.putNumber(NekoTags.Attribute.MAX, binaryValue.upper, shadowTagType)
            compound.putOperation(binaryValue.operation)
            compound
        }

        // register shadow tag decoder
        AttributeRegistry.shadowTagDecoder[key] = ShadowTagDecoder { shadowTag ->
            val lower = shadowTag.getNumber(NekoTags.Attribute.MIN)
            val upper = shadowTag.getNumber(NekoTags.Attribute.MAX)
            val operation = shadowTag.getOperation()
            BinaryAttributeValueLU(lower, upper, operation)
        }

        // register attribute factory
        AttributeRegistry.attributeFactoryRegistry[key] = AttributeModifierFactory { uuid, value ->
            value as BinaryAttributeValueLU
            val modifier1 = AttributeModifier(uuid, value.lower.toStableDouble(), value.operation)
            val modifier2 = AttributeModifier(uuid, value.upper.toStableDouble(), value.operation)
            ImmutableMap.of(component1, modifier1, component2, modifier2)
        }

        // register attribute struct meta
        AttributeRegistry.attributeStructRegistry[key] = AttributeStructMeta(AttributeStructMeta.Format.RANGED, false)
    }
}

@InternalApi
internal class SingleElementAttributeBinderImpl(
    private val key: Key,
    private val shadowTagType: ShadowTagType,
) : SingleElementAttributeBinder {

    /**
     * Format: SE
     */
    override fun bind(
        component: (Element) -> ElementAttribute,
    ) {
        // register scheme builder
        AttributeRegistry.schemeBuilderRegistry[key] = SchemeBuilder(SchemeAttributeValueSerializerSE::deserialize)

        // register shadow tag encoder
        AttributeRegistry.shadowTagEncoder[key] = ShadowTagEncoder { binaryValue ->
            val compound = CompoundShadowTag.create()
            binaryValue as BinaryAttributeValueSE
            compound.putId(key)
            compound.putNumber(NekoTags.Attribute.VAL, binaryValue.value, shadowTagType)
            compound.putElement(binaryValue.element)
            compound.putOperation(binaryValue.operation)
            compound
        }

        // register shadow tag decoder
        AttributeRegistry.shadowTagDecoder[key] = ShadowTagDecoder { shadowTag ->
            val value = shadowTag.getNumber(NekoTags.Attribute.VAL)
            val element = shadowTag.getElement()
            val operation = shadowTag.getOperation()
            BinaryAttributeValueSE(value, element, operation)
        }

        // register attribute factory
        AttributeRegistry.attributeFactoryRegistry[key] = AttributeModifierFactory { uuid, value ->
            value as BinaryAttributeValueSE
            val modifier = AttributeModifier(uuid, value.value.toStableDouble(), value.operation)
            ImmutableMap.of(component(value.element), modifier)
        }

        // register attribute struct meta
        AttributeRegistry.attributeStructRegistry[key] = AttributeStructMeta(AttributeStructMeta.Format.SINGLE, true)
    }
}

@InternalApi
internal class RangedElementAttributeBinderImpl(
    private val key: Key,
    private val shadowTagType: ShadowTagType,
) : RangedElementAttributeBinder {

    /**
     * Format: LUE
     */
    override fun bind(
        component1: (Element) -> ElementAttribute,
        component2: (Element) -> ElementAttribute,
    ) {
        // register scheme builder
        AttributeRegistry.schemeBuilderRegistry[key] = SchemeBuilder(SchemeAttributeValueSerializerLUE::deserialize)

        // register shadow tag encoder
        AttributeRegistry.shadowTagEncoder[key] = ShadowTagEncoder { binaryValue ->
            val compound = CompoundShadowTag.create()
            binaryValue as BinaryAttributeValueLUE
            compound.putId(key)
            compound.putNumber(NekoTags.Attribute.MIN, binaryValue.lower, shadowTagType)
            compound.putNumber(NekoTags.Attribute.MAX, binaryValue.upper, shadowTagType)
            compound.putElement(binaryValue.element)
            compound.putOperation(binaryValue.operation)
            compound
        }

        // register shadow tag decoder
        AttributeRegistry.shadowTagDecoder[key] = ShadowTagDecoder { shadowTag ->
            val lower = shadowTag.getNumber(NekoTags.Attribute.MIN)
            val upper = shadowTag.getNumber(NekoTags.Attribute.MAX)
            val element = shadowTag.getElement()
            val operation = shadowTag.getOperation()
            BinaryAttributeValueLUE(lower, upper, element, operation)
        }

        // register attribute factory
        AttributeRegistry.attributeFactoryRegistry[key] = AttributeModifierFactory { uuid, value ->
            value as BinaryAttributeValueLUE
            val modifier1 = AttributeModifier(uuid, value.lower.toStableDouble(), value.operation)
            val modifier2 = AttributeModifier(uuid, value.upper.toStableDouble(), value.operation)
            ImmutableMap.of(component1(value.element), modifier1, component2(value.element), modifier2)
        }

        // register attribute struct meta
        AttributeRegistry.attributeStructRegistry[key] = AttributeStructMeta(AttributeStructMeta.Format.RANGED, true)
    }
}