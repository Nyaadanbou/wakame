package cc.mewcraft.wakame.registry

import cc.mewcraft.commons.provider.immutable.map
import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.ReloadableProperty
import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeComponent
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.attribute.ElementAttribute
import cc.mewcraft.wakame.attribute.ElementAttributes
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.derive
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttribute
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttributeR
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttributeRE
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttributeS
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttributeSE
import cc.mewcraft.wakame.item.components.cells.template.cores.attribute.TemplateCoreAttribute
import cc.mewcraft.wakame.item.components.cells.template.cores.attribute.TemplateCoreAttributeR
import cc.mewcraft.wakame.item.components.cells.template.cores.attribute.TemplateCoreAttributeRE
import cc.mewcraft.wakame.item.components.cells.template.cores.attribute.TemplateCoreAttributeS
import cc.mewcraft.wakame.item.components.cells.template.cores.attribute.TemplateCoreAttributeSE
import cc.mewcraft.wakame.registry.AttributeRegistry.FACADES
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toSimpleString
import cc.mewcraft.wakame.util.toStableDouble
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
 * Check the [AttributeFacade] for more details.
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
    val FACADES: Registry<Key, AttributeFacade<CoreAttribute, TemplateCoreAttribute>> = SimpleRegistry()

    /**
     * The config of all attributes.
     */
    val CONFIG: ConfigProvider by lazy { Configs.YAML[ATTRIBUTE_GLOBAL_CONFIG_FILE] }

    /**
     * Builds an attribute facade.
     *
     * 注意, 参数 [key] 仅仅是词条在 NBT/模板 中的唯一标识.
     * 底层由多个对象组成的词条标识就与这里的 [key] 不同.
     *
     * 例如攻击力这个属性词条, 底层实际上是由两个属性组成的, 分别是 `MIN_ATTACK_DAMAGE` 和
     * `MAX_ATTACK_DAMAGE`, 但攻击力属性词条在 NBT/模板中的标识是一个经过“合并”得到的
     * `attribute:attack_damage`.
     *
     * @param key 词条在 NBT/模板 中的唯一标识
     */
    private fun buildFacade(key: String): FormatSelection {
        return FormatSelectionImpl(Key(Namespaces.ATTRIBUTE, key))
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
        +buildFacade("attack_speed_level").single().bind { ATTACK_SPEED_LEVEL }.override {
            // create closures
            val tooltips = DiscreteTooltips(config)
            // override it
            createTooltipLore = { core: CoreAttributeS ->
                val lines = tooltips.line(core.operation)
                val resolver = tooltips.value("value", core.value.toInt())
                listOf(AttributeRegistrySupport.miniMessage.deserialize(lines, resolver))
            }
        }
        +buildFacade("block_interaction_range").single().bind { BLOCK_INTERACTION_RANGE }
        +buildFacade("critical_strike_chance").single().bind { CRITICAL_STRIKE_CHANCE }
        +buildFacade("critical_strike_power").single().bind { CRITICAL_STRIKE_POWER }
        +buildFacade("defense").single().element().bind { DEFENSE }
        +buildFacade("defense_penetration").single().element().bind { DEFENSE_PENETRATION }
        +buildFacade("defense_penetration_rate").single().element().bind { DEFENSE_PENETRATION_RATE }
        +buildFacade("entity_interaction_range").single().bind { ENTITY_INTERACTION_RANGE }
        +buildFacade("health_regeneration").single().bind { HEALTH_REGENERATION }.override {
            // create closures
            val tooltips = NumericTooltips(config)
            // override it
            createTooltipLore = { core: CoreAttributeS ->
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
            createTooltipLore = { core: CoreAttributeS ->
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
        +buildFacade("universal_attack_damage_rate").single().bind { UNIVERSAL_ATTACK_DAMAGE_RATE }
        +buildFacade("universal_incoming_damage_rate").single().bind { UNIVERSAL_INCOMING_DAMAGE_RATE }
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
 * 包含了一个属性的各种操作和实现。
 *
 * 这里将各种操作和实现放在一起以方便外部统一调用。
 *
 * @param T the type of [CoreAttribute]
 * @param S the type of [TemplateCoreAttribute]
 */
interface AttributeFacade<T : CoreAttribute, S : TemplateCoreAttribute> : Keyed {
    /**
     * 该属性的全局配置文件.
     */
    val config: ConfigProvider

    /**
     * 属性 facade 的唯一标识。
     *
     * 属性 facade 的唯一标识与单个属性的唯一标识不一定相同。
     * 当一个属性的 facade 是由多个属性构成时（例如攻击力），
     * 它们的唯一标识就略微不同。
     */
    val facadeId: Key

    /**
     * Holds metadata about the attribute components.
     */
    val components: AttributeComponentMetadata

    /**
     * A creator for attribute modifiers.
     */
    val createAttributeModifiers: (Key, T) -> Map<Attribute, AttributeModifier>

    /**
     * A creator for [TemplateCoreAttribute].
     */
    val convertNode2Template: (ConfigurationNode) -> S

    /**
     * A creator for [CoreAttribute].
     */
    val convertNode2Instance: (ConfigurationNode) -> T

    /**
     * A creator for [CoreAttribute].
     */
    val convertNBT2Instance: (CompoundTag) -> T

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
    FACADES.register(this.facadeId, this as AttributeFacade<CoreAttribute, TemplateCoreAttribute>)
}

private operator fun AttributeFacadeOverride<*, *>.unaryPlus() {
    @Suppress("UNCHECKED_CAST")
    FACADES.register(this.prototype.facadeId, this.prototype as AttributeFacade<CoreAttribute, TemplateCoreAttribute>)
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
    ): AttributeFacadeOverride<CoreAttributeS, TemplateCoreAttributeS>
}

/**
 * 已选择 `ranged`，然后最终绑定到属性上。
 */
private interface RangedAttributeBinder {
    fun bind(
        component1: Attributes.() -> Attribute,
        component2: Attributes.() -> Attribute,
    ): AttributeFacadeOverride<CoreAttributeR, TemplateCoreAttributeR>
}

/**
 * 已选择 `single` + `element`，然后最终绑定到属性上。
 */
private interface SingleElementAttributeBinder {
    fun bind(
        component: ElementAttributes.() -> ElementAttribute,
    ): AttributeFacadeOverride<CoreAttributeSE, TemplateCoreAttributeSE>
}

/**
 * 已选择 `ranged` + `element`，然后最终绑定到属性上。
 */
private interface RangedElementAttributeBinder {
    fun bind(
        component1: ElementAttributes.() -> ElementAttribute,
        component2: ElementAttributes.() -> ElementAttribute,
    ): AttributeFacadeOverride<CoreAttributeRE, TemplateCoreAttributeRE>
}

private class AttributeFacadeOverride<BC : CoreAttribute, SC : TemplateCoreAttribute>(
    val prototype: MutableAttributeFacade<BC, SC>,
) {
    fun override(mutator: MutableAttributeFacade<BC, SC>.() -> Unit): AttributeFacade<BC, SC> {
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
 * A mutable [AttributeFacade] (except the property [facadeId]).
 */
private class MutableAttributeFacade<T : CoreAttribute, S : TemplateCoreAttribute>(
    // the config provider of this facade
    override val config: ConfigProvider,
    // this should be immutable
    override val facadeId: Key,
    // these are mutable through override() in DSL
    override var components: AttributeComponentMetadata,
    override var createAttributeModifiers: (Key, T) -> Map<Attribute, AttributeModifier>,
    override var convertNode2Template: (ConfigurationNode) -> S,
    override var convertNode2Instance: (ConfigurationNode) -> T,
    override var convertNBT2Instance: (CompoundTag) -> T,
    override var createTooltipName: (T) -> Component,
    override var createTooltipLore: (T) -> List<Component>,
) : AttributeFacade<T, S> {
    override val key: Key = facadeId
}

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
                "??? ($int)" // fallback for unknown discrete values
            }.mapValues { (_, v) ->
                AttributeRegistrySupport.miniMessage.deserialize(v)
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
    private val facadeId: Key,
) : FormatSelection {
    override fun single(): SingleSelection {
        return SingleSelectionImpl(facadeId)
    }

    override fun ranged(): RangedSelection {
        return RangedSelectionImpl(facadeId)
    }
}

private class SingleSelectionImpl(
    private val facadeId: Key,
) : SingleSelection {
    private val config: ConfigProvider = AttributeRegistry.CONFIG.derive(facadeId.value())
    private val displayName: String by config.entry<String>("display_name")
    private val tooltips: NumericTooltips = NumericTooltips(config)

    override fun element(): SingleElementAttributeBinder {
        return SingleElementAttributeBinderImpl(facadeId)
    }

    /**
     * Components: Operation, Single
     */
    override fun bind(component: Attributes.() -> Attribute): AttributeFacadeOverride<CoreAttributeS, TemplateCoreAttributeS> {
        val facade = MutableAttributeFacade(
            config = config,
            facadeId = facadeId,
            components = AttributeComponentMetadataImpl(
                AttributeComponent.Op::class, AttributeComponent.Fixed::class
            ),
            createAttributeModifiers = { id: Key, core: CoreAttributeS ->
                ImmutableMap.of(
                    Attributes.component(), AttributeModifier(id, core.value.toStableDouble(), core.operation)
                )
            },
            convertNode2Template = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val value = node.getTemplateSingle()
                TemplateCoreAttributeS(facadeId, operation, value)
            },
            convertNode2Instance = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val value = node.getSingle()
                CoreAttributeS(facadeId, operation, value)
            },
            convertNBT2Instance = { tag: CompoundTag ->
                CoreAttributeS(tag)
            },
            createTooltipName = {
                AttributeRegistrySupport.miniMessage.deserialize(displayName)
            },
            createTooltipLore = { core: CoreAttributeS ->
                val lines = tooltips.line(core.operation)
                val resolver = tooltips.number("value", core.value)
                listOf(AttributeRegistrySupport.miniMessage.deserialize(lines, resolver))
            },
        )

        return AttributeFacadeOverride(facade)
    }
}

private class RangedSelectionImpl(
    private val facadeId: Key,
) : RangedSelection {
    private val config: ConfigProvider = AttributeRegistry.CONFIG.derive(facadeId.value())
    private val displayName: String by config.entry<String>("display_name")
    private val tooltips: NumericTooltips = NumericTooltips(config)

    override fun element(): RangedElementAttributeBinder {
        return RangedElementAttributeBinderImpl(facadeId)
    }

    /**
     * Components: Operation, Ranged
     */
    override fun bind(
        component1: Attributes.() -> Attribute,
        component2: Attributes.() -> Attribute,
    ): AttributeFacadeOverride<CoreAttributeR, TemplateCoreAttributeR> {
        val facade = MutableAttributeFacade(
            config = config,
            facadeId = facadeId,
            components = AttributeComponentMetadataImpl(
                AttributeComponent.Op::class, AttributeComponent.Ranged::class
            ),
            createAttributeModifiers = { id: Key, core: CoreAttributeR ->
                ImmutableMap.of(
                    Attributes.component1(), AttributeModifier(id, core.lower.toStableDouble(), core.operation),
                    Attributes.component2(), AttributeModifier(id, core.upper.toStableDouble(), core.operation),
                )
            },
            convertNode2Template = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val lower = node.getTemplateLower()
                val upper = node.getTemplateUpper()
                TemplateCoreAttributeR(facadeId, operation, lower, upper)
            },
            convertNode2Instance = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val lower = node.getLower()
                val upper = node.getUpper()
                CoreAttributeR(facadeId, operation, lower, upper)
            },
            convertNBT2Instance = { tag: CompoundTag ->
                CoreAttributeR(tag)
            },
            createTooltipName = {
                AttributeRegistrySupport.miniMessage.deserialize(displayName)
            },
            createTooltipLore = { core: CoreAttributeR ->
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
    private val facadeId: Key,
) : SingleElementAttributeBinder {
    private val config: ConfigProvider = AttributeRegistry.CONFIG.derive(facadeId.value())
    private val displayName: String by config.entry<String>("display_name")
    private val tooltips: NumericTooltips = NumericTooltips(config)

    /**
     * Components: Operation, Single, Element
     */
    override fun bind(component: ElementAttributes.() -> ElementAttribute): AttributeFacadeOverride<CoreAttributeSE, TemplateCoreAttributeSE> {
        val facade = MutableAttributeFacade(
            config = config,
            facadeId = facadeId,
            components = AttributeComponentMetadataImpl(
                AttributeComponent.Op::class, AttributeComponent.Fixed::class, AttributeComponent.Element::class
            ),
            createAttributeModifiers = { id: Key, core: CoreAttributeSE ->
                ImmutableMap.of(
                    Attributes.element(core.element).component(), AttributeModifier(id, core.value.toStableDouble(), core.operation)
                )
            },
            convertNode2Template = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val value = node.getTemplateSingle()
                val element = node.getElement()
                TemplateCoreAttributeSE(facadeId, operation, value, element)
            },
            convertNode2Instance = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val value = node.getSingle()
                val element = node.getElement()
                CoreAttributeSE(facadeId, operation, value, element)
            },
            convertNBT2Instance = { tag: CompoundTag ->
                CoreAttributeSE(tag)
            },
            createTooltipName = {
                val resolver = Placeholder.component("element", it.element.displayName)
                AttributeRegistrySupport.miniMessage.deserialize(displayName, resolver)
            },
            createTooltipLore = { core: CoreAttributeSE ->
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
    private val facadeId: Key,
) : RangedElementAttributeBinder {
    private val config: ConfigProvider = AttributeRegistry.CONFIG.derive(facadeId.value())
    private val displayName: String by config.entry<String>("display_name")
    private val tooltips: NumericTooltips = NumericTooltips(config)

    /**
     * Components: Operation, Ranged, Element
     */
    override fun bind(
        component1: ElementAttributes.() -> ElementAttribute,
        component2: ElementAttributes.() -> ElementAttribute,
    ): AttributeFacadeOverride<CoreAttributeRE, TemplateCoreAttributeRE> {
        val facade = MutableAttributeFacade(
            config = config,
            facadeId = facadeId,
            components = AttributeComponentMetadataImpl(
                AttributeComponent.Op::class, AttributeComponent.Ranged::class, AttributeComponent.Element::class
            ),
            createAttributeModifiers = { id: Key, core: CoreAttributeRE ->
                ImmutableMap.of(
                    Attributes.element(core.element).component1(), AttributeModifier(id, core.lower.toStableDouble(), core.operation),
                    Attributes.element(core.element).component2(), AttributeModifier(id, core.upper.toStableDouble(), core.operation),
                )
            },
            convertNode2Template = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val lower = node.getTemplateLower()
                val upper = node.getTemplateUpper()
                val element = node.getElement()
                TemplateCoreAttributeRE(facadeId, operation, lower, upper, element)
            },
            convertNode2Instance = { node: ConfigurationNode ->
                val operation = node.getOperation()
                val lower = node.getLower()
                val upper = node.getUpper()
                val element = node.getElement()
                CoreAttributeRE(facadeId, operation, lower, upper, element)
            },
            convertNBT2Instance = { tag: CompoundTag ->
                CoreAttributeRE(tag)
            },
            createTooltipName = {
                val resolver = Placeholder.component("element", it.element.displayName)
                AttributeRegistrySupport.miniMessage.deserialize(displayName, resolver)
            },
            createTooltipLore = { core: CoreAttributeRE ->
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

private fun ConfigurationNode.getSingle(): Double {
    return node("value").krequire<Double>()
}

private fun ConfigurationNode.getLower(): Double {
    return node("lower").krequire<Double>()
}

private fun ConfigurationNode.getUpper(): Double {
    return node("upper").krequire<Double>()
}

private fun ConfigurationNode.getTemplateSingle(): RandomizedValue {
    return node("value").krequire<RandomizedValue>()
}

private fun ConfigurationNode.getTemplateLower(): RandomizedValue {
    return node("lower").krequire<RandomizedValue>()
}

private fun ConfigurationNode.getTemplateUpper(): RandomizedValue {
    return node("upper").krequire<RandomizedValue>()
}

private fun ConfigurationNode.getElement(): Element {
    return node("element").krequire<Element>()
}

private fun ConfigurationNode.getOperation(): Operation {
    return node("operation").string?.let { Operation.byKeyOrThrow(it) } ?: Operation.ADD
}
//</editor-fold>
