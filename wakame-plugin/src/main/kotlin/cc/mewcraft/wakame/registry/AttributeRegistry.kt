package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.attribute.ElementAttribute
import cc.mewcraft.wakame.attribute.facade.*
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.util.*
import com.google.common.collect.ImmutableMap
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.lang.invoke.MethodHandle
import java.util.EnumMap
import kotlin.math.max

/**
 * This singleton holds various implementations for **each** attribute.
 *
 * Currently, the types of implementations are the following:
 * - [SchemaAttributeDataBaker]
 * - [SchemaAttributeDataNodeEncoder]
 * - [PlainAttributeDataNbtEncoder]
 * - [PlainAttributeDataNbtDecoder]
 * - [AttributeModifierFactory]
 * - [AttributeStructMetadata]
 *
 * Check their kdoc for what they do.
 */
@PreWorldDependency(runBefore = [ElementRegistry::class])
@ReloadDependency(runBefore = [ElementRegistry::class])
object AttributeRegistry : Initializable {

    /**
     * The key of the empty attribute.
     */
    val EMPTY_KEY: Key = Attributes.EMPTY.key()

    val schemaDataBaker: MutableMap<Key, SchemaAttributeDataBaker> = HashMap()
    val schemaNodeEncoder: MutableMap<Key, SchemaAttributeDataNodeEncoder> = HashMap()
    val plainNodeEncoder: MutableMap<Key, PlainAttributeDataNodeEncoder> = HashMap()
    val plainNbtEncoder: MutableMap<Key, PlainAttributeDataNbtEncoder> = HashMap()
    val plainNbtDecoder: MutableMap<Key, PlainAttributeDataNbtDecoder> = HashMap()
    val modifierFactory: MutableMap<Key, AttributeModifierFactory> = HashMap()
    val structMetadata: MutableMap<Key, AttributeStructMetadata> = HashMap()

    /**
     * Registers an attribute facade.
     *
     * ## 参数: [key]
     * 词条在 NBT/模板 中的唯一标识，用来定位词条的序列化实现。
     *
     * 注意，这仅仅是词条在 NBT/模板 中的唯一标识。底层由多个对象组成的词条标识就与这里的 [key] 不同。
     *
     * 例如攻击力这个属性词条，底层实际上是由两个属性组成的，分别是 `MIN_ATTACK_DAMAGE` 和
     * `MAX_ATTACK_DAMAGE`，但攻击力属性词条在 NBT/模板中的标识是一个经过“合并”得到的
     * `attribute:attack_damage`.
     *
     * ## 参数: [type]
     * 词条在 NBT 中的数据类型。
     */
    private fun build(key: String, type: ShadowTagType): FormatSelection {
        return FormatSelectionImpl(Key.key(NekoNamespaces.ATTRIBUTE, key), type)
    }

    private fun register() {
        // register regular attributes
        build("attack_damage", ShadowTagType.SHORT).ranged().element().bind(Attributes.byElement { MIN_ATTACK_DAMAGE }, Attributes.byElement { MAX_ATTACK_DAMAGE })
        build("attack_effect_chance", ShadowTagType.DOUBLE).single().bind(Attributes.ATTACK_EFFECT_CHANCE)
        build("attack_speed_level", ShadowTagType.BYTE).single().bind(Attributes.ATTACK_SPEED_LEVEL)
        build("block_interaction_range", ShadowTagType.DOUBLE).single().bind(Attributes.BLOCK_INTERACTION_RANGE)
        build("critical_strike_chance", ShadowTagType.DOUBLE).single().bind(Attributes.CRITICAL_STRIKE_CHANCE)
        build("critical_strike_power", ShadowTagType.DOUBLE).single().bind(Attributes.CRITICAL_STRIKE_POWER)
        build("damage_reduction_rate", ShadowTagType.DOUBLE).single().bind(Attributes.DAMAGE_REDUCTION_RATE)
        build("defense", ShadowTagType.SHORT).single().element().bind(Attributes.byElement { DEFENSE })
        build("defense_penetration", ShadowTagType.SHORT).single().element().bind(Attributes.byElement { DEFENSE_PENETRATION })
        build("defense_penetration_rate", ShadowTagType.DOUBLE).single().element().bind(Attributes.byElement { DEFENSE_PENETRATION_RATE })
        build("entity_interaction_range", ShadowTagType.DOUBLE).single().bind(Attributes.ENTITY_INTERACTION_RANGE)
        build("health_regeneration", ShadowTagType.SHORT).single().bind(Attributes.HEALTH_REGENERATION)
        build("lifesteal", ShadowTagType.SHORT).single().bind(Attributes.LIFESTEAL)
        build("lifesteal_rate", ShadowTagType.DOUBLE).single().bind(Attributes.LIFESTEAL_RATE)
        build("mana_consumption_rate", ShadowTagType.DOUBLE).single().bind(Attributes.MANA_CONSUMPTION_RATE)
        build("mana_regeneration", ShadowTagType.SHORT).single().bind(Attributes.MANA_REGENERATION)
        build("manasteal", ShadowTagType.SHORT).single().bind(Attributes.MANASTEAL)
        build("manasteal_rate", ShadowTagType.DOUBLE).single().bind(Attributes.MANASTEAL_RATE)
        build("max_absorption", ShadowTagType.SHORT).single().bind(Attributes.MAX_ABSORPTION)
        build("max_health", ShadowTagType.SHORT).single().bind(Attributes.MAX_HEALTH)
        build("max_mana", ShadowTagType.SHORT).single().bind(Attributes.MAX_MANA)
        build("movement_speed_rate", ShadowTagType.DOUBLE).single().bind(Attributes.MOVEMENT_SPEED_RATE)
    }

    override fun onPreWorld() {
        register()
    }

    override fun onReload() {
        // TODO("Not yet implemented") // what to reload?
    }
}

//<editor-fold desc="Struct">
/**
 * 属性结构体的元数据。
 */
data class AttributeStructMetadata(
    /**
     * 数值的格式。
     */
    val format: Format,
    /**
     * 是否为元素属性。
     */
    val element: Boolean,
) {
    enum class Format { SINGLE, RANGED }
}
//</editor-fold>

//<editor-fold desc="Builder">
interface FormatSelection {
    fun single(): SingleSelection
    fun ranged(): RangedSelection
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
//</editor-fold>

// fun interface AttributeSchemaCoreDataBuilder : SchemaCoreDataBuilder<ConfigurationNode, SchemaAttributeData>
// fun interface AttributeSchemaCoreDataBaker : SchemaCoreDataBaker<SchemaAttributeData, PlainAttributeData>
// fun interface AttributeNbtCoreDataEncoder : NbtCoreDataEncoder<PlainAttributeData, CompoundShadowTag>
// fun interface AttributeNbtCoreDataDecoder : NbtCoreDataDecoder<CompoundShadowTag, PlainAttributeData>

//<editor-fold desc="Support">
private class FormatSelectionImpl(
    private val key: Key,
    private val type: ShadowTagType,
) : FormatSelection {
    override fun single(): SingleSelectionImpl {
        return SingleSelectionImpl(key, type)
    }

    override fun ranged(): RangedSelectionImpl {
        return RangedSelectionImpl(key, type)
    }
}

private class SingleSelectionImpl(
    private val key: Key,
    private val type: ShadowTagType,
) : SingleSelection {
    override fun element(): SingleElementAttributeBinderImpl {
        return SingleElementAttributeBinderImpl(key, type)
    }

    /**
     * Components: Op, Single
     */
    override fun bind(component: Attribute) {
        AttributeRegistry.schemaDataBaker[key] = SchemaAttributeDataBaker { schema, factor ->
            schema as SchemaAttributeData.S
            val operation = schema.operation
            val value = schema.value.calculate(factor)
            PlainAttributeData.S(operation, value)
        }

        AttributeRegistry.schemaNodeEncoder[key] = SchemaAttributeDataNodeEncoder { node ->
            val operation = node.operation()
            val value = node.schemaSingle()
            SchemaAttributeData.S(operation, value)
        }

        AttributeRegistry.plainNbtEncoder[key] = PlainAttributeDataNbtEncoder { compound ->
            val operation = compound.getOperation()
            val value = compound.getNumber(NekoTags.Attribute.VAL)
            PlainAttributeData.S(operation, value)
        }

        AttributeRegistry.plainNbtDecoder[key] = PlainAttributeDataNbtDecoder { value ->
            value as PlainAttributeData.S
            val compound = CompoundShadowTag.create()
            compound.putId(key)
            compound.putNumber(NekoTags.Attribute.VAL, value.value, type)
            compound.putOperation(value.operation)
            compound
        }

        AttributeRegistry.plainNodeEncoder[key] = PlainAttributeDataNodeEncoder { node ->
            val operation = node.operation()
            val value = node.plainSingle()
            PlainAttributeData.S(operation, value)
        }

        AttributeRegistry.modifierFactory[key] = AttributeModifierFactory { uuid, value ->
            value as PlainAttributeData.S
            val modifier = AttributeModifier(uuid, value.value.toStableDouble(), value.operation)
            ImmutableMap.of(component, modifier)
        }

        AttributeRegistry.structMetadata[key] = AttributeStructMetadata(AttributeStructMetadata.Format.SINGLE, false)
    }
}

private class RangedSelectionImpl(
    private val key: Key,
    private val type: ShadowTagType,
) : RangedSelection {
    override fun element(): RangedElementAttributeBinderImpl {
        return RangedElementAttributeBinderImpl(key, type)
    }

    /**
     * Components: Op, Ranged
     */
    override fun bind(
        component1: Attribute,
        component2: Attribute,
    ) {
        AttributeRegistry.schemaDataBaker[key] = SchemaAttributeDataBaker { schema, factor ->
            schema as SchemaAttributeData.R
            val operation = schema.operation
            val lower = schema.lower.calculate(factor)
            val upper = schema.upper.calculate(factor)
            PlainAttributeData.R(operation, lower, max(lower, upper))
        }

        AttributeRegistry.schemaNodeEncoder[key] = SchemaAttributeDataNodeEncoder { node ->
            val operation = node.operation()
            val lower = node.schemaLower()
            val upper = node.schemaUpper()
            SchemaAttributeData.R(operation, lower, upper)
        }

        AttributeRegistry.plainNbtEncoder[key] = PlainAttributeDataNbtEncoder { compound ->
            val lower = compound.getNumber(NekoTags.Attribute.MIN)
            val upper = compound.getNumber(NekoTags.Attribute.MAX)
            val operation = compound.getOperation()
            PlainAttributeData.R(operation, lower, upper)
        }

        AttributeRegistry.plainNbtDecoder[key] = PlainAttributeDataNbtDecoder { value ->
            value as PlainAttributeData.R
            val compound = CompoundShadowTag.create()
            compound.putId(key)
            compound.putNumber(NekoTags.Attribute.MIN, value.lower, type)
            compound.putNumber(NekoTags.Attribute.MAX, value.upper, type)
            compound.putOperation(value.operation)
            compound
        }

        AttributeRegistry.plainNodeEncoder[key] = PlainAttributeDataNodeEncoder { node ->
            val operation = node.operation()
            val lower = node.plainLower()
            val upper = node.plainUpper()
            PlainAttributeData.R(operation, lower, upper)
        }

        AttributeRegistry.modifierFactory[key] = AttributeModifierFactory { uuid, value ->
            value as PlainAttributeData.R
            val modifier1 = AttributeModifier(uuid, value.lower.toStableDouble(), value.operation)
            val modifier2 = AttributeModifier(uuid, value.upper.toStableDouble(), value.operation)
            ImmutableMap.of(component1, modifier1, component2, modifier2)
        }

        AttributeRegistry.structMetadata[key] = AttributeStructMetadata(AttributeStructMetadata.Format.RANGED, false)
    }
}

private class SingleElementAttributeBinderImpl(
    private val key: Key,
    private val type: ShadowTagType,
) : SingleElementAttributeBinder {

    /**
     * Components: Op, Single, Element
     */
    override fun bind(
        component: (Element) -> ElementAttribute,
    ) {
        AttributeRegistry.schemaDataBaker[key] = SchemaAttributeDataBaker { schema, factor ->
            schema as SchemaAttributeData.SE
            val operation = schema.operation
            val value = schema.value.calculate(factor)
            val element = schema.element
            PlainAttributeData.SE(operation, value, element)
        }

        AttributeRegistry.schemaNodeEncoder[key] = SchemaAttributeDataNodeEncoder { node ->
            val operation = node.operation()
            val value = node.schemaSingle()
            val element = node.element()
            SchemaAttributeData.SE(operation, value, element)
        }

        AttributeRegistry.plainNbtEncoder[key] = PlainAttributeDataNbtEncoder { compound ->
            val value = compound.getNumber(NekoTags.Attribute.VAL)
            val element = compound.getElement()
            val operation = compound.getOperation()
            PlainAttributeData.SE(operation, value, element)
        }

        AttributeRegistry.plainNbtDecoder[key] = PlainAttributeDataNbtDecoder { value ->
            value as PlainAttributeData.SE
            val compound = CompoundShadowTag.create()
            compound.putId(key)
            compound.putNumber(NekoTags.Attribute.VAL, value.value, type)
            compound.putElement(value.element)
            compound.putOperation(value.operation)
            compound
        }

        AttributeRegistry.plainNodeEncoder[key] = PlainAttributeDataNodeEncoder { node ->
            val operation = node.operation()
            val value = node.plainSingle()
            val element = node.element()
            PlainAttributeData.SE(operation, value, element)
        }

        AttributeRegistry.modifierFactory[key] = AttributeModifierFactory { uuid, value ->
            value as PlainAttributeData.SE
            val modifier = AttributeModifier(uuid, value.value.toStableDouble(), value.operation)
            ImmutableMap.of(component(value.element), modifier)
        }

        AttributeRegistry.structMetadata[key] = AttributeStructMetadata(AttributeStructMetadata.Format.SINGLE, true)
    }
}

private class RangedElementAttributeBinderImpl(
    private val key: Key,
    private val type: ShadowTagType,
) : RangedElementAttributeBinder {

    /**
     * Components: Op, Ranged, Element
     */
    override fun bind(
        component1: (Element) -> ElementAttribute,
        component2: (Element) -> ElementAttribute,
    ) {
        AttributeRegistry.schemaDataBaker[key] = SchemaAttributeDataBaker { schema, factor ->
            schema as SchemaAttributeData.RE
            val operation = schema.operation
            val lower = schema.lower.calculate(factor)
            val upper = schema.upper.calculate(factor)
            val element = schema.element
            PlainAttributeData.RE(operation, lower, max(lower, upper), element)
        }

        AttributeRegistry.schemaNodeEncoder[key] = SchemaAttributeDataNodeEncoder { node ->
            val operation = node.operation()
            val lower = node.schemaLower()
            val upper = node.schemaUpper()
            val element = node.element()
            SchemaAttributeData.RE(operation, lower, upper, element)
        }

        AttributeRegistry.plainNbtEncoder[key] = PlainAttributeDataNbtEncoder { compound ->
            val lower = compound.getNumber(NekoTags.Attribute.MIN)
            val upper = compound.getNumber(NekoTags.Attribute.MAX)
            val element = compound.getElement()
            val operation = compound.getOperation()
            PlainAttributeData.RE(operation, lower, upper, element)
        }

        AttributeRegistry.plainNbtDecoder[key] = PlainAttributeDataNbtDecoder { value ->
            value as PlainAttributeData.RE
            val compound = CompoundShadowTag.create()
            compound.putId(key)
            compound.putNumber(NekoTags.Attribute.MIN, value.lower, type)
            compound.putNumber(NekoTags.Attribute.MAX, value.upper, type)
            compound.putElement(value.element)
            compound.putOperation(value.operation)
            compound
        }

        AttributeRegistry.plainNodeEncoder[key] = PlainAttributeDataNodeEncoder { node ->
            val operation = node.operation()
            val lower = node.plainLower()
            val upper = node.plainUpper()
            val element = node.element()
            PlainAttributeData.RE(operation, lower, upper, element)
        }

        AttributeRegistry.modifierFactory[key] = AttributeModifierFactory { uuid, value ->
            value as PlainAttributeData.RE
            val modifier1 = AttributeModifier(uuid, value.lower.toStableDouble(), value.operation)
            val modifier2 = AttributeModifier(uuid, value.upper.toStableDouble(), value.operation)
            ImmutableMap.of(component1(value.element), modifier1, component2(value.element), modifier2)
        }

        AttributeRegistry.structMetadata[key] = AttributeStructMetadata(AttributeStructMetadata.Format.RANGED, true)
    }
}

// Map Value's KFunction Type: Map<ShadowTagType, CompoundShadowTag.(String, Number) -> Unit>
private val TAG_TYPE_2_TAG_SETTER_MAP: Map<ShadowTagType, MethodHandle> =
    buildMap {
        this[ShadowTagType.BYTE] = CompoundShadowTag::putByte
        this[ShadowTagType.SHORT] = CompoundShadowTag::putShort
        this[ShadowTagType.INT] = CompoundShadowTag::putInt
        this[ShadowTagType.LONG] = CompoundShadowTag::putLong
        this[ShadowTagType.FLOAT] = CompoundShadowTag::putFloat
        this[ShadowTagType.DOUBLE] = CompoundShadowTag::putDouble
    }.mapValues { it.value.toMethodHandle() }.let { EnumMap(it) }

// Map Value's KFunction Type: (Number) -> Number
private val TAG_TYPE_2_NUMBER_CONVERTER_MAP: Map<ShadowTagType, MethodHandle> =
    buildMap {
        this[ShadowTagType.BYTE] = Number::toStableByte
        this[ShadowTagType.SHORT] = Number::toStableShort
        this[ShadowTagType.INT] = Number::toStableInt
        this[ShadowTagType.LONG] = Number::toStableLong
        this[ShadowTagType.FLOAT] = Number::toStableFloat
        this[ShadowTagType.DOUBLE] = Number::toStableDouble
    }.mapValues { it.value.toMethodHandle() }.let { EnumMap(it) }

/* Specialized Compound Operations */

private fun CompoundShadowTag.getElement(): Element {
    return this.getByteOrNull(NekoTags.Attribute.ELEMENT)?.let { ElementRegistry.getByOrThrow(it) } ?: ElementRegistry.DEFAULT
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

private fun CompoundShadowTag.putNumber(key: String, value: Double, shadowTagType: ShadowTagType) {
    val converted = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getOrThrow(shadowTagType).invoke(value)
    TAG_TYPE_2_TAG_SETTER_MAP.getOrThrow(shadowTagType).invoke(this, key, converted)
}

private fun CompoundShadowTag.putId(id: Key) {
    this.putString(NekoTags.Cell.CORE_KEY, id.asString())
}

/* Specialized Configuration Operations */

private fun ConfigurationNode.plainSingle(): Double {
    return node("value").requireKt<Double>()
}

private fun ConfigurationNode.plainLower(): Double {
    return node("lower").requireKt<Double>()
}

private fun ConfigurationNode.plainUpper(): Double {
    return node("upper").requireKt<Double>()
}

private fun ConfigurationNode.schemaSingle(): RandomizedValue {
    return node("value").requireKt<RandomizedValue>()
}

private fun ConfigurationNode.schemaLower(): RandomizedValue {
    return node("lower").requireKt<RandomizedValue>()
}

private fun ConfigurationNode.schemaUpper(): RandomizedValue {
    return node("upper").requireKt<RandomizedValue>()
}

private fun ConfigurationNode.element(): Element {
    return node("element").requireKt<Element>()
}

private fun ConfigurationNode.operation(): AttributeModifier.Operation {
    return node("operation").string?.let { AttributeModifier.Operation.byKey(it) } ?: AttributeModifier.Operation.ADD
}
//</editor-fold>
