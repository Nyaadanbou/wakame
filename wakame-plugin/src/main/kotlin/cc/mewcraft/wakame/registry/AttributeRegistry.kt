package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.attribute.base.Attribute
import cc.mewcraft.wakame.attribute.base.AttributeModifier
import cc.mewcraft.wakame.attribute.base.Attributes
import cc.mewcraft.wakame.attribute.base.ElementAttribute
import cc.mewcraft.wakame.attribute.facade.*
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.item.SchemeBaker
import cc.mewcraft.wakame.item.SchemeBuilder
import cc.mewcraft.wakame.item.ShadowTagDecoder
import cc.mewcraft.wakame.item.ShadowTagEncoder
import cc.mewcraft.wakame.registry.AttributeStructMeta.Format
import cc.mewcraft.wakame.util.*
import com.google.common.collect.ImmutableMap
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import java.util.EnumMap
import kotlin.reflect.KFunction

/**
 * This singleton holds various implementations for **each** attribute in
 * the server.
 *
 * Currently, the types of implementations are the following:
 * - [SchemeBuilder]
 * - [SchemeBaker]
 * - [ShadowTagEncoder]
 * - [ShadowTagDecoder]
 * - [AttributeModifierFactory]
 * - [AttributeStructMeta]
 *
 * Check their kdoc for what they do.
 */
@PreWorldDependency(
    runBefore = [ElementRegistry::class]
)
@ReloadDependency(
    runBefore = [ElementRegistry::class]
)
object AttributeRegistry : Initializable {

    /**
     * The key of the empty attribute.
     */
    val EMPTY_KEY: Key = Attributes.EMPTY.key()

    @InternalApi
    val schemeBuilderRegistry: MutableMap<Key, SchemeBuilder> = hashMapOf()

    @InternalApi
    val shadowTagEncoder: MutableMap<Key, ShadowTagEncoder> = hashMapOf()

    @InternalApi
    val shadowTagDecoder: MutableMap<Key, ShadowTagDecoder> = hashMapOf()

    @InternalApi
    val attributeFactoryRegistry: MutableMap<Key, AttributeModifierFactory> = hashMapOf()

    @InternalApi
    val attributeStructRegistry: MutableMap<Key, AttributeStructMeta> = hashMapOf()

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
        return (@OptIn(InternalApi::class) FormatSelectionImpl(Key.key(NekoNamespaces.ATTRIBUTE, key), type))
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

    fun getMeta(key: Key): AttributeStructMeta {
        return (@OptIn(InternalApi::class) attributeStructRegistry[key] ?: error("Can't find attribute struct meta with key '$key'"))
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
data class AttributeStructMeta(
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

/**
 * 属性结构体的所有类型。
 */
enum class AttributeStructType(
    val meta: AttributeStructMeta,
) {
    SINGLE(AttributeStructMeta(Format.SINGLE, false)),
    RANGED(AttributeStructMeta(Format.RANGED, false)),
    SINGLE_ELEMENT(AttributeStructMeta(Format.SINGLE, true)),
    RANGED_ELEMENT(AttributeStructMeta(Format.RANGED, true));
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

//<editor-fold desc="Support">
// TODO use MethodHandle for better reflection performance

@InternalApi
private class FormatSelectionImpl(
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
private class SingleSelectionImpl(
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
private class RangedSelectionImpl(
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
private class SingleElementAttributeBinderImpl(
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
private class RangedElementAttributeBinderImpl(
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
//</editor-fold>