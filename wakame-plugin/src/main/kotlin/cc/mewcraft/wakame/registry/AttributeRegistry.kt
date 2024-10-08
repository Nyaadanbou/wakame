package cc.mewcraft.wakame.registry

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.ReloadableProperty
import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.attribute.*
import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.composite.*
import cc.mewcraft.wakame.config.*
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.initializer.*
import cc.mewcraft.wakame.registry.AttributeRegistry.FACADES
import cc.mewcraft.wakame.util.*
import com.google.common.collect.ImmutableMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
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
import java.util.stream.Stream
import kotlin.reflect.KClass

//
// Caution! This is a big file. Navigate the file by the Structure view of your IDE.
//

/**
 * This singleton holds various implementations for **each** attribute.
 *
 * Check the [CompositeAttributeFacade] for more details.
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

    /**
     * The facades of all attributes.
     */
    val FACADES: Registry<String, CompositeAttributeFacade<ConstantCompositeAttribute, VariableCompositeAttribute>> = SimpleRegistry()

    /**
     * The config of all attributes.
     */
    val CONFIG: ConfigProvider by lazy { Configs.YAML[ATTRIBUTE_GLOBAL_CONFIG_FILE] }

    /**
     * Builds an attribute facade.
     *
     * 注意, 参数 [id] 仅仅是词条在 NBT/模板 中的唯一标识.
     * 底层由多个对象组成的词条标识就与这里的 [id] 不同.
     *
     * 例如攻击力这个属性词条, 底层实际上是由两个属性组成的, 分别是 `MIN_ATTACK_DAMAGE` 和
     * `MAX_ATTACK_DAMAGE`, 但攻击力属性词条在 NBT/模板中的标识是一个经过“合并”得到的
     * `attribute:attack_damage`.
     *
     * @param id 词条在 NBT/模板 中的唯一标识
     */
    private fun buildFacade(id: String): FormatSelection {
        return FormatSelectionImpl(id)
    }

    /**
     * Registers all attribute facades using DSL.
     */
    private fun registerFacades() {
        // Register special attribute
        +buildFacade("empty").single().bind { EMPTY }
        // Registry more attribute facades here ...
        +buildFacade("attack_damage").ranged().element().bind({ MIN_ATTACK_DAMAGE }, { MAX_ATTACK_DAMAGE })
        +buildFacade("attack_damage_rate").single().element().bind { ATTACK_DAMAGE_RATE }
        +buildFacade("attack_effect_chance").single().bind { ATTACK_EFFECT_CHANCE }
        +buildFacade("block_interaction_range").single().bind { BLOCK_INTERACTION_RANGE }
        +buildFacade("critical_strike_chance").single().bind { CRITICAL_STRIKE_CHANCE }
        +buildFacade("critical_strike_power").single().bind { CRITICAL_STRIKE_POWER }
        +buildFacade("negative_critical_strike_power").single().bind { NEGATIVE_CRITICAL_STRIKE_POWER }
        +buildFacade("defense").single().element().bind { DEFENSE }
        +buildFacade("defense_penetration").single().element().bind { DEFENSE_PENETRATION }
        +buildFacade("defense_penetration_rate").single().element().bind { DEFENSE_PENETRATION_RATE }
        +buildFacade("entity_interaction_range").single().bind { ENTITY_INTERACTION_RANGE }
        +buildFacade("health_regeneration").single().bind { HEALTH_REGENERATION }.override {
            // create closures
            val tooltips = NumericTooltips(config)
            // override it
            createTooltipLore = { core: ConstantCompositeAttributeS ->
                val lines = tooltips.line(core.operation)
                if (core.operation == Operation.ADD) {
                    val resolver = tooltips.number("value", core.value * 20)
                    listOf(AttributeRegistrySupport.miniMessage.deserialize(lines, resolver))
                } else {
                    val resolver = tooltips.number("value", core.value)
                    listOf(AttributeRegistrySupport.miniMessage.deserialize(lines, resolver))
                }
            }
        }
        +buildFacade("incoming_damage_rate").single().element().bind { INCOMING_DAMAGE_RATE }
        +buildFacade("lifesteal").single().bind { LIFESTEAL }
        +buildFacade("mana_consumption_rate").single().bind { MANA_CONSUMPTION_RATE }
        +buildFacade("mana_regeneration").single().bind { MANA_REGENERATION }.override {
            // create closures
            val tooltips = NumericTooltips(config)
            // override it
            createTooltipLore = { core: ConstantCompositeAttributeS ->
                val lines = tooltips.line(core.operation)
                if (core.operation == Operation.ADD) {
                    val resolver = tooltips.number("value", core.value * 20)
                    listOf(AttributeRegistrySupport.miniMessage.deserialize(lines, resolver))
                } else {
                    val resolver = tooltips.number("value", core.value)
                    listOf(AttributeRegistrySupport.miniMessage.deserialize(lines, resolver))
                }
            }
        }
        +buildFacade("manasteal").single().bind { MANASTEAL }
        +buildFacade("max_absorption").single().bind { MAX_ABSORPTION }
        +buildFacade("max_health").single().bind { MAX_HEALTH }
        +buildFacade("max_mana").single().bind { MAX_MANA }
        +buildFacade("movement_speed").single().bind { MOVEMENT_SPEED }
        +buildFacade("universal_attack_damage").ranged().bind({ UNIVERSAL_MIN_ATTACK_DAMAGE }, { UNIVERSAL_MAX_ATTACK_DAMAGE })
        +buildFacade("universal_defense").single().bind { UNIVERSAL_DEFENSE }
        +buildFacade("universal_defense_penetration").single().bind { UNIVERSAL_DEFENSE_PENETRATION }
        +buildFacade("universal_defense_penetration_rate").single().bind { UNIVERSAL_DEFENSE_PENETRATION_RATE }
    }

    override fun onPreWorld() {
        // 注册所有 facade
        registerFacades()
        // 初始化
        ElementRegistry.INSTANCES.forEach { (_, element) ->
            Attributes.element(element)
        }
    }
}

/**
 * 包含了一个 [cc.mewcraft.wakame.attribute.composite.CompositeAttribute] 所相关的各种字段和操作.
 *
 * @param T [ConstantCompositeAttribute] 的一个子类
 * @param S [VariableCompositeAttribute] 的一个子类
 */
interface CompositeAttributeFacade<T : ConstantCompositeAttribute, S : VariableCompositeAttribute> : Keyed {
    /**
     * 本实例的全局配置文件.
     */
    val config: ConfigProvider

    /**
     * 属性的唯一标识.
     *
     * 属性 facade 的唯一标识与单个属性的唯一标识不一定相同,
     * 当一个属性的 facade 是由多个属性构成时(例如攻击力),
     * 它们的唯一标识就不一样.
     */
    val id: String

    /**
     * Holds metadata about the attribute components.
     */
    val components: CompositeAttributeMetadata

    /**
     * A creator for attribute modifiers.
     */
    val createAttributeModifiers: (Key, T) -> Map<Attribute, AttributeModifier>

    /**
     * A creator for [cc.mewcraft.wakame.item.templates.components.cells.cores.AttributeCoreBlueprint].
     */
    val convertNode2Variable: (ConfigurationNode) -> S

    /**
     * A creator for [cc.mewcraft.wakame.item.components.cells.AttributeCore].
     */
    val convertNode2Constant: (ConfigurationNode) -> T

    /**
     * A creator for [cc.mewcraft.wakame.item.components.cells.AttributeCore].
     */
    val convertNBT2Constant: (CompoundTag) -> T

    /**
     * A creator for tooltip name.
     */
    val createTooltipName: (T) -> Component

    /**
     * A creator for tooltip lore.
     */
    val createTooltipLore: (T) -> List<Component>
}

/**
 * 一个属性的组件相关信息。
 */
interface CompositeAttributeMetadata {
    /**
     * 查询该属性是否有指定的组件。
     *
     * @param T 组件的类型
     * @param componentClass 组件的接口类
     * @return 如果该属性拥有该组件，则返回 `true`
     */
    fun <T : CompositeAttributeComponent> hasComponent(componentClass: KClass<T>): Boolean
}

/**
 * @see CompositeAttributeMetadata.hasComponent
 */
inline fun <reified T : CompositeAttributeComponent> CompositeAttributeMetadata.hasComponent(): Boolean {
    return hasComponent(T::class)
}

//
// Mini DSL for building an attribute facade
//

private operator fun CompositeAttributeFacade<*, *>.unaryPlus() {
    @Suppress("UNCHECKED_CAST")
    FACADES.register(this.id, this as CompositeAttributeFacade<ConstantCompositeAttribute, VariableCompositeAttribute>)
}

private operator fun AttributeFacadeOverride<*, *>.unaryPlus() {
    @Suppress("UNCHECKED_CAST")
    FACADES.register(this.prototype.id, this.prototype as CompositeAttributeFacade<ConstantCompositeAttribute, VariableCompositeAttribute>)
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
        component: Attributes.() -> Attribute,
    ): AttributeFacadeOverride<ConstantCompositeAttributeS, VariableCompositeAttributeS>
}

/**
 * 已选择 `ranged`，然后最终绑定到属性上。
 */
private interface RangedAttributeBinder {
    fun bind(
        component1: Attributes.() -> Attribute,
        component2: Attributes.() -> Attribute,
    ): AttributeFacadeOverride<ConstantCompositeAttributeR, VariableCompositeAttributeR>
}

/**
 * 已选择 `single` + `element`，然后最终绑定到属性上。
 */
private interface SingleElementAttributeBinder {
    fun bind(
        component: ElementAttributes.() -> ElementAttribute,
    ): AttributeFacadeOverride<ConstantCompositeAttributeSE, VariableCompositeAttributeSE>
}

/**
 * 已选择 `ranged` + `element`，然后最终绑定到属性上。
 */
private interface RangedElementAttributeBinder {
    fun bind(
        component1: ElementAttributes.() -> ElementAttribute,
        component2: ElementAttributes.() -> ElementAttribute,
    ): AttributeFacadeOverride<ConstantCompositeAttributeRE, VariableCompositeAttributeRE>
}

private class AttributeFacadeOverride<S : ConstantCompositeAttribute, V : VariableCompositeAttribute>(
    val prototype: MutableCompositeAttributeFacade<S, V>,
) {
    fun override(mutator: MutableCompositeAttributeFacade<S, V>.() -> Unit): CompositeAttributeFacade<S, V> {
        return prototype.apply(mutator)
    }
}

//
// Implementations start from here
//

//<editor-fold desc="Implementations">
private object AttributeRegistrySupport : KoinComponent {
    val miniMessage: MiniMessage by inject()
}

/**
 * A mutable [CompositeAttributeFacade] (except the property [id]).
 */
private class MutableCompositeAttributeFacade<T : ConstantCompositeAttribute, S : VariableCompositeAttribute>(
    // the config provider of this facade
    override val config: ConfigProvider,

    // this should be immutable
    override val id: String,

    // these are mutable through override() in DSL
    override var components: CompositeAttributeMetadata,
    override var createAttributeModifiers: (Key, T) -> Map<Attribute, AttributeModifier>,
    override var convertNode2Variable: (ConfigurationNode) -> S,
    override var convertNode2Constant: (ConfigurationNode) -> T,
    override var convertNBT2Constant: (CompoundTag) -> T,
    override var createTooltipName: (T) -> Component,
    override var createTooltipLore: (T) -> List<Component>,
) : CompositeAttributeFacade<T, S> {
    override val key: Key = Key.key(Namespaces.ATTRIBUTE, id)
}

private class CompositeAttributeMetadataImpl private constructor(
    components: Set<KClass<out CompositeAttributeComponent>>,
) : CompositeAttributeMetadata {
    constructor(vararg components: KClass<out CompositeAttributeComponent>) : this(components.toHashSet())

    private val components: Set<KClass<out CompositeAttributeComponent>> = components.toHashSet()

    override fun <T : CompositeAttributeComponent> hasComponent(componentClass: KClass<T>): Boolean {
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

private class FormatSelectionImpl(
    private val id: String,
) : FormatSelection {
    override fun single(): SingleSelection {
        return SingleSelectionImpl(id)
    }

    override fun ranged(): RangedSelection {
        return RangedSelectionImpl(id)
    }
}

private class SingleSelectionImpl(
    private val id: String,
) : SingleSelection {
    private val config: ConfigProvider = AttributeRegistry.CONFIG.derive(this@SingleSelectionImpl.id)
    private val displayName: String by config.entry<String>("display_name")
    private val tooltips: NumericTooltips = NumericTooltips(config)

    override fun element(): SingleElementAttributeBinder {
        return SingleElementAttributeBinderImpl(this@SingleSelectionImpl.id)
    }

    /**
     * Components: Operation, Single
     */
    override fun bind(component: Attributes.() -> Attribute): AttributeFacadeOverride<ConstantCompositeAttributeS, VariableCompositeAttributeS> {
        val facade = MutableCompositeAttributeFacade(
            config = config,
            id = this@SingleSelectionImpl.id,
            components = CompositeAttributeMetadataImpl(
                CompositeAttributeComponent.Operation::class, CompositeAttributeComponent.Scalar::class
            ),
            createAttributeModifiers = { id: Key, core: ConstantCompositeAttributeS ->
                ImmutableMap.of(
                    Attributes.component(), AttributeModifier(id, core.value.toStableDouble(), core.operation)
                )
            },
            convertNode2Variable = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val value = node.getVariableScalar()
                VariableCompositeAttributeS(id, operation, value)
            },
            convertNode2Constant = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val value = node.getSimpleScalar()
                ConstantCompositeAttributeS(id, operation, value)
            },
            convertNBT2Constant = { tag: CompoundTag ->
                ConstantCompositeAttributeS(id, tag)
            },
            createTooltipName = {
                AttributeRegistrySupport.miniMessage.deserialize(displayName)
            },
            createTooltipLore = { core: ConstantCompositeAttributeS ->
                val lines = tooltips.line(core.operation)
                val resolver = tooltips.number("value", core.value)
                listOf(AttributeRegistrySupport.miniMessage.deserialize(lines, resolver))
            },
        )

        return AttributeFacadeOverride(facade)
    }
}

private class RangedSelectionImpl(
    private val id: String,
) : RangedSelection {
    private val config: ConfigProvider = AttributeRegistry.CONFIG.derive(id)
    private val displayName: String by config.entry<String>("display_name")
    private val tooltips: NumericTooltips = NumericTooltips(config)

    override fun element(): RangedElementAttributeBinder {
        return RangedElementAttributeBinderImpl(id)
    }

    /**
     * Components: Operation, Ranged
     */
    override fun bind(
        component1: Attributes.() -> Attribute,
        component2: Attributes.() -> Attribute,
    ): AttributeFacadeOverride<ConstantCompositeAttributeR, VariableCompositeAttributeR> {
        val facade = MutableCompositeAttributeFacade(
            config = config,
            id = id,
            components = CompositeAttributeMetadataImpl(
                CompositeAttributeComponent.Operation::class, CompositeAttributeComponent.Ranged::class
            ),
            createAttributeModifiers = { id: Key, core: ConstantCompositeAttributeR ->
                ImmutableMap.of(
                    Attributes.component1(), AttributeModifier(id, core.lower.toStableDouble(), core.operation),
                    Attributes.component2(), AttributeModifier(id, core.upper.toStableDouble(), core.operation),
                )
            },
            convertNode2Variable = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val lower = node.getVariableMin()
                val upper = node.getVariableMax()
                VariableCompositeAttributeR(id, operation, lower, upper)
            },
            convertNode2Constant = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val lower = node.getSimpleMin()
                val upper = node.getSimpleMax()
                ConstantCompositeAttributeR(id, operation, lower, upper)
            },
            convertNBT2Constant = { tag: CompoundTag ->
                ConstantCompositeAttributeR(id, tag)
            },
            createTooltipName = {
                AttributeRegistrySupport.miniMessage.deserialize(displayName)
            },
            createTooltipLore = { core: ConstantCompositeAttributeR ->
                val lines = tooltips.line(core.operation)
                val resolver1 = tooltips.number("min", core.lower)
                val resolver2 = tooltips.number("max", core.upper)
                listOf(AttributeRegistrySupport.miniMessage.deserialize(lines, resolver1, resolver2))
            },
        )

        return AttributeFacadeOverride(facade)
    }
}

private class SingleElementAttributeBinderImpl(
    private val id: String,
) : SingleElementAttributeBinder {
    private val config: ConfigProvider = AttributeRegistry.CONFIG.derive(id)
    private val displayName: String by config.entry<String>("display_name")
    private val tooltips: NumericTooltips = NumericTooltips(config)

    /**
     * Components: Operation, Single, Element
     */
    override fun bind(component: ElementAttributes.() -> ElementAttribute): AttributeFacadeOverride<ConstantCompositeAttributeSE, VariableCompositeAttributeSE> {
        val facade = MutableCompositeAttributeFacade(
            config = config,
            id = id,
            components = CompositeAttributeMetadataImpl(
                CompositeAttributeComponent.Operation::class, CompositeAttributeComponent.Scalar::class, CompositeAttributeComponent.Element::class
            ),
            createAttributeModifiers = { id: Key, core: ConstantCompositeAttributeSE ->
                ImmutableMap.of(
                    Attributes.element(core.element).component(), AttributeModifier(id, core.value.toStableDouble(), core.operation)
                )
            },
            convertNode2Variable = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val value = node.getVariableScalar()
                val element = node.getElement()
                VariableCompositeAttributeSE(id, operation, value, element)
            },
            convertNode2Constant = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val value = node.getSimpleScalar()
                val element = node.getElement()
                ConstantCompositeAttributeSE(id, operation, value, element)
            },
            convertNBT2Constant = { tag: CompoundTag ->
                ConstantCompositeAttributeSE(id, tag)
            },
            createTooltipName = {
                val resolver = Placeholder.component("element", it.element.displayName)
                AttributeRegistrySupport.miniMessage.deserialize(displayName, resolver)
            },
            createTooltipLore = { core: ConstantCompositeAttributeSE ->
                val lines = tooltips.line(core.operation)
                val resolver1 = tooltips.number("value", core.value)
                val resolver2 = tooltips.component("element", core.element.displayName)
                listOf(AttributeRegistrySupport.miniMessage.deserialize(lines, resolver1, resolver2))
            },
        )

        return AttributeFacadeOverride(facade)
    }
}

private class RangedElementAttributeBinderImpl(
    private val id: String,
) : RangedElementAttributeBinder {
    private val config: ConfigProvider = AttributeRegistry.CONFIG.derive(id)
    private val displayName: String by config.entry<String>("display_name")
    private val tooltips: NumericTooltips = NumericTooltips(config)

    /**
     * Components: Operation, Ranged, Element
     */
    override fun bind(
        component1: ElementAttributes.() -> ElementAttribute,
        component2: ElementAttributes.() -> ElementAttribute,
    ): AttributeFacadeOverride<ConstantCompositeAttributeRE, VariableCompositeAttributeRE> {
        val facade = MutableCompositeAttributeFacade(
            config = config,
            id = id,
            components = CompositeAttributeMetadataImpl(
                CompositeAttributeComponent.Operation::class, CompositeAttributeComponent.Ranged::class, CompositeAttributeComponent.Element::class
            ),
            createAttributeModifiers = { id: Key, core: ConstantCompositeAttributeRE ->
                ImmutableMap.of(
                    Attributes.element(core.element).component1(), AttributeModifier(id, core.lower.toStableDouble(), core.operation),
                    Attributes.element(core.element).component2(), AttributeModifier(id, core.upper.toStableDouble(), core.operation),
                )
            },
            convertNode2Variable = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val lower = node.getVariableMin()
                val upper = node.getVariableMax()
                val element = node.getElement()
                VariableCompositeAttributeRE(id, operation, lower, upper, element)
            },
            convertNode2Constant = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val lower = node.getSimpleMin()
                val upper = node.getSimpleMax()
                val element = node.getElement()
                ConstantCompositeAttributeRE(id, operation, lower, upper, element)
            },
            convertNBT2Constant = { tag: CompoundTag ->
                ConstantCompositeAttributeRE(id, tag)
            },
            createTooltipName = {
                val resolver = Placeholder.component("element", it.element.displayName)
                AttributeRegistrySupport.miniMessage.deserialize(displayName, resolver)
            },
            createTooltipLore = { core: ConstantCompositeAttributeRE ->
                val lines = tooltips.line(core.operation)
                val resolver1 = tooltips.number("min", core.lower)
                val resolver2 = tooltips.number("max", core.upper)
                val resolver3 = tooltips.component("element", core.element.displayName)
                listOf(AttributeRegistrySupport.miniMessage.deserialize(lines, resolver1, resolver2, resolver3))
            },
        )

        return AttributeFacadeOverride(facade)
    }
}

/* Specialized Configuration Operations */


private fun ConfigurationNode.getOperation(): Operation {
    return node("operation").string?.let { Operation.byKeyOrThrow(it) } ?: Operation.ADD
}

private fun ConfigurationNode.getElement(): Element {
    return node("element").krequire<Element>()
}

private fun ConfigurationNode.getSimpleScalar(): Double {
    return node("value").krequire<Double>()
}

private fun ConfigurationNode.getSimpleMin(): Double {
    return node("lower").krequire<Double>()
}

private fun ConfigurationNode.getSimpleMax(): Double {
    return node("upper").krequire<Double>()
}

private fun ConfigurationNode.getVariableScalar(): RandomizedValue {
    return node("value").krequire<RandomizedValue>()
}

private fun ConfigurationNode.getVariableMin(): RandomizedValue {
    return node("lower").krequire<RandomizedValue>()
}

private fun ConfigurationNode.getVariableMax(): RandomizedValue {
    return node("upper").krequire<RandomizedValue>()
}
//</editor-fold>
