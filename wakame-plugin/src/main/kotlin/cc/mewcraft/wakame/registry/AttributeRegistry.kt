package cc.mewcraft.wakame.registry

import cc.mewcraft.commons.provider.immutable.map
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.ReloadableProperty
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.attribute.ElementAttribute
import cc.mewcraft.wakame.attribute.facade.AttributeComponent
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.node
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.item.binary.cell.core.attribute.*
import cc.mewcraft.wakame.item.schema.cell.core.attribute.*
import cc.mewcraft.wakame.registry.AttributeRegistry.FACADES
import cc.mewcraft.wakame.util.*
import com.google.common.collect.ImmutableMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.nbt.ShadowTagType.*
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.spongepowered.configurate.ConfigurationNode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.UUID
import java.util.stream.Stream
import kotlin.reflect.KClass

//
// Caution! This is a big file. Navigate the file by the Structure view of your IDE.
//

/**
 * This singleton holds various implementations for **each** attribute.
 *
 * Check the [AttributeFacade] for implementation details.
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
    val FACADES: Registry<Key, AttributeFacade<BinaryAttributeCore, SchemaAttributeCore>> = SimpleRegistry()

    /**
     * The config of all attributes.
     */
    val CONFIG: ConfigProvider by lazy { Configs.YAML["attributes.yml"] }

    /**
     * Builds an attribute facade.
     *
     * @param key 词条在 NBT/模板 中的唯一标识。
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
        return FormatSelectionImpl(Key(Namespaces.ATTRIBUTE, key), type)
    }

    /**
     * Registers all attribute facades using DSL.
     */
    private fun registerFacades() {
        // Register special attribute
        +buildFacade("empty", BYTE).single().bind(Attributes.EMPTY)

        // Registry more attribute facade here ...
        +buildFacade("attack_damage", SHORT).ranged().element().bind(Attributes.byElement { MIN_ATTACK_DAMAGE }, Attributes.byElement { MAX_ATTACK_DAMAGE })
        +buildFacade("attack_effect_chance", DOUBLE).single().bind(Attributes.ATTACK_EFFECT_CHANCE)
        +buildFacade("attack_speed_level", BYTE).single().bind(Attributes.ATTACK_SPEED_LEVEL).override {
            // create closures
            val tooltips = DiscreteTooltips(config)
            // override it
            displayTextCreator = { core: BinaryAttributeCoreS ->
                val lines = tooltips.line(core.operation)
                val resolver = tooltips.value("value", core.value.toInt())
                listOf(AttributeRegistrySupport.mini().deserialize(lines, resolver))
            }
        }
        +buildFacade("block_interaction_range", DOUBLE).single().bind(Attributes.BLOCK_INTERACTION_RANGE)
        +buildFacade("critical_strike_chance", DOUBLE).single().bind(Attributes.CRITICAL_STRIKE_CHANCE)
        +buildFacade("critical_strike_power", DOUBLE).single().bind(Attributes.CRITICAL_STRIKE_POWER)
        +buildFacade("damage_reduction_rate", DOUBLE).single().bind(Attributes.DAMAGE_REDUCTION_RATE)
        +buildFacade("defense", SHORT).single().element().bind(Attributes.byElement { DEFENSE })
        +buildFacade("defense_penetration", SHORT).single().element().bind(Attributes.byElement { DEFENSE_PENETRATION })
        +buildFacade("defense_penetration_rate", DOUBLE).single().element().bind(Attributes.byElement { DEFENSE_PENETRATION_RATE })
        +buildFacade("entity_interaction_range", DOUBLE).single().bind(Attributes.ENTITY_INTERACTION_RANGE)
        +buildFacade("health_regeneration", SHORT).single().bind(Attributes.HEALTH_REGENERATION)
        +buildFacade("lifesteal", SHORT).single().bind(Attributes.LIFESTEAL)
        +buildFacade("lifesteal_rate", DOUBLE).single().bind(Attributes.LIFESTEAL_RATE)
        +buildFacade("mana_consumption_rate", DOUBLE).single().bind(Attributes.MANA_CONSUMPTION_RATE)
        +buildFacade("mana_regeneration", SHORT).single().bind(Attributes.MANA_REGENERATION)
        +buildFacade("manasteal", SHORT).single().bind(Attributes.MANASTEAL)
        +buildFacade("manasteal_rate", DOUBLE).single().bind(Attributes.MANASTEAL_RATE)
        +buildFacade("max_absorption", SHORT).single().bind(Attributes.MAX_ABSORPTION)
        +buildFacade("max_health", SHORT).single().bind(Attributes.MAX_HEALTH)
        +buildFacade("max_mana", SHORT).single().bind(Attributes.MAX_MANA)
        +buildFacade("movement_speed", DOUBLE).single().bind(Attributes.MOVEMENT_SPEED)
    }

    override fun onPreWorld() {
        registerFacades()
    }

    override fun onReload() {
        // TODO("Not yet implemented") // what to reload?
    }
}

/**
 * 包含了一个属性的各种操作和实现。
 *
 * 这里将各种操作和实现放在一起以方便外部统一调用。
 *
 * @param BC the type of [BinaryAttributeCore]
 * @param SC the type of [SchemaAttributeCore]
 */
interface AttributeFacade<BC : BinaryAttributeCore, SC : SchemaAttributeCore> : Keyed {
    /**
     * 属性 facade 的唯一标识。
     *
     * 属性 facade 的唯一标识与单个属性的唯一标识不一定相同。
     * 当一个属性的 facade 是由多个属性构成时（例如攻击力），
     * 它们的唯一标识就略微不同。
     */
    override val key: Key

    /**
     * Holds metadata about the attribute components.
     */
    val attributeComponentMetadata: AttributeComponentMetadata

    /**
     * A creator for attribute modifiers.
     */
    val attributeModifierCreator: (UUID, BC) -> Map<Attribute, AttributeModifier>

    /**
     * A creator for [SchemaAttributeCore].
     */
    val schemaCoreCreatorByConfig: (ConfigurationNode) -> SC

    /**
     * A creator for [BinaryAttributeCore].
     */
    val binaryCoreCreatorByConfig: (ConfigurationNode) -> BC

    /**
     * A creator for [BinaryAttributeCore].
     */
    val binaryCoreCreatorByTag: (CompoundShadowTag) -> BC

    /**
     * A creator for display text.
     */
    val displayTextCreator: (BC) -> List<Component>
}

/**
 * A mutable [AttributeFacade] (except the property [key]).
 */
private interface MutableAttributeFacade<BC : BinaryAttributeCore, SC : SchemaAttributeCore> : AttributeFacade<BC, SC> {
    /**
     * The config of the attribute facade.
     */
    val config: ConfigProvider

    // this should be immutable
    override val key: Key

    // these are mutable through override() in DSL
    override var attributeComponentMetadata: AttributeComponentMetadata
    override var attributeModifierCreator: (UUID, BC) -> Map<Attribute, AttributeModifier>
    override var schemaCoreCreatorByConfig: (ConfigurationNode) -> SC
    override var binaryCoreCreatorByConfig: (ConfigurationNode) -> BC
    override var binaryCoreCreatorByTag: (CompoundShadowTag) -> BC
    override var displayTextCreator: (BC) -> List<Component>
}

/**
 * 一个属性的组件相关信息。
 */
interface AttributeComponentMetadata {
    /**
     * 查询该属性是否有指定的组件。
     *
     * @param T 组件的类型
     * @param componentClass 组件的接口类
     * @return 如果该属性拥有该组件，则返回 `true`
     */
    fun <T : AttributeComponent> hasComponent(componentClass: KClass<T>): Boolean
}

/**
 * @see AttributeComponentMetadata.hasComponent
 */
inline fun <reified T : AttributeComponent> AttributeComponentMetadata.hasComponent(): Boolean {
    return hasComponent(T::class)
}

//
// Mini DSL for building an attribute facade
//

private operator fun AttributeFacade<*, *>.unaryPlus() {
    @Suppress("UNCHECKED_CAST")
    FACADES.register(this.key, this as AttributeFacade<BinaryAttributeCore, SchemaAttributeCore>)
}

private operator fun AttributeFacadeOverride<*, *>.unaryPlus() {
    @Suppress("UNCHECKED_CAST")
    FACADES.register(this.prototype.key, this as AttributeFacade<BinaryAttributeCore, SchemaAttributeCore>)
}

/**
 * 开始选择 `single` 或 `ranged`.
 */
private interface FormatSelection {
    fun single(): SingleSelection
    fun ranged(): RangedSelection
}

/**
 * 已选择 `single`，接下来直接 `bind`，或接着构造 `element`.
 */
private interface SingleSelection : SingleAttributeBinder {
    fun element(): SingleElementAttributeBinder
}

/**
 * 已选择 `ranged`，接下来直接 `bind`，或接着构造 `element`.
 */
private interface RangedSelection : RangedAttributeBinder {
    fun element(): RangedElementAttributeBinder
}

/**
 * 已选择 `single`，然后最终绑定到属性上。
 */
private interface SingleAttributeBinder {
    fun bind(
        component: Attribute,
    ): AttributeFacadeOverride<BinaryAttributeCoreS, SchemaAttributeCoreS>
}

/**
 * 已选择 `ranged`，然后最终绑定到属性上。
 */
private interface RangedAttributeBinder {
    fun bind(
        component1: Attribute,
        component2: Attribute,
    ): AttributeFacadeOverride<BinaryAttributeCoreR, SchemaAttributeCoreR>
}

/**
 * 已选择 `single` + `element`，然后最终绑定到属性上。
 */
private interface SingleElementAttributeBinder {
    fun bind(
        component: (Element) -> ElementAttribute,
    ): AttributeFacadeOverride<BinaryAttributeCoreSE, SchemaAttributeCoreSE>
}

/**
 * 已选择 `ranged` + `element`，然后最终绑定到属性上。
 */
private interface RangedElementAttributeBinder {
    fun bind(
        component1: (Element) -> ElementAttribute,
        component2: (Element) -> ElementAttribute,
    ): AttributeFacadeOverride<BinaryAttributeCoreRE, SchemaAttributeCoreRE>
}

private interface AttributeFacadeOverride<BC : BinaryAttributeCore, SC : SchemaAttributeCore> {
    val prototype: AttributeFacade<BC, SC>
    fun override(mutator: MutableAttributeFacade<BC, SC>.() -> Unit): AttributeFacade<BC, SC>
}

//
// Implementations start from here
//

//<editor-fold desc="Implementations">
private object AttributeRegistrySupport : KoinComponent {
    private val MINI: MiniMessage by inject()

    fun mini(): MiniMessage {
        return MINI
    }
}

private class MutableAttributeFacadeImpl<A : BinaryAttributeCore, B : SchemaAttributeCore>(
    override val key: Key,
    override val config: ConfigProvider,
    override var attributeModifierCreator: (UUID, A) -> Map<Attribute, AttributeModifier>,
    override var attributeComponentMetadata: AttributeComponentMetadata,
    override var schemaCoreCreatorByConfig: (ConfigurationNode) -> B,
    override var binaryCoreCreatorByConfig: (ConfigurationNode) -> A,
    override var binaryCoreCreatorByTag: (CompoundShadowTag) -> A,
    override var displayTextCreator: (A) -> List<Component>,
) : MutableAttributeFacade<A, B>

private class AttributeComponentMetadataImpl private constructor(
    components: Set<KClass<out AttributeComponent>>,
) : AttributeComponentMetadata {
    constructor(vararg components: KClass<out AttributeComponent>) : this(components.toHashSet())

    private val components: Set<KClass<out AttributeComponent>> = components.toHashSet()

    override fun <T : AttributeComponent> hasComponent(componentClass: KClass<T>): Boolean {
        return componentClass in components
    }
}

private sealed class Tooltips(
    config: ConfigProvider,
) : Examinable {
    // These are raw string values from the config
    private val add: String by config.entry("tooltips", "add")
    private val multiplyBase: String by config.entry("tooltips", "multiply_base")
    private val multiplyTotal: String by config.entry("tooltips", "multiply_total")

    /**
     * Gets a line by specific [Operation].
     */
    fun line(operation: Operation): String {
        return when (operation) {
            Operation.ADD -> add
            Operation.MULTIPLY_BASE -> multiplyBase
            Operation.MULTIPLY_TOTAL -> multiplyTotal
        }
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("add", add),
            ExaminableProperty.of("multiplyBase", multiplyBase),
            ExaminableProperty.of("multiplyTotal", multiplyTotal)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * Encapsulation of the tooltips for an attribute facade.
 *
 * @param config the config of the attribute facade
 */
private class NumericTooltips(
    config: ConfigProvider,
) : Tooltips(config), Examinable {
    /**
     * This companion object is an object pool of [DecimalFormat].
     */
    companion object Cache {
        /**
         * The default [DecimalFormat] to be used if no one is provided.
         */
        val DEFAULT_NUMBER_FORMAT: NumberFormat = DecimalFormat.getInstance()

        /**
         * The cache of [DecimalFormat]. The `map key` are patterns for
         * [DecimalFormat].
         */
        val CUSTOM_NUMBER_FORMAT: MutableMap<String, NumberFormat> by ReloadableProperty {
            Object2ObjectOpenHashMap<String, NumberFormat>(8).withDefault { DecimalFormat(it) }
        }
    }

    /**
     * Creates a [TagResolver] for the tooltips.
     */
    fun number(key: String, value: Number): TagResolver {
        return TagResolver.resolver(key) { args, context ->
            val decimalFormat = if (args.hasNext()) {
                val pattern = args.pop().value()
                CUSTOM_NUMBER_FORMAT.getValue(pattern)
            } else {
                DEFAULT_NUMBER_FORMAT
            }
            Tag.inserting(context.deserialize(decimalFormat.format(value)))
        }
    }

    /**
     * Creates a [TagResolver] for the tooltips.
     */
    fun component(key: String, value: Component): TagResolver {
        return Placeholder.component(key, value)
    }
}

/**
 * Encapsulation of the tooltips for an attribute facade.
 *
 * Unlike [NumericTooltips], this is for the attributes with discrete values.
 *
 * @param config the config of the attribute facade
 */
private class DiscreteTooltips(
    config: ConfigProvider,
) : Tooltips(config), Examinable {
    val mappings: Map<Int, Component> by config
        .entry<Map<Int, String>>("mappings")
        .map { map ->
            map.withDefault { int ->
                "??? ($int)"
            }.mapValues { (_, v) ->
                AttributeRegistrySupport.mini().deserialize(v)
            }
        }

    /**
     * Creates a [TagResolver] for the tooltips.
     */
    fun value(key: String, value: Int): TagResolver {
        return Placeholder.component(key, mappings.getValue(value))
    }
}

private class FormatSelectionImpl(
    private val id: Key,
    private val tagType: ShadowTagType,
) : FormatSelection {
    override fun single(): SingleSelection {
        return SingleSelectionImpl(id, tagType)
    }

    override fun ranged(): RangedSelection {
        return RangedSelectionImpl(id, tagType)
    }
}

private class SingleSelectionImpl(
    private val id: Key,
    private val tagType: ShadowTagType,
) : SingleSelection {
    private val config: ConfigProvider = AttributeRegistry.CONFIG.node(id.value())
    private val tooltips: NumericTooltips = NumericTooltips(config)

    override fun element(): SingleElementAttributeBinder {
        return SingleElementAttributeBinderImpl(id, tagType)
    }

    /**
     * Components: Operation, Single
     */
    override fun bind(
        component: Attribute,
    ): AttributeFacadeOverride<BinaryAttributeCoreS, SchemaAttributeCoreS> {
        val facade = MutableAttributeFacadeImpl(
            key = id,
            config = config,
            attributeModifierCreator = { uuid: UUID, core: BinaryAttributeCoreS ->
                ImmutableMap.of(component, AttributeModifier(uuid, core.value.toStableDouble(), core.operation))
            },
            attributeComponentMetadata = AttributeComponentMetadataImpl(
                AttributeComponent.Op::class, AttributeComponent.Single::class
            ),
            schemaCoreCreatorByConfig = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val value = node.getSchemaSingle()
                SchemaAttributeCoreS(id, tagType, operation, value)
            },
            binaryCoreCreatorByConfig = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val value = node.getBinarySingle()
                BinaryAttributeCoreDataHolderS(id, tagType, operation, value)
            },
            binaryCoreCreatorByTag = { tag: CompoundShadowTag ->
                BinaryAttributeCoreTagWrapperS(tag)
            },
            displayTextCreator = { core: BinaryAttributeCoreS ->
                val lines = tooltips.line(core.operation)
                val resolver = tooltips.number("value", core.value)
                listOf(AttributeRegistrySupport.mini().deserialize(lines, resolver))
            },
        )

        val override = AttributeFacadeOverrideImpl(facade)

        return override
    }
}

private class RangedSelectionImpl(
    private val id: Key,
    private val tagType: ShadowTagType,
) : RangedSelection {
    private val config: ConfigProvider = AttributeRegistry.CONFIG.node(id.value())
    private val tooltips: NumericTooltips = NumericTooltips(config)

    override fun element(): RangedElementAttributeBinder {
        return RangedElementAttributeBinderImpl(id, tagType)
    }

    /**
     * Components: Operation, Ranged
     */
    override fun bind(
        component1: Attribute,
        component2: Attribute,
    ): AttributeFacadeOverride<BinaryAttributeCoreR, SchemaAttributeCoreR> {
        val facade = MutableAttributeFacadeImpl(
            key = id,
            config = config,
            attributeModifierCreator = { uuid: UUID, core: BinaryAttributeCoreR ->
                ImmutableMap.of(
                    component1, AttributeModifier(uuid, core.lower.toStableDouble(), core.operation),
                    component2, AttributeModifier(uuid, core.upper.toStableDouble(), core.operation),
                )
            },
            attributeComponentMetadata = AttributeComponentMetadataImpl(
                AttributeComponent.Op::class, AttributeComponent.Ranged::class
            ),
            schemaCoreCreatorByConfig = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val lower = node.getSchemaLower()
                val upper = node.getSchemaUpper()
                SchemaAttributeCoreR(id, tagType, operation, lower, upper)
            },
            binaryCoreCreatorByConfig = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val lower = node.getBinaryLower()
                val upper = node.getBinaryUpper()
                BinaryAttributeCoreDataHolderR(id, tagType, operation, lower, upper)
            },
            binaryCoreCreatorByTag = { tag: CompoundShadowTag ->
                BinaryAttributeCoreTagWrapperR(tag)
            },
            displayTextCreator = { core: BinaryAttributeCoreR ->
                val lines = tooltips.line(core.operation)
                val resolver1 = tooltips.number("min", core.lower)
                val resolver2 = tooltips.number("max", core.upper)
                listOf(AttributeRegistrySupport.mini().deserialize(lines, resolver1, resolver2))
            },
        )

        val override = AttributeFacadeOverrideImpl(facade)

        return override
    }
}

private class SingleElementAttributeBinderImpl(
    private val id: Key,
    private val tagType: ShadowTagType,
) : SingleElementAttributeBinder {
    private val config: ConfigProvider = AttributeRegistry.CONFIG.node(id.value())
    private val tooltips: NumericTooltips = NumericTooltips(config)

    /**
     * Components: Operation, Single, Element
     */
    override fun bind(
        component: (Element) -> ElementAttribute,
    ): AttributeFacadeOverride<BinaryAttributeCoreSE, SchemaAttributeCoreSE> {
        val facade = MutableAttributeFacadeImpl(
            key = id,
            config = config,
            attributeModifierCreator = { uuid: UUID, core: BinaryAttributeCoreSE ->
                ImmutableMap.of(component(core.element), AttributeModifier(uuid, core.value.toStableDouble(), core.operation))
            },
            attributeComponentMetadata = AttributeComponentMetadataImpl(
                AttributeComponent.Op::class, AttributeComponent.Single::class, AttributeComponent.Element::class
            ),
            schemaCoreCreatorByConfig = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val value = node.getSchemaSingle()
                val element = node.getElement()
                SchemaAttributeCoreSE(id, tagType, operation, value, element)
            },
            binaryCoreCreatorByConfig = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val value = node.getBinarySingle()
                val element = node.getElement()
                BinaryAttributeCoreDataHolderSE(id, tagType, operation, value, element)
            },
            binaryCoreCreatorByTag = { tag: CompoundShadowTag ->
                BinaryAttributeCoreTagWrapperSE(tag)
            },
            displayTextCreator = { core: BinaryAttributeCoreSE ->
                val lines = tooltips.line(core.operation)
                val resolver1 = tooltips.number("value", core.value)
                val resolver2 = tooltips.component("element", core.element.displayName)
                listOf(AttributeRegistrySupport.mini().deserialize(lines, resolver1, resolver2))
            },
        )

        val override = AttributeFacadeOverrideImpl(facade)

        return override
    }
}

private class RangedElementAttributeBinderImpl(
    private val id: Key,
    private val tagType: ShadowTagType,
) : RangedElementAttributeBinder {
    private val config: ConfigProvider = AttributeRegistry.CONFIG.node(id.value())
    private val tooltips: NumericTooltips = NumericTooltips(config)

    /**
     * Components: Operation, Ranged, Element
     */
    override fun bind(
        component1: (Element) -> ElementAttribute,
        component2: (Element) -> ElementAttribute,
    ): AttributeFacadeOverride<BinaryAttributeCoreRE, SchemaAttributeCoreRE> {
        val facade = MutableAttributeFacadeImpl(
            key = id,
            config = config,
            attributeModifierCreator = { uuid: UUID, core: BinaryAttributeCoreRE ->
                ImmutableMap.of(
                    component1(core.element), AttributeModifier(uuid, core.lower.toStableDouble(), core.operation),
                    component2(core.element), AttributeModifier(uuid, core.upper.toStableDouble(), core.operation),
                )
            },
            attributeComponentMetadata = AttributeComponentMetadataImpl(
                AttributeComponent.Op::class, AttributeComponent.Ranged::class, AttributeComponent.Element::class
            ),
            schemaCoreCreatorByConfig = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val lower = node.getSchemaLower()
                val upper = node.getSchemaUpper()
                val element = node.getElement()
                SchemaAttributeCoreRE(id, tagType, operation, lower, upper, element)
            },
            binaryCoreCreatorByConfig = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val lower = node.getBinaryLower()
                val upper = node.getBinaryUpper()
                val element = node.getElement()
                BinaryAttributeCoreDataHolderRE(id, tagType, operation, lower, upper, element)
            },
            binaryCoreCreatorByTag = { tag: CompoundShadowTag ->
                BinaryAttributeCoreTagWrapperRE(tag)
            },
            displayTextCreator = { core: BinaryAttributeCoreRE ->
                val lines = tooltips.line(core.operation)
                val resolver1 = tooltips.number("min", core.lower)
                val resolver2 = tooltips.number("max", core.upper)
                val resolver3 = tooltips.component("element", core.element.displayName)
                listOf(AttributeRegistrySupport.mini().deserialize(lines, resolver1, resolver2, resolver3))
            },
        )

        val override = AttributeFacadeOverrideImpl(facade)

        return override
    }
}

private class AttributeFacadeOverrideImpl<BC : BinaryAttributeCore, SC : SchemaAttributeCore>(
    override val prototype: MutableAttributeFacade<BC, SC>,
) : AttributeFacadeOverride<BC, SC> {
    override fun override(mutator: MutableAttributeFacade<BC, SC>.() -> Unit): AttributeFacade<BC, SC> {
        return prototype.apply(mutator)
    }
}

/* Specialized Configuration Operations */

private fun ConfigurationNode.getBinarySingle(): Double {
    return node("value").krequire<Double>()
}

private fun ConfigurationNode.getBinaryLower(): Double {
    return node("lower").krequire<Double>()
}

private fun ConfigurationNode.getBinaryUpper(): Double {
    return node("upper").krequire<Double>()
}

private fun ConfigurationNode.getSchemaSingle(): RandomizedValue {
    return node("value").krequire<RandomizedValue>()
}

private fun ConfigurationNode.getSchemaLower(): RandomizedValue {
    return node("lower").krequire<RandomizedValue>()
}

private fun ConfigurationNode.getSchemaUpper(): RandomizedValue {
    return node("upper").krequire<RandomizedValue>()
}

private fun ConfigurationNode.getElement(): Element {
    return node("element").krequire<Element>()
}

private fun ConfigurationNode.getOperation(): Operation {
    return node("operation").string?.let { Operation.byKey(it) } ?: Operation.ADD
}
//</editor-fold>
