package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.attribute.base.Attribute
import cc.mewcraft.wakame.attribute.base.AttributeModifier
import cc.mewcraft.wakame.attribute.base.ElementAttribute
import cc.mewcraft.wakame.attribute.facade.*
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.SchemeBaker
import cc.mewcraft.wakame.item.SchemeBuilder
import cc.mewcraft.wakame.item.ShadowTagDecoder
import cc.mewcraft.wakame.item.ShadowTagEncoder
import cc.mewcraft.wakame.util.*
import com.google.common.collect.ImmutableMap
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import java.util.EnumMap
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor


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

private inline fun <reified T : BinaryAttributeValue> constructBinaryAttributeValue(vararg args: Any): BinaryAttributeValue {
    val clazz = T::class
    val constructor = requireNotNull(clazz.primaryConstructor) { "Class does not have a primary constructor" }
    return constructor.call(args)
}

internal class FormatSelectionImpl(
    val key: Key,
    val type: ShadowTagType,
) : FormatSelection {
    override fun ranged(): RangedSelectionImpl {
        return RangedSelectionImpl(key, type)
    }

    override fun single(): SingleSelectionImpl {
        return SingleSelectionImpl(key, type)
    }
}

@OptIn(InternalApi::class)
internal class SingleSelectionImpl(
    val key: Key,
    val type: ShadowTagType,
) : SingleSelection {
    override fun element(): SingleElementAttributeBinderImpl {
        return SingleElementAttributeBinderImpl(key, type)
    }

    override fun bind(component: Attribute) {
        // format: S

        // register scheme builder
        AttributeRegistry.schemeBuilderRegistry[key] = SchemeBuilder(SchemeAttributeValueSerializerS::deserialize)

        // register scheme baker
        AttributeRegistry.schemeBakerRegistry[key] = SchemeBaker { scheme, factor ->
            scheme as SchemeAttributeValueS
            val value = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getOrThrow(type).call(scheme.value.calculate(factor))
            val operation = scheme.operation
            constructBinaryAttributeValue<BinaryAttributeValueS<Number>>(value, operation)
        }

        // register shadow tag encoder
        AttributeRegistry.shadowTagEncoder[key] = ShadowTagEncoder { binaryValue ->
            compoundShadowTag {
                binaryValue as BinaryAttributeValueS<*>
                TAG_TYPE_2_TAG_SETTER_MAP.getOrThrow(type).call(this, NekoTags.Attribute.VAL, binaryValue.value)
                putOperation(NekoTags.Attribute.OPERATION, binaryValue.operation)
            }
        }

        // register shadow tag decoder
        AttributeRegistry.shadowTagDecoder[key] = ShadowTagDecoder { shadowTag ->
            shadowTag as CompoundShadowTag
            val value = TAG_TYPE_2_TAG_GETTER_MAP.getOrThrow(type).call(shadowTag, NekoTags.Attribute.VAL)
            val operation = shadowTag.getOperation(NekoTags.Attribute.OPERATION)
            constructBinaryAttributeValue<BinaryAttributeValueS<Number>>(value, operation)
        }

        // register attribute factory
        AttributeRegistry.attributeFactoryRegistry[key] = AttributeModifierFactory { uuid, value ->
            value as BinaryAttributeValueS<*>
            ImmutableMap.of(
                component,
                AttributeModifier(uuid, value.value.toStableDouble(), value.operation)
            )
        }
    }
}

@OptIn(InternalApi::class)
internal class RangedSelectionImpl(
    val key: Key,
    val type: ShadowTagType,
) : RangedSelection {
    override fun element(): RangedElementAttributeBinderImpl {
        return RangedElementAttributeBinderImpl(key, type)
    }

    override fun bind(
        component1: Attribute,
        component2: Attribute,
    ) {
        // format: LU

        // register scheme builder
        AttributeRegistry.schemeBuilderRegistry[key] = SchemeBuilder(SchemeAttributeValueSerializerLU::deserialize)

        // register scheme baker
        AttributeRegistry.schemeBakerRegistry[key] = SchemeBaker { scheme, factor ->
            scheme as SchemeAttributeValueLU
            val lower = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getOrThrow(type).call(scheme.lower.calculate(factor))
            val upper = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getOrThrow(type).call(scheme.upper.calculate(factor))
            val operation = scheme.operation
            constructBinaryAttributeValue<BinaryAttributeValueLU<Number>>(lower, upper, operation)
        }

        // register shadow tag encoder
        AttributeRegistry.shadowTagEncoder[key] = ShadowTagEncoder { binaryValue ->
            compoundShadowTag {
                binaryValue as BinaryAttributeValueLU<*>
                TAG_TYPE_2_TAG_SETTER_MAP.getOrThrow(type).call(this, NekoTags.Attribute.MIN, binaryValue.lower)
                TAG_TYPE_2_TAG_SETTER_MAP.getOrThrow(type).call(this, NekoTags.Attribute.MAX, binaryValue.upper)
                putOperation(NekoTags.Attribute.OPERATION, binaryValue.operation)
            }
        }

        // register shadow tag decoder
        AttributeRegistry.shadowTagDecoder[key] = ShadowTagDecoder { shadowTag ->
            shadowTag as CompoundShadowTag
            val lower = TAG_TYPE_2_TAG_GETTER_MAP.getOrThrow(type).call(shadowTag, NekoTags.Attribute.MIN)
            val upper = TAG_TYPE_2_TAG_GETTER_MAP.getOrThrow(type).call(shadowTag, NekoTags.Attribute.MAX)
            val operation = shadowTag.getOperation(NekoTags.Attribute.OPERATION)
            constructBinaryAttributeValue<BinaryAttributeValueLU<Number>>(lower, upper, operation)
        }

        // register attribute factory
        AttributeRegistry.attributeFactoryRegistry[key] = AttributeModifierFactory { uuid, value ->
            value as BinaryAttributeValueLU<*>
            ImmutableMap.of(
                component1,
                AttributeModifier(uuid, value.lower.toStableDouble(), value.operation),
                component2,
                AttributeModifier(uuid, value.upper.toStableDouble(), value.operation),
            )
        }
    }
}

@OptIn(InternalApi::class)
internal class SingleElementAttributeBinderImpl(
    val key: Key,
    val type: ShadowTagType,
) : SingleElementAttributeBinder {
    override fun bind(
        component: (Element) -> ElementAttribute,
    ) {
        // format: SE

        // register scheme builder
        AttributeRegistry.schemeBuilderRegistry[key] = SchemeBuilder(SchemeAttributeValueSerializerSE::deserialize)

        // register scheme baker
        AttributeRegistry.schemeBakerRegistry[key] = SchemeBaker { scheme, factor ->
            scheme as SchemeAttributeValueSE
            val value = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getOrThrow(type).call(scheme.value.calculate(factor))
            val element = scheme.element
            val operation = scheme.operation
            constructBinaryAttributeValue<BinaryAttributeValueSE<Number>>(value, element, operation)
        }

        // register shadow tag encoder
        AttributeRegistry.shadowTagEncoder[key] = ShadowTagEncoder { binaryValue ->
            compoundShadowTag {
                binaryValue as BinaryAttributeValueSE<*>
                TAG_TYPE_2_TAG_SETTER_MAP.getOrThrow(type).call(this, NekoTags.Attribute.VAL, binaryValue.value)
                putElement(NekoTags.Attribute.ELEMENT, binaryValue.element)
                putOperation(NekoTags.Attribute.OPERATION, binaryValue.operation)
            }
        }

        // register shadow tag decoder
        AttributeRegistry.shadowTagDecoder[key] = ShadowTagDecoder { shadowTag ->
            shadowTag as CompoundShadowTag
            val value = TAG_TYPE_2_TAG_GETTER_MAP.getOrThrow(type).call(shadowTag, NekoTags.Attribute.VAL)
            val element = shadowTag.getElement(NekoTags.Attribute.ELEMENT)
            val operation = shadowTag.getOperation(NekoTags.Attribute.OPERATION)
            constructBinaryAttributeValue<BinaryAttributeValueSE<Number>>(value, element, operation)
        }

        // register attribute factory
        AttributeRegistry.attributeFactoryRegistry[key] = AttributeModifierFactory { uuid, value ->
            value as BinaryAttributeValueSE<*>
            ImmutableMap.of(
                component(value.element),
                AttributeModifier(uuid, value.value.toStableDouble(), value.operation)
            )
        }
    }
}

@OptIn(InternalApi::class)
internal class RangedElementAttributeBinderImpl(
    val key: Key,
    val type: ShadowTagType,
) : RangedElementAttributeBinder {
    override fun bind(
        component1: (Element) -> ElementAttribute,
        component2: (Element) -> ElementAttribute,
    ) {
        // format: LUE

        // register scheme builder
        AttributeRegistry.schemeBuilderRegistry[key] = SchemeBuilder(SchemeAttributeValueSerializerLUE::deserialize)

        // register scheme baker
        AttributeRegistry.schemeBakerRegistry[key] = SchemeBaker { scheme, factor ->
            scheme as SchemeAttributeValueLUE
            val lower = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getOrThrow(type).call(scheme.lower.calculate(factor))
            val upper = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getOrThrow(type).call(scheme.upper.calculate(factor))
            val element = scheme.element
            val operation = scheme.operation
            constructBinaryAttributeValue<BinaryAttributeValueLUE<Number>>(lower, upper, element, operation)
        }

        // register shadow tag encoder
        AttributeRegistry.shadowTagEncoder[key] = ShadowTagEncoder { binaryValue ->
            compoundShadowTag {
                binaryValue as BinaryAttributeValueLUE<*>
                TAG_TYPE_2_TAG_SETTER_MAP.getOrThrow(type).call(this, NekoTags.Attribute.MIN, binaryValue.lower)
                TAG_TYPE_2_TAG_SETTER_MAP.getOrThrow(type).call(this, NekoTags.Attribute.MAX, binaryValue.upper)
                putElement(NekoTags.Attribute.ELEMENT, binaryValue.element)
                putOperation(NekoTags.Attribute.OPERATION, binaryValue.operation)
            }
        }

        // register shadow tag decoder
        AttributeRegistry.shadowTagDecoder[key] = ShadowTagDecoder { shadowTag ->
            shadowTag as CompoundShadowTag
            val lower = TAG_TYPE_2_TAG_GETTER_MAP.getOrThrow(type).call(shadowTag, NekoTags.Attribute.MIN)
            val upper = TAG_TYPE_2_TAG_GETTER_MAP.getOrThrow(type).call(shadowTag, NekoTags.Attribute.MAX)
            val element = shadowTag.getElement(NekoTags.Attribute.ELEMENT)
            val operation = shadowTag.getOperation(NekoTags.Attribute.OPERATION)
            constructBinaryAttributeValue<BinaryAttributeValueLUE<Number>>(lower, upper, element, operation)
        }

        // register attribute factory
        AttributeRegistry.attributeFactoryRegistry[key] = AttributeModifierFactory { uuid, value ->
            value as BinaryAttributeValueLUE<*>
            ImmutableMap.of(
                component1(value.element),
                AttributeModifier(uuid, value.lower.toStableDouble(), value.operation),
                component2(value.element),
                AttributeModifier(uuid, value.upper.toStableDouble(), value.operation),
            )
        }
    }
}