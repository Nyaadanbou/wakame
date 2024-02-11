package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.*
import cc.mewcraft.wakame.util.*
import com.google.common.collect.ImmutableMap
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor

object Playground {
    fun callerSideUsage() {
        AttributeFacadeRegistry.build("attack_damage", ShadowTagType.SHORT).ranged().element().bind(
            Attributes.byElement { MIN_ATTACK_DAMAGE },
            Attributes.byElement { MAX_ATTACK_DAMAGE }
        )

        AttributeFacadeRegistry.build("attack_damage_rate", ShadowTagType.DOUBLE).single().element().bind(
            Attributes.byElement { ATTACK_DAMAGE_RATE }
        )

        AttributeFacadeRegistry.build("attack_effect_chance", ShadowTagType.DOUBLE).single().bind(Attributes.ATTACK_EFFECT_CHANCE)

        AttributeFacadeRegistry.build("attack_speed_level", ShadowTagType.BYTE).single().bind(Attributes.ATTACK_SPEED_LEVEL)

        AttributeFacadeRegistry.build("critical_strike_chance", ShadowTagType.DOUBLE).single().bind(Attributes.CRITICAL_STRIKE_CHANCE)

        AttributeFacadeRegistry.build("critical_strike_power", ShadowTagType.DOUBLE).single().bind(Attributes.CRITICAL_STRIKE_POWER)

        AttributeFacadeRegistry.build("damage_taken_rate", ShadowTagType.DOUBLE).single().bind(Attributes.DAMAGE_TAKEN_RATE)

        AttributeFacadeRegistry.build("defense", ShadowTagType.SHORT).single().bind(Attributes.DEFENSE)

        AttributeFacadeRegistry.build("defense_penetration", ShadowTagType.SHORT).single().bind(Attributes.DEFENSE_PENETRATION)

        AttributeFacadeRegistry.build("defense_penetration_rate", ShadowTagType.DOUBLE).single().bind(Attributes.DEFENSE_PENETRATION_RATE)

        AttributeFacadeRegistry.build("element_defense", ShadowTagType.SHORT).single().element().bind(
            Attributes.byElement { DEFENSE }
        )

        AttributeFacadeRegistry.build("health_regeneration", ShadowTagType.SHORT).single().bind(Attributes.HEALTH_REGENERATION)

        AttributeFacadeRegistry.build("lifesteal", ShadowTagType.SHORT).single().bind(Attributes.LIFESTEAL)

        AttributeFacadeRegistry.build("lifesteal_rate", ShadowTagType.DOUBLE).single().bind(Attributes.LIFESTEAL_RATE)

        AttributeFacadeRegistry.build("mana_consumption_rate", ShadowTagType.DOUBLE).single().bind(Attributes.MANA_CONSUMPTION_RATE)

        AttributeFacadeRegistry.build("mana_regeneration", ShadowTagType.SHORT).single().bind(Attributes.MANA_REGENERATION)

        AttributeFacadeRegistry.build("manasteal", ShadowTagType.SHORT).single().bind(Attributes.MANASTEAL)

        AttributeFacadeRegistry.build("manasteal_rate", ShadowTagType.DOUBLE).single().bind(Attributes.MANASTEAL_RATE)

        AttributeFacadeRegistry.build("max_absorption", ShadowTagType.SHORT).single().bind(Attributes.MAX_ABSORPTION)

        AttributeFacadeRegistry.build("max_health", ShadowTagType.SHORT).single().bind(Attributes.MAX_HEALTH)

        AttributeFacadeRegistry.build("max_mana", ShadowTagType.SHORT).single().bind(Attributes.MAX_MANA)

        AttributeFacadeRegistry.build("movement_speed_rate", ShadowTagType.DOUBLE).single().bind(Attributes.MOVEMENT_SPEED_RATE)
    }
}

object AttributeFacadeRegistry {
    @InternalApi
    val schemeBuilderRegistry: MutableMap<Key, SchemeBuilder> = hashMapOf()

    @InternalApi
    val schemeBakerRegistry: MutableMap<Key, SchemeBaker> = hashMapOf()

    @InternalApi
    val shadowTagEncoder: MutableMap<Key, ShadowTagEncoder> = hashMapOf()

    @InternalApi
    val shadowTagDecoder: MutableMap<Key, ShadowTagDecoder> = hashMapOf()

    @InternalApi
    val attributeFactoryRegistry: MutableMap<Key, AttributeFactory<*>> = hashMapOf()

    fun build(key: String, type: ShadowTagType): FormatSelection {
        return FormatSelectionImpl(Key.key(Core.ATTRIBUTE_NAMESPACE, key), type)
    }
}

interface FormatSelection {
    fun ranged(): RangedSelection
    fun single(): SingleSelection
}

interface SingleSelection : SingleAttributeBinder {
    fun element(): SingleElementAttributeBinder
}

interface RangedSelection : RangedAttributeBinder {
    fun element(): RangedElementAttributeBinder
}

interface SingleAttributeBinder {
    fun bind(component: Attribute)
}

interface RangedAttributeBinder {
    fun bind(component1: Attribute, component2: Attribute)
}

interface SingleElementAttributeBinder {
    fun bind(component: (Element) -> ElementAttribute)
}

interface RangedElementAttributeBinder {
    fun bind(component1: (Element) -> ElementAttribute, component2: (Element) -> ElementAttribute)
}

// Implementations Start!

// FIXME unused
private enum class AttributeFormat {
    S,
    LU,
    SE,
    LUE
}

// Map's KFunction Type: () -> SchemeAttributeValue // FIXME unused
private val FORMAT_2_SAV: Map<AttributeFormat, KFunction<SchemeAttributeValue>> = buildMap {
    this[AttributeFormat.S] = SchemeAttributeValueS::deserialize
    this[AttributeFormat.LU] = SchemeAttributeValueLU::deserialize
    this[AttributeFormat.SE] = SchemeAttributeValueSE::deserialize
    this[AttributeFormat.LUE] = SchemeAttributeValueLUE::deserialize
}

// Map's KFunction Type: () -> SchemeAttributeValue // FIXME unused
private val BAV_CLASS_2_SAV: Map<KClass<out BinaryAttributeValue>, KFunction<SchemeAttributeValue>> = buildMap {
    this[BinaryAttributeValueS::class] = SchemeAttributeValueS::deserialize
    this[BinaryAttributeValueLU::class] = SchemeAttributeValueLU::deserialize
    this[BinaryAttributeValueSE::class] = SchemeAttributeValueSE::deserialize
    this[BinaryAttributeValueLUE::class] = SchemeAttributeValueLUE::deserialize
}

// Map's KFunction Type: Map<ShadowTagType, CompoundShadowTag.(String, Number) -> Unit>
private val TAG_TYPE_2_TAG_SETTER_MAP: Map<ShadowTagType, KFunction<Unit>> = buildMap {
    this[ShadowTagType.BYTE] = CompoundShadowTag::putByte
    this[ShadowTagType.SHORT] = CompoundShadowTag::putShort
    this[ShadowTagType.INT] = CompoundShadowTag::putInt
    this[ShadowTagType.LONG] = CompoundShadowTag::putLong
    this[ShadowTagType.FLOAT] = CompoundShadowTag::putFloat
    this[ShadowTagType.DOUBLE] = CompoundShadowTag::putDouble
}

// Map's KFunction Type: CompoundShadowTag.(String) -> Number
private val TAG_TYPE_2_TAG_GETTER_MAP: Map<ShadowTagType, KFunction<Number>> = buildMap {
    this[ShadowTagType.BYTE] = CompoundShadowTag::getByte
    this[ShadowTagType.SHORT] = CompoundShadowTag::getShort
    this[ShadowTagType.INT] = CompoundShadowTag::getInt
    this[ShadowTagType.LONG] = CompoundShadowTag::getLong
    this[ShadowTagType.FLOAT] = CompoundShadowTag::getFloat
    this[ShadowTagType.DOUBLE] = CompoundShadowTag::getDouble
}

// Map's KFunction Type: (Number) -> Number
private val TAG_TYPE_2_NUMBER_CONVERTER_MAP: Map<ShadowTagType, KFunction<Number>> = buildMap {
    this[ShadowTagType.BYTE] = Number::toStableByte
    this[ShadowTagType.SHORT] = Number::toStableShort
    this[ShadowTagType.INT] = Number::toStableInt
    this[ShadowTagType.LONG] = Number::toStableLong
    this[ShadowTagType.FLOAT] = Number::toStableFloat
    this[ShadowTagType.DOUBLE] = Number::toStableDouble
}

private inline fun <reified T : BinaryAttributeValue> constructBinaryAttributeValue(vararg args: Any): BinaryAttributeValue {
    val clazz = T::class
    val constructor = requireNotNull(clazz.primaryConstructor) { "Class does not have a primary constructor" }
    return constructor.call(args)
}

private fun <K, V> Map<K, V>.getOrThrow(key: K): V {
    return get(key) ?: throw NoSuchElementException("Can't find corresponding value for key $key")
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
        val format = AttributeFormat.S

        // register scheme builder
        AttributeFacadeRegistry.schemeBuilderRegistry[key] = SchemeBuilder { node ->
            SchemeAttributeValueS.deserialize(node)
        }

        // register scheme baker
        AttributeFacadeRegistry.schemeBakerRegistry[key] = SchemeBaker { scheme, factor ->
            scheme as SchemeAttributeValueS
            val value0 = scheme.value.calculate(factor)
            val value = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getOrThrow(type).call(value0)
            val operation = scheme.operation
            constructBinaryAttributeValue<BinaryAttributeValueS<*>>(value, operation)
        }

        // register shadow tag encoder
        AttributeFacadeRegistry.shadowTagEncoder[key] = ShadowTagEncoder { binaryValue ->
            compoundShadowTag {
                binaryValue as BinaryAttributeValueS<*>
                TAG_TYPE_2_TAG_SETTER_MAP.getOrThrow(type).call(this, AttributeTagNames.VALUE, binaryValue.value)
                putOperation(AttributeTagNames.OPERATION, binaryValue.operation)
            }
        }

        // register shadow tag decoder
        AttributeFacadeRegistry.shadowTagDecoder[key] = ShadowTagDecoder { shadowTag ->
            shadowTag as CompoundShadowTag
            val value = TAG_TYPE_2_TAG_GETTER_MAP.getOrThrow(type).call(shadowTag, AttributeTagNames.VALUE)
            val operation = shadowTag.getOperation(AttributeTagNames.OPERATION)
            constructBinaryAttributeValue<BinaryAttributeValueS<*>>(value, operation)
        }

        // register attribute factory
        AttributeFacadeRegistry.attributeFactoryRegistry[key] = AttributeFactory<BinaryAttributeValueS<*>> { uuid, value ->
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
        val format = AttributeFormat.LU

        // register scheme builder
        AttributeFacadeRegistry.schemeBuilderRegistry[key] = SchemeBuilder { node ->
            SchemeAttributeValueLU.deserialize(node)
        }

        // register scheme baker
        AttributeFacadeRegistry.schemeBakerRegistry[key] = SchemeBaker { scheme, factor ->
            scheme as SchemeAttributeValueLU
            val lower0 = scheme.lower.calculate(factor)
            val upper0 = scheme.upper.calculate(factor)
            val lower = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getOrThrow(type).call(lower0)
            val upper = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getOrThrow(type).call(upper0)
            val operation = scheme.operation
            constructBinaryAttributeValue<BinaryAttributeValueLU<*>>(lower, upper, operation)
        }

        // register shadow tag encoder
        AttributeFacadeRegistry.shadowTagEncoder[key] = ShadowTagEncoder { binaryValue ->
            compoundShadowTag {
                binaryValue as BinaryAttributeValueLU<*>
                TAG_TYPE_2_TAG_SETTER_MAP.getOrThrow(type).call(this, AttributeTagNames.MIN_VALUE, binaryValue.lower)
                TAG_TYPE_2_TAG_SETTER_MAP.getOrThrow(type).call(this, AttributeTagNames.MAX_VALUE, binaryValue.upper)
                putOperation(AttributeTagNames.OPERATION, binaryValue.operation)
            }
        }

        // register shadow tag decoder
        AttributeFacadeRegistry.shadowTagDecoder[key] = ShadowTagDecoder { shadowTag ->
            shadowTag as CompoundShadowTag
            val lower = TAG_TYPE_2_TAG_GETTER_MAP.getOrThrow(type).call(shadowTag, AttributeTagNames.MIN_VALUE)
            val upper = TAG_TYPE_2_TAG_GETTER_MAP.getOrThrow(type).call(shadowTag, AttributeTagNames.MAX_VALUE)
            val operation = shadowTag.getOperation(AttributeTagNames.OPERATION)
            constructBinaryAttributeValue<BinaryAttributeValueLU<*>>(lower, upper, operation)
        }

        // register attribute factory
        AttributeFacadeRegistry.attributeFactoryRegistry[key] = AttributeFactory<BinaryAttributeValueLU<*>> { uuid, value ->
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
        val format = AttributeFormat.SE

        // register scheme builder
        AttributeFacadeRegistry.schemeBuilderRegistry[key] = SchemeBuilder { node ->
            SchemeAttributeValueSE.deserialize(node)
        }

        // register scheme baker
        AttributeFacadeRegistry.schemeBakerRegistry[key] = SchemeBaker { scheme, factor ->
            scheme as SchemeAttributeValueSE
            val value0 = scheme.value.calculate(factor)
            val value = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getOrThrow(type).call(value0)
            val element = scheme.element
            val operation = scheme.operation
            constructBinaryAttributeValue<BinaryAttributeValueSE<*>>(value, element, operation)
        }

        // register shadow tag encoder
        AttributeFacadeRegistry.shadowTagEncoder[key] = ShadowTagEncoder { binaryValue ->
            compoundShadowTag {
                binaryValue as BinaryAttributeValueSE<*>
                TAG_TYPE_2_TAG_SETTER_MAP.getOrThrow(type).call(this, AttributeTagNames.VALUE, binaryValue.value)
                putElement(AttributeTagNames.ELEMENT, binaryValue.element)
                putOperation(AttributeTagNames.OPERATION, binaryValue.operation)
            }
        }

        // register shadow tag decoder
        AttributeFacadeRegistry.shadowTagDecoder[key] = ShadowTagDecoder { shadowTag ->
            shadowTag as CompoundShadowTag
            val value = TAG_TYPE_2_TAG_GETTER_MAP.getOrThrow(type).call(shadowTag, AttributeTagNames.VALUE)
            val element = shadowTag.getElement(AttributeTagNames.ELEMENT)
            val operation = shadowTag.getOperation(AttributeTagNames.OPERATION)
            constructBinaryAttributeValue<BinaryAttributeValueSE<*>>(value, element, operation)
        }

        // register attribute factory
        AttributeFacadeRegistry.attributeFactoryRegistry[key] = AttributeFactory<BinaryAttributeValueSE<*>> { uuid, value ->
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
        val format = AttributeFormat.LUE

        // register scheme builder
        AttributeFacadeRegistry.schemeBuilderRegistry[key] = SchemeBuilder { node ->
            SchemeAttributeValueLUE.deserialize(node)
        }

        // register scheme baker
        AttributeFacadeRegistry.schemeBakerRegistry[key] = SchemeBaker { scheme, factor ->
            scheme as SchemeAttributeValueLUE
            val lower0 = scheme.lower.calculate(factor)
            val upper0 = scheme.upper.calculate(factor)
            val lower = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getOrThrow(type).call(lower0)
            val upper = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getOrThrow(type).call(upper0)
            val element = scheme.element
            val operation = scheme.operation
            constructBinaryAttributeValue<BinaryAttributeValueLUE<*>>(lower, upper, element, operation)
        }

        // register shadow tag encoder
        AttributeFacadeRegistry.shadowTagEncoder[key] = ShadowTagEncoder { binaryValue ->
            compoundShadowTag {
                binaryValue as BinaryAttributeValueLUE<*>
                TAG_TYPE_2_TAG_SETTER_MAP.getOrThrow(type).call(this, AttributeTagNames.MIN_VALUE, binaryValue.lower)
                TAG_TYPE_2_TAG_SETTER_MAP.getOrThrow(type).call(this, AttributeTagNames.MAX_VALUE, binaryValue.upper)
                putElement(AttributeTagNames.ELEMENT, binaryValue.element)
                putOperation(AttributeTagNames.OPERATION, binaryValue.operation)
            }
        }

        // register shadow tag decoder
        AttributeFacadeRegistry.shadowTagDecoder[key] = ShadowTagDecoder { shadowTag ->
            shadowTag as CompoundShadowTag
            val lower = TAG_TYPE_2_TAG_GETTER_MAP.getOrThrow(type).call(shadowTag, AttributeTagNames.MIN_VALUE)
            val upper = TAG_TYPE_2_TAG_GETTER_MAP.getOrThrow(type).call(shadowTag, AttributeTagNames.MAX_VALUE)
            val element = shadowTag.getElement(AttributeTagNames.ELEMENT)
            val operation = shadowTag.getOperation(AttributeTagNames.OPERATION)
            constructBinaryAttributeValue<BinaryAttributeValueLUE<*>>(lower, upper, element, operation)
        }

        // register attribute factory
        AttributeFacadeRegistry.attributeFactoryRegistry[key] = AttributeFactory<BinaryAttributeValueLUE<*>> { uuid, value ->
            ImmutableMap.of(
                component1(value.element),
                AttributeModifier(uuid, value.lower.toStableDouble(), value.operation),
                component2(value.element),
                AttributeModifier(uuid, value.upper.toStableDouble(), value.operation),
            )
        }
    }
}
