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
import java.util.UUID
import kotlin.math.max

/**
 * This singleton holds various implementations for **each** attribute.
 *
 * Currently, the types of implementations are the following:
 * - [SchemaAttributeDataRealizer]
 * - [SchemaAttributeDataNodeEncoder]
 * - [BinaryAttributeDataNbtEncoder]
 * - [BinaryAttributeDataNbtDecoder]
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

    /**
     * The facades of all attributes.
     */
    val FACADES: Registry<Key, AttributeFacade> = SimpleRegistry()

    /**
     * Build an attribute facade.
     *
     * @param key 词条在 NBT/模板 中的唯一标识，用来定位词条的序列化实现。
     *
     * 注意，这仅仅是词条在 NBT/模板 中的唯一标识。底层由多个对象组成的词条标识就与这里的 [key] 不同。
     *
     * 例如攻击力这个属性词条，底层实际上是由两个属性组成的，分别是 `MIN_ATTACK_DAMAGE` 和
     * `MAX_ATTACK_DAMAGE`，但攻击力属性词条在 NBT/模板中的标识是一个经过“合并”得到的
     * `attribute:attack_damage`.
     *
     * @param type 词条在 NBT 中的数据类型。
     */
    private fun buildFacade(key: String, type: ShadowTagType): FormatSelection {
        return FormatSelectionImpl(Key(NekoNamespaces.ATTRIBUTE, key), type)
    }

    private operator fun AttributeFacade.unaryPlus() {
        FACADES.register(KEY, this)
    }

    private fun registerFacades() {
        +buildFacade("attack_damage", ShadowTagType.SHORT).ranged().element().bind(
            Attributes.byElement { MIN_ATTACK_DAMAGE },
            Attributes.byElement { MAX_ATTACK_DAMAGE }
        )

        +buildFacade("attack_effect_chance", ShadowTagType.DOUBLE).single().bind(
            Attributes.ATTACK_EFFECT_CHANCE
        )

        +buildFacade("attack_speed_level", ShadowTagType.BYTE).single().bind(
            Attributes.ATTACK_SPEED_LEVEL
        )

        +buildFacade("block_interaction_range", ShadowTagType.DOUBLE).single().bind(
            Attributes.BLOCK_INTERACTION_RANGE
        )

        +buildFacade("critical_strike_chance", ShadowTagType.DOUBLE).single().bind(
            Attributes.CRITICAL_STRIKE_CHANCE
        )

        +buildFacade("critical_strike_power", ShadowTagType.DOUBLE).single().bind(
            Attributes.CRITICAL_STRIKE_POWER
        )

        +buildFacade("damage_reduction_rate", ShadowTagType.DOUBLE).single().bind(
            Attributes.DAMAGE_REDUCTION_RATE
        )

        +buildFacade("defense", ShadowTagType.SHORT).single().element().bind(
            Attributes.byElement { DEFENSE }
        )

        +buildFacade("defense_penetration", ShadowTagType.SHORT).single().element().bind(
            Attributes.byElement { DEFENSE_PENETRATION }
        )

        +buildFacade("defense_penetration_rate", ShadowTagType.DOUBLE).single().element().bind(
            Attributes.byElement { DEFENSE_PENETRATION_RATE }
        )

        +buildFacade("entity_interaction_range", ShadowTagType.DOUBLE).single().bind(
            Attributes.ENTITY_INTERACTION_RANGE
        )

        +buildFacade("health_regeneration", ShadowTagType.SHORT).single().bind(
            Attributes.HEALTH_REGENERATION
        )

        +buildFacade("lifesteal", ShadowTagType.SHORT).single().bind(
            Attributes.LIFESTEAL
        )

        +buildFacade("lifesteal_rate", ShadowTagType.DOUBLE).single().bind(
            Attributes.LIFESTEAL_RATE
        )

        +buildFacade("mana_consumption_rate", ShadowTagType.DOUBLE).single().bind(
            Attributes.MANA_CONSUMPTION_RATE
        )

        +buildFacade("mana_regeneration", ShadowTagType.SHORT).single().bind(
            Attributes.MANA_REGENERATION
        )

        +buildFacade("manasteal", ShadowTagType.SHORT).single().bind(
            Attributes.MANASTEAL
        )

        +buildFacade("manasteal_rate", ShadowTagType.DOUBLE).single().bind(
            Attributes.MANASTEAL_RATE
        )

        +buildFacade("max_absorption", ShadowTagType.SHORT).single().bind(
            Attributes.MAX_ABSORPTION
        )

        +buildFacade("max_health", ShadowTagType.SHORT).single().bind(
            Attributes.MAX_HEALTH
        )

        +buildFacade("max_mana", ShadowTagType.SHORT).single().bind(
            Attributes.MAX_MANA
        )

        +buildFacade("movement_speed_rate", ShadowTagType.DOUBLE).single().bind(
            Attributes.MOVEMENT_SPEED_RATE
        )

        // Registry more attribute facade here ...
    }

    override fun onPreWorld() {
        registerFacades()
    }

    override fun onReload() {
        // TODO("Not yet implemented") // what to reload?
    }
}

/**
 * 一个属性相关的所有操作。
 */
@Suppress("PropertyName")
interface AttributeFacade {
    /** 属性的 facade 的 key. */
    val KEY: Key

    val MODIFIER_FACTORY: AttributeModifierFactory
    val STRUCT_METADATA: AttributeStructMetadata
    val SCHEMA_DATA_REALIZER: SchemaAttributeDataRealizer
    val SCHEMA_DATA_NODE_ENCODER: SchemaAttributeDataNodeEncoder
    val BINARY_DATA_NODE_ENCODER: BinaryAttributeDataNodeEncoder
    val BINARY_DATA_NBT_ENCODER: BinaryAttributeDataNbtEncoder
    val BINARY_DATA_NBT_DECODER: BinaryAttributeDataNbtDecoder
}

/**
 * 一个属性结构体的元数据。
 */
data class AttributeStructMetadata(
    val format: Format,
    val element: Boolean,
) {
    enum class Format { SINGLE, RANGED }
}

/**
 * 开始选择 single 或 ranged.
 */
interface FormatSelection {
    fun single(): SingleSelection
    fun ranged(): RangedSelection
}

/**
 * 已选择 single，然后接着构造 element.
 */
interface SingleSelection : SingleAttributeBinder {
    fun element(): SingleElementAttributeBinder
}

/**
 * 已选择 ranged，然后接着构造 element.
 */
interface RangedSelection : RangedAttributeBinder {
    fun element(): RangedElementAttributeBinder
}

/**
 * 已选择 single，然后最终绑定到属性上。
 */
interface SingleAttributeBinder {
    fun bind(component: Attribute): AttributeFacade
}

/**
 * 已选择 ranged，然后最终绑定到属性上。
 */
interface RangedAttributeBinder {
    fun bind(component1: Attribute, component2: Attribute): AttributeFacade
}

/**
 * 已选择 single + element，然后最终绑定到属性上。
 */
interface SingleElementAttributeBinder {
    fun bind(component: (Element) -> ElementAttribute): AttributeFacade
}

/**
 * 已选择 ranged + element，然后最终绑定到属性上。
 */
interface RangedElementAttributeBinder {
    fun bind(component1: (Element) -> ElementAttribute, component2: (Element) -> ElementAttribute): AttributeFacade
}

//<editor-fold desc="Implementation">
private class AttributeFacadeImpl(
    override val KEY: Key,
    override val STRUCT_METADATA: AttributeStructMetadata,
    override val MODIFIER_FACTORY: AttributeModifierFactory,
    override val SCHEMA_DATA_REALIZER: SchemaAttributeDataRealizer,
    override val SCHEMA_DATA_NODE_ENCODER: SchemaAttributeDataNodeEncoder,
    override val BINARY_DATA_NODE_ENCODER: BinaryAttributeDataNodeEncoder,
    override val BINARY_DATA_NBT_ENCODER: BinaryAttributeDataNbtEncoder,
    override val BINARY_DATA_NBT_DECODER: BinaryAttributeDataNbtDecoder,
) : AttributeFacade

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
     * Components: Operation, Single
     */
    override fun bind(
        component: Attribute,
    ): AttributeFacade = AttributeFacadeImpl(
        KEY = key,

        STRUCT_METADATA = AttributeStructMetadata(AttributeStructMetadata.Format.SINGLE, false),

        MODIFIER_FACTORY = { uuid: UUID, value: BinaryAttributeData ->
            value as BinaryAttributeData.S
            val modifier = AttributeModifier(uuid, value.value.toStableDouble(), value.operation)
            ImmutableMap.of(component, modifier)
        },

        SCHEMA_DATA_REALIZER = { schema: SchemaAttributeData, factor: Number ->
            schema as SchemaAttributeData.S
            val operation = schema.operation
            val value = schema.value.calculate(factor)
            BinaryAttributeData.S(operation, value)
        },

        SCHEMA_DATA_NODE_ENCODER = { node: ConfigurationNode ->
            val operation = node.getOperation()
            val value = node.getSchemaSingle()
            SchemaAttributeData.S(operation, value)
        },

        BINARY_DATA_NODE_ENCODER = { node: ConfigurationNode ->
            val operation = node.getOperation()
            val value = node.getBinarySingle()
            BinaryAttributeData.S(operation, value)
        },

        BINARY_DATA_NBT_ENCODER = { compound: CompoundShadowTag ->
            val operation = compound.getOperation()
            val value = compound.getNumber(NekoTags.Attribute.VAL)
            BinaryAttributeData.S(operation, value)
        },

        BINARY_DATA_NBT_DECODER = { value: BinaryAttributeData ->
            value as BinaryAttributeData.S
            val compound = CompoundShadowTag.create()
            compound.putId(key)
            compound.putNumber(NekoTags.Attribute.VAL, value.value, type)
            compound.putOperation(value.operation)
            compound
        }
    )
}

private class RangedSelectionImpl(
    private val key: Key,
    private val type: ShadowTagType,
) : RangedSelection {
    override fun element(): RangedElementAttributeBinderImpl {
        return RangedElementAttributeBinderImpl(key, type)
    }

    /**
     * Components: Operation, Ranged
     */
    override fun bind(
        component1: Attribute,
        component2: Attribute,
    ): AttributeFacade = AttributeFacadeImpl(
        KEY = key,

        STRUCT_METADATA = AttributeStructMetadata(AttributeStructMetadata.Format.RANGED, false),

        MODIFIER_FACTORY = { uuid: UUID, value: BinaryAttributeData ->
            value as BinaryAttributeData.R
            val modifier1 = AttributeModifier(uuid, value.lower.toStableDouble(), value.operation)
            val modifier2 = AttributeModifier(uuid, value.upper.toStableDouble(), value.operation)
            ImmutableMap.of(component1, modifier1, component2, modifier2)
        },

        SCHEMA_DATA_REALIZER = { schema: SchemaAttributeData, factor: Number ->
            schema as SchemaAttributeData.R
            val operation = schema.operation
            val lower = schema.lower.calculate(factor)
            val upper = schema.upper.calculate(factor)
            BinaryAttributeData.R(operation, lower, max(lower, upper))
        },

        SCHEMA_DATA_NODE_ENCODER = { node: ConfigurationNode ->
            val operation = node.getOperation()
            val lower = node.getSchemaLower()
            val upper = node.getSchemaUpper()
            SchemaAttributeData.R(operation, lower, upper)
        },

        BINARY_DATA_NODE_ENCODER = { node: ConfigurationNode ->
            val operation = node.getOperation()
            val lower = node.getBinaryLower()
            val upper = node.getBinaryUpper()
            BinaryAttributeData.R(operation, lower, upper)
        },

        BINARY_DATA_NBT_ENCODER = { compound: CompoundShadowTag ->
            val lower = compound.getNumber(NekoTags.Attribute.MIN)
            val upper = compound.getNumber(NekoTags.Attribute.MAX)
            val operation = compound.getOperation()
            BinaryAttributeData.R(operation, lower, upper)
        },

        BINARY_DATA_NBT_DECODER = { value: BinaryAttributeData ->
            value as BinaryAttributeData.R
            val compound = CompoundShadowTag.create()
            compound.putId(key)
            compound.putNumber(NekoTags.Attribute.MIN, value.lower, type)
            compound.putNumber(NekoTags.Attribute.MAX, value.upper, type)
            compound.putOperation(value.operation)
            compound
        },
    )
}

private class SingleElementAttributeBinderImpl(
    private val key: Key,
    private val type: ShadowTagType,
) : SingleElementAttributeBinder {

    /**
     * Components: Operation, Single, Element
     */
    override fun bind(
        component: (Element) -> ElementAttribute,
    ): AttributeFacade = AttributeFacadeImpl(
        KEY = key,

        STRUCT_METADATA = AttributeStructMetadata(AttributeStructMetadata.Format.SINGLE, true),

        MODIFIER_FACTORY = { uuid: UUID, value: BinaryAttributeData ->
            value as BinaryAttributeData.SE
            val modifier = AttributeModifier(uuid, value.value.toStableDouble(), value.operation)
            ImmutableMap.of(component(value.element), modifier)
        },

        SCHEMA_DATA_REALIZER = { schema: SchemaAttributeData, factor: Number ->
            schema as SchemaAttributeData.SE
            val operation = schema.operation
            val value = schema.value.calculate(factor)
            val element = schema.element
            BinaryAttributeData.SE(operation, value, element)
        },

        SCHEMA_DATA_NODE_ENCODER = { node: ConfigurationNode ->
            val operation = node.getOperation()
            val value = node.getSchemaSingle()
            val element = node.getElement()
            SchemaAttributeData.SE(operation, value, element)
        },

        BINARY_DATA_NODE_ENCODER = { node: ConfigurationNode ->
            val operation = node.getOperation()
            val value = node.getBinarySingle()
            val element = node.getElement()
            BinaryAttributeData.SE(operation, value, element)
        },

        BINARY_DATA_NBT_ENCODER = { compound: CompoundShadowTag ->
            val value = compound.getNumber(NekoTags.Attribute.VAL)
            val element = compound.getElement()
            val operation = compound.getOperation()
            BinaryAttributeData.SE(operation, value, element)
        },

        BINARY_DATA_NBT_DECODER = { value: BinaryAttributeData ->
            value as BinaryAttributeData.SE
            val compound = CompoundShadowTag.create()
            compound.putId(key)
            compound.putNumber(NekoTags.Attribute.VAL, value.value, type)
            compound.putElement(value.element)
            compound.putOperation(value.operation)
            compound
        },
    )
}

private class RangedElementAttributeBinderImpl(
    private val key: Key,
    private val type: ShadowTagType,
) : RangedElementAttributeBinder {

    /**
     * Components: Operation, Ranged, Element
     */
    override fun bind(
        component1: (Element) -> ElementAttribute,
        component2: (Element) -> ElementAttribute,
    ): AttributeFacade = AttributeFacadeImpl(
        KEY = key,

        STRUCT_METADATA = AttributeStructMetadata(AttributeStructMetadata.Format.RANGED, true),

        MODIFIER_FACTORY = { uuid: UUID, value: BinaryAttributeData ->
            value as BinaryAttributeData.RE
            val modifier1 = AttributeModifier(uuid, value.lower.toStableDouble(), value.operation)
            val modifier2 = AttributeModifier(uuid, value.upper.toStableDouble(), value.operation)
            ImmutableMap.of(component1(value.element), modifier1, component2(value.element), modifier2)
        },

        SCHEMA_DATA_REALIZER = { schema: SchemaAttributeData, factor: Number ->
            schema as SchemaAttributeData.RE
            val operation = schema.operation
            val lower = schema.lower.calculate(factor)
            val upper = schema.upper.calculate(factor)
            val element = schema.element
            BinaryAttributeData.RE(operation, lower, max(lower, upper), element)
        },

        SCHEMA_DATA_NODE_ENCODER = { node: ConfigurationNode ->
            val operation = node.getOperation()
            val lower = node.getSchemaLower()
            val upper = node.getSchemaUpper()
            val element = node.getElement()
            SchemaAttributeData.RE(operation, lower, upper, element)
        },

        BINARY_DATA_NODE_ENCODER = { node: ConfigurationNode ->
            val operation = node.getOperation()
            val lower = node.getBinaryLower()
            val upper = node.getBinaryUpper()
            val element = node.getElement()
            BinaryAttributeData.RE(operation, lower, upper, element)
        },

        BINARY_DATA_NBT_ENCODER = { compound: CompoundShadowTag ->
            val lower = compound.getNumber(NekoTags.Attribute.MIN)
            val upper = compound.getNumber(NekoTags.Attribute.MAX)
            val element = compound.getElement()
            val operation = compound.getOperation()
            BinaryAttributeData.RE(operation, lower, upper, element)
        },

        BINARY_DATA_NBT_DECODER = { value: BinaryAttributeData ->
            value as BinaryAttributeData.RE
            val compound = CompoundShadowTag.create()
            compound.putId(key)
            compound.putNumber(NekoTags.Attribute.MIN, value.lower, type)
            compound.putNumber(NekoTags.Attribute.MAX, value.upper, type)
            compound.putElement(value.element)
            compound.putOperation(value.operation)
            compound
        },
    )
}

// Map Value's KFunction Type: CompoundShadowTag.(String, Number) -> Unit
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
    }.let {
        EnumMap(it)
    }

// Map Value's KFunction Type: (Number) -> Number
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
    }.let {
        EnumMap(it)
    }

/* Specialized Compound Operations */

private fun CompoundShadowTag.getElement(): Element {
    return this.getByteOrNull(NekoTags.Attribute.ELEMENT)?.let { ElementRegistry.getBy(it) } ?: ElementRegistry.DEFAULT
}

private fun CompoundShadowTag.putElement(element: Element) {
    this.putByte(NekoTags.Attribute.ELEMENT, element.binaryId)
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
    val converted = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getValue(shadowTagType).invoke(value)
    TAG_TYPE_2_TAG_SETTER_MAP.getValue(shadowTagType).invoke(this, key, converted)
}

private fun CompoundShadowTag.putId(id: Key) {
    this.putString(NekoTags.Cell.CORE_KEY, id.asString())
}

/* Specialized Configuration Operations */

private fun ConfigurationNode.getBinarySingle(): Double {
    return node("value").requireKt<Double>()
}

private fun ConfigurationNode.getBinaryLower(): Double {
    return node("lower").requireKt<Double>()
}

private fun ConfigurationNode.getBinaryUpper(): Double {
    return node("upper").requireKt<Double>()
}

private fun ConfigurationNode.getSchemaSingle(): RandomizedValue {
    return node("value").requireKt<RandomizedValue>()
}

private fun ConfigurationNode.getSchemaLower(): RandomizedValue {
    return node("lower").requireKt<RandomizedValue>()
}

private fun ConfigurationNode.getSchemaUpper(): RandomizedValue {
    return node("upper").requireKt<RandomizedValue>()
}

private fun ConfigurationNode.getElement(): Element {
    return node("element").requireKt<Element>()
}

private fun ConfigurationNode.getOperation(): AttributeModifier.Operation {
    return node("operation").string?.let { AttributeModifier.Operation.byKey(it) } ?: AttributeModifier.Operation.ADD
}
//</editor-fold>
