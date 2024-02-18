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

private inline fun <reified T : BinaryAttributeValue> constructBinaryValue(vararg args: Any): BinaryAttributeValue {
    val constructor = requireNotNull(T::class.primaryConstructor) { "Class does not have a primary constructor" }
    return constructor.call(*args)
}

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

        // register scheme baker
        AttributeRegistry.schemeBakerRegistry[key] = SchemeBaker { scheme, factor ->
            scheme as SchemeAttributeValueS
            val value = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getOrThrow(shadowTagType).call(scheme.value.calculate(factor))
            val operation = scheme.operation
            constructBinaryValue<BinaryAttributeValueS<Number>>(value, operation)
        }

        // register shadow tag encoder
        AttributeRegistry.shadowTagEncoder[key] = ShadowTagEncoder { binaryValue ->
            compoundShadowTag {
                binaryValue as BinaryAttributeValueS<*>
                putString(NekoTags.Cell.CORE_ID, key.asString())
                TAG_TYPE_2_TAG_SETTER_MAP.getOrThrow(shadowTagType).call(this, NekoTags.Attribute.VAL, binaryValue.value)
                putOperation(NekoTags.Attribute.OPERATION, binaryValue.operation)
            }
        }

        // register shadow tag decoder
        AttributeRegistry.shadowTagDecoder[key] = ShadowTagDecoder { shadowTag ->
            shadowTag as CompoundShadowTag
            val value = TAG_TYPE_2_TAG_GETTER_MAP.getOrThrow(shadowTagType).call(shadowTag, NekoTags.Attribute.VAL)
            val operation = shadowTag.getOperation(NekoTags.Attribute.OPERATION)
            constructBinaryValue<BinaryAttributeValueS<Number>>(value, operation)
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

        // register scheme baker
        AttributeRegistry.schemeBakerRegistry[key] = SchemeBaker { scheme, factor ->
            scheme as SchemeAttributeValueLU
            val lower = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getOrThrow(shadowTagType).call(scheme.lower.calculate(factor))
            val upper = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getOrThrow(shadowTagType).call(scheme.upper.calculate(factor))
            val operation = scheme.operation
            constructBinaryValue<BinaryAttributeValueLU<Number>>(lower, upper, operation)
        }

        // register shadow tag encoder
        AttributeRegistry.shadowTagEncoder[key] = ShadowTagEncoder { binaryValue ->
            compoundShadowTag {
                binaryValue as BinaryAttributeValueLU<*>
                putString(NekoTags.Cell.CORE_ID, key.asString())
                TAG_TYPE_2_TAG_SETTER_MAP.getOrThrow(shadowTagType).call(this, NekoTags.Attribute.MIN, binaryValue.lower)
                TAG_TYPE_2_TAG_SETTER_MAP.getOrThrow(shadowTagType).call(this, NekoTags.Attribute.MAX, binaryValue.upper)
                putOperation(NekoTags.Attribute.OPERATION, binaryValue.operation)
            }
        }

        // register shadow tag decoder
        AttributeRegistry.shadowTagDecoder[key] = ShadowTagDecoder { shadowTag ->
            shadowTag as CompoundShadowTag
            val lower = TAG_TYPE_2_TAG_GETTER_MAP.getOrThrow(shadowTagType).call(shadowTag, NekoTags.Attribute.MIN)
            val upper = TAG_TYPE_2_TAG_GETTER_MAP.getOrThrow(shadowTagType).call(shadowTag, NekoTags.Attribute.MAX)
            val operation = shadowTag.getOperation(NekoTags.Attribute.OPERATION)
            constructBinaryValue<BinaryAttributeValueLU<Number>>(lower, upper, operation)
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

        // register scheme baker
        AttributeRegistry.schemeBakerRegistry[key] = SchemeBaker { scheme, factor ->
            scheme as SchemeAttributeValueSE
            val value = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getOrThrow(shadowTagType).call(scheme.value.calculate(factor))
            val element = scheme.element
            val operation = scheme.operation
            constructBinaryValue<BinaryAttributeValueSE<Number>>(value, element, operation)
        }

        // register shadow tag encoder
        AttributeRegistry.shadowTagEncoder[key] = ShadowTagEncoder { binaryValue ->
            binaryValue as BinaryAttributeValueSE<*>
            compoundShadowTag {
                putString(NekoTags.Cell.CORE_ID, key.asString())
                TAG_TYPE_2_TAG_SETTER_MAP.getOrThrow(shadowTagType).call(this, NekoTags.Attribute.VAL, binaryValue.value)
                putElement(NekoTags.Attribute.ELEMENT, binaryValue.element)
                putOperation(NekoTags.Attribute.OPERATION, binaryValue.operation)
            }
        }

        // register shadow tag decoder
        AttributeRegistry.shadowTagDecoder[key] = ShadowTagDecoder { shadowTag ->
            shadowTag as CompoundShadowTag
            val value = TAG_TYPE_2_TAG_GETTER_MAP.getOrThrow(shadowTagType).call(shadowTag, NekoTags.Attribute.VAL)
            val element = shadowTag.getElement(NekoTags.Attribute.ELEMENT)
            val operation = shadowTag.getOperation(NekoTags.Attribute.OPERATION)
            constructBinaryValue<BinaryAttributeValueSE<Number>>(value, element, operation)
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

        // register scheme baker
        AttributeRegistry.schemeBakerRegistry[key] = SchemeBaker { scheme, factor ->
            scheme as SchemeAttributeValueLUE
            val lower = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getOrThrow(shadowTagType).call(scheme.lower.calculate(factor))
            val upper = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getOrThrow(shadowTagType).call(scheme.upper.calculate(factor))
            val element = scheme.element
            val operation = scheme.operation
            constructBinaryValue<BinaryAttributeValueLUE<Number>>(lower, upper, element, operation)
        }

        // register shadow tag encoder
        AttributeRegistry.shadowTagEncoder[key] = ShadowTagEncoder { binaryValue ->
            binaryValue as BinaryAttributeValueLUE<*>
            compoundShadowTag {
                putString(NekoTags.Cell.CORE_ID, key.asString())
                TAG_TYPE_2_TAG_SETTER_MAP.getOrThrow(shadowTagType).call(this, NekoTags.Attribute.MIN, binaryValue.lower)
                TAG_TYPE_2_TAG_SETTER_MAP.getOrThrow(shadowTagType).call(this, NekoTags.Attribute.MAX, binaryValue.upper)
                putElement(NekoTags.Attribute.ELEMENT, binaryValue.element)
                putOperation(NekoTags.Attribute.OPERATION, binaryValue.operation)
            }
        }

        // register shadow tag decoder
        AttributeRegistry.shadowTagDecoder[key] = ShadowTagDecoder { shadowTag ->
            shadowTag as CompoundShadowTag
            val lower = TAG_TYPE_2_TAG_GETTER_MAP.getOrThrow(shadowTagType).call(shadowTag, NekoTags.Attribute.MIN)
            val upper = TAG_TYPE_2_TAG_GETTER_MAP.getOrThrow(shadowTagType).call(shadowTag, NekoTags.Attribute.MAX)
            val element = shadowTag.getElement(NekoTags.Attribute.ELEMENT)
            val operation = shadowTag.getOperation(NekoTags.Attribute.OPERATION)
            constructBinaryValue<BinaryAttributeValueLUE<Number>>(lower, upper, element, operation)
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