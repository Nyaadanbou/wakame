package cc.mewcraft.wakame.entity.attribute

import cc.mewcraft.wakame.MM
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.attribute.AttributeGetter
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.attribute.GLOBAL_ATTRIBUTE_CONFIG
import cc.mewcraft.wakame.attribute.bundle.*
import cc.mewcraft.wakame.attribute.bundle.ConstantAttributeBundle.Quality
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.node
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.element.ElementType
import cc.mewcraft.wakame.entity.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.RegistryLoader
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.*
import cc.mewcraft.wakame.util.adventure.toSimpleString
import com.google.common.collect.ImmutableMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import net.minecraft.nbt.CompoundTag
import org.spongepowered.configurate.ConfigurationNode
import xyz.xenondevs.commons.provider.Provider
import xyz.xenondevs.commons.provider.orElse
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.stream.Stream
import kotlin.reflect.KClass

@Init(
    stage = InitStage.PRE_WORLD, runAfter = [
        AttributeBootstrap2.Pre::class,
    ]
)
internal object AttributeFacadeRegistryLoader : RegistryLoader {
    const val CONFIG_ID: String = "attributes"
    const val FILE_PATH: String = "attributes.yml"

    @InitFun
    fun init() {
        KoishRegistries.ATTRIBUTE_BUNDLE_FACADE.resetRegistry()
        addAll()
        KoishRegistries.ATTRIBUTE_BUNDLE_FACADE.freeze()
    }

    /**
     * Starts to build an **Attribute Bundle Facade**.
     *
     * 注意, 参数 [id] 仅仅是核心在 NBT/模板 中的唯一标识.
     * 底层由多个对象组成的核心标识就与这里的 [id] 不同.
     *
     * 例如攻击力这个属性核心, 底层实际上是由两个属性组成的, 分别是 `MIN_ATTACK_DAMAGE` 和
     * `MAX_ATTACK_DAMAGE`, 但攻击力属性核心在 NBT/模板中的标识是一个经过“合并”得到的
     * `attribute:attack_damage`.
     *
     * @param id 核心在 NBT/模板 中的唯一标识
     */
    private fun build(id: String): FormatSelection {
        return FormatSelectionImpl(id)
    }

    /**
     * Registers all [AttributeFacade].
     */
    private fun addAll() {
        +build("attack_damage").ranged().element().bind(Attributes.MIN_ATTACK_DAMAGE, Attributes.MAX_ATTACK_DAMAGE)
        +build("attack_damage_rate").single().element().bind(Attributes.ATTACK_DAMAGE_RATE)
        +build("attack_effect_chance").single().bind(Attributes.ATTACK_EFFECT_CHANCE)
        +build("attack_knockback").single().bind(Attributes.ATTACK_KNOCKBACK)
        +build("block_interaction_range").single().bind(Attributes.BLOCK_INTERACTION_RANGE)
        +build("critical_strike_chance").single().bind(Attributes.CRITICAL_STRIKE_CHANCE)
        +build("critical_strike_power").single().bind(Attributes.CRITICAL_STRIKE_POWER)
        +build("defense").single().element().bind(Attributes.DEFENSE)
        +build("defense_penetration").single().element().bind(Attributes.DEFENSE_PENETRATION)
        +build("defense_penetration_rate").single().element().bind(Attributes.DEFENSE_PENETRATION_RATE)
        +build("entity_interaction_range").single().bind(Attributes.ENTITY_INTERACTION_RANGE)
        +build("hammer_damage_range").single().bind(Attributes.HAMMER_DAMAGE_RANGE)
        +build("hammer_damage_ratio").single().bind(Attributes.HAMMER_DAMAGE_RATIO)
        +build("health_regeneration").single().bind(Attributes.HEALTH_REGENERATION)
        +build("incoming_damage_rate").single().element().bind(Attributes.INCOMING_DAMAGE_RATE)
        +build("knockback_resistance").single().bind(Attributes.KNOCKBACK_RESISTANCE)
        +build("lifesteal").single().bind(Attributes.LIFESTEAL)
        +build("mana_consumption_rate").single().bind(Attributes.MANA_CONSUMPTION_RATE)
        +build("mana_regeneration").single().bind(Attributes.MANA_REGENERATION)
        +build("manasteal").single().bind(Attributes.MANASTEAL)
        +build("max_absorption").single().bind(Attributes.MAX_ABSORPTION)
        +build("max_health").single().bind(Attributes.MAX_HEALTH)
        +build("max_mana").single().bind(Attributes.MAX_MANA)
        +build("mining_efficiency").single().bind(Attributes.MINING_EFFICIENCY)
        +build("movement_speed").single().bind(Attributes.MOVEMENT_SPEED)
        +build("negative_critical_strike_power").single().bind(Attributes.NEGATIVE_CRITICAL_STRIKE_POWER)
        +build("none_critical_strike_power").single().bind(Attributes.NONE_CRITICAL_STRIKE_POWER)
        +build("safe_fall_distance").single().bind(Attributes.SAFE_FALL_DISTANCE)
        +build("scale").single().bind(Attributes.SCALE)
        +build("step_height").single().bind(Attributes.STEP_HEIGHT)
        +build("sweeping_damage_ratio").single().bind(Attributes.SWEEPING_DAMAGE_RATIO)
        +build("universal_attack_damage").ranged().bind(Attributes.UNIVERSAL_MIN_ATTACK_DAMAGE, Attributes.UNIVERSAL_MAX_ATTACK_DAMAGE)
        +build("universal_defense").single().bind(Attributes.UNIVERSAL_DEFENSE)
        +build("universal_defense_penetration").single().bind(Attributes.UNIVERSAL_DEFENSE_PENETRATION)
        +build("universal_defense_penetration_rate").single().bind(Attributes.UNIVERSAL_DEFENSE_PENETRATION_RATE)
        +build("water_movement_efficiency").single().bind(Attributes.WATER_MOVEMENT_EFFICIENCY)
    }

    private operator fun <T : ConstantAttributeBundle, S : VariableAttributeBundle> AttributeFacade<T, S>.unaryPlus() {
        @Suppress("UNCHECKED_CAST")
        KoishRegistries.ATTRIBUTE_BUNDLE_FACADE.add(
            id = Identifier.key(KOISH_NAMESPACE, id),
            value = (this as AttributeFacade<ConstantAttributeBundle, VariableAttributeBundle>)
        )
    }
}

/**
 * 包含了一个 [cc.mewcraft.wakame.attribute.bundle.AttributeBundle] 所相关的各种字段和操作.
 *
 * @param T [ConstantAttributeBundle] 的一个子类
 * @param S [VariableAttributeBundle] 的一个子类
 */
interface AttributeFacade<T : ConstantAttributeBundle, S : VariableAttributeBundle> : Keyed {
    /**
     * 本实例的全局配置文件.
     */
    val config: Provider<ConfigurationNode>

    /**
     * [属性块][cc.mewcraft.wakame.attribute.bundle.AttributeBundle]的唯一标识.
     *
     * 属性块的唯一标识与单个[属性][Attribute]的唯一标识不一定相同,
     * 当属性块是由多个属性构成时(例如攻击力),
     * 它们的唯一标识就不一样.
     */
    val id: String

    /**
     * Holds metadata about the attribute components.
     */
    val bundleTrait: AttributeBundleTraitSet

    /**
     * A creator for attribute modifiers.
     */
    val createAttributeModifiers: (Key, T) -> Map<Attribute, AttributeModifier>

    /**
     * A creator for [cc.mewcraft.wakame.item.templates.components.cells.cores.AttributeCoreArchetype].
     */
    val convertNodeToVariable: (ConfigurationNode) -> S

    /**
     * A creator for [cc.mewcraft.wakame.item.components.cells.AttributeCore].
     */
    val convertNodeToConstant: (ConfigurationNode) -> T

    /**
     * A creator for [cc.mewcraft.wakame.item.components.cells.AttributeCore].
     */
    val convertNbtToConstant: (CompoundTag) -> T

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
 * 一个属性的组件相关信息.
 */
class AttributeBundleTraitSet private constructor(
    val components: Set<KClass<out AttributeBundleTrait>>,
) {
    constructor(vararg components: KClass<out AttributeBundleTrait>) : this(components.toHashSet())

    /**
     * 查询该属性是否有指定的组件.
     *
     * @param T 组件的类型
     * @return 如果该属性拥有该组件，则返回 `true`
     */
    inline fun <reified T : AttributeBundleTrait> has(): Boolean {
        return T::class in components
    }
}


//
// Mini DSL for building an attribute bundle facade
//


/**
 * 开始选择 `single` 或 `ranged`.
 */
private interface FormatSelection {
    fun single(): SingleSelection
    fun ranged(): RangedSelection
}

/**
 * 已选择 `single` (S)，接下来直接 `bind`，或接着构造 `element`.
 */
private interface SingleSelection : AttributeBinderS {
    fun element(): AttributeBinderSE
}

/**
 * 已选择 `ranged` (R)，接下来直接 `bind`，或接着构造 `element`.
 */
private interface RangedSelection : AttributeBinderR {
    fun element(): AttributeBinderRE
}

/**
 * 已选择 `single` (S)，然后最终绑定到属性上。
 */
private interface AttributeBinderS {
    fun bind(
        component: Attribute,
    ): AttributeFacade<ConstantAttributeBundleS, VariableAttributeBundleS>
}

/**
 * 已选择 `ranged` (R)，然后最终绑定到属性上。
 */
private interface AttributeBinderR {
    fun bind(
        component1: Attribute,
        component2: Attribute,
    ): AttributeFacade<ConstantAttributeBundleR, VariableAttributeBundleR>
}

/**
 * 已选择 `single` + `element` (SE)，然后最终绑定到属性上。
 */
private interface AttributeBinderSE {
    fun bind(
        component: AttributeGetter,
    ): AttributeFacade<ConstantAttributeBundleSE, VariableAttributeBundleSE>
}

/**
 * 已选择 `ranged` + `element` (RE)，然后最终绑定到属性上。
 */
private interface AttributeBinderRE {
    fun bind(
        component1: AttributeGetter,
        component2: AttributeGetter,
    ): AttributeFacade<ConstantAttributeBundleRE, VariableAttributeBundleRE>
}


//
// Implementations start from here
//


//<editor-fold desc="Implementations">
/**
 * A mutable [AttributeFacade] (except the property [id]).
 */
private class AttributeFacadeImpl<T : ConstantAttributeBundle, S : VariableAttributeBundle>(
    override val config: Provider<ConfigurationNode>,
    override val id: String,
    override val bundleTrait: AttributeBundleTraitSet,
    override val createAttributeModifiers: (Key, T) -> Map<Attribute, AttributeModifier>,
    override val convertNodeToVariable: (ConfigurationNode) -> S,
    override val convertNodeToConstant: (ConfigurationNode) -> T,
    override val convertNbtToConstant: (CompoundTag) -> T,
    override val createTooltipName: (T) -> Component,
    override val createTooltipLore: (T) -> List<Component>,
) : AttributeFacade<T, S> {
    override val key: Key = Key.key(Namespaces.ATTRIBUTE, id)
}

private object AttributeConfigFallback {
    private val default: Provider<ConfigurationNode> = GLOBAL_ATTRIBUTE_CONFIG.node("__default__")
    val quality: Provider<Map<Quality, Component>> = default.entry("quality")
}

private sealed class Tooltips(
    config: Provider<ConfigurationNode>,
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

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("add", add),
        ExaminableProperty.of("multiplyBase", multiplyBase),
        ExaminableProperty.of("multiplyTotal", multiplyTotal)
    )

    override fun toString(): String = toSimpleString()
}

/**
 * Encapsulation of the tooltips for an attribute facade.
 *
 * @param config the config of the attribute facade
 */
private class NumericTooltips(
    config: Provider<ConfigurationNode>,
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

private class NumericScaling(
    config: Provider<ConfigurationNode>,
) : Tooltips(config) {
    private val add: Double by config.optionalEntry<Double>("scaling", "add").orElse(1.0)
    private val multiplyBase: Double by config.optionalEntry<Double>("scaling", "multiply_base").orElse(1.0)
    private val multiplyTotal: Double by config.optionalEntry<Double>("scaling", "multiply_total").orElse(1.0)

    fun scale(operation: Operation, value: Double): Double {
        return when (operation) {
            Operation.ADD -> value * add
            Operation.MULTIPLY_BASE -> value * multiplyBase
            Operation.MULTIPLY_TOTAL -> value * multiplyTotal
        }
    }
}

private class QualityText(
    config: Provider<ConfigurationNode>,
) {
    private val quality: Map<Quality, Component> by config.optionalEntry<Map<Quality, Component>>("quality").orElse(AttributeConfigFallback.quality)

    fun translate(quality: Quality?): Component {
        return this.quality[quality] ?: Component.empty()
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
    private val config: Provider<ConfigurationNode> = GLOBAL_ATTRIBUTE_CONFIG.node(id)
    private val displayName: String by config.entry<String>("display_name")
    private val tooltips: NumericTooltips = NumericTooltips(config)
    private val scaling: NumericScaling = NumericScaling(config)
    private val quality: QualityText = QualityText(config)

    override fun element(): AttributeBinderSE {
        return AttributeBinderSEImpl(id)
    }

    /**
     * Components: Operation, Single
     */
    override fun bind(component: Attribute): AttributeFacade<ConstantAttributeBundleS, VariableAttributeBundleS> {
        return AttributeFacadeImpl(
            config = config,
            id = id,
            bundleTrait = AttributeBundleTraitSet(
                AttributeBundleTrait.Operation::class, AttributeBundleTrait.Scalar::class
            ),
            createAttributeModifiers = { id, data ->
                ImmutableMap.of(
                    component, AttributeModifier(id, data.value, data.operation)
                )
            },
            convertNodeToVariable = { node ->
                val operation = node.operation
                val value = node.variableScalar
                VariableAttributeBundleS(id, operation, value)
            },
            convertNodeToConstant = { node ->
                val operation = node.operation
                val value = node.scalar
                ConstantAttributeBundleS(id, operation, value)
            },
            convertNbtToConstant = { tag ->
                ConstantAttributeBundleS(id, tag)
            },
            createTooltipName = {
                MM.deserialize(displayName)
            },
            createTooltipLore = { data ->
                val input = tooltips.line(data.operation)
                val resolver1 = tooltips.number("value", scaling.scale(data.operation, data.value))
                val resolver2 = tooltips.component("quality", quality.translate(data.quality))
                listOf(MM.deserialize(input, resolver1, resolver2))
            },
        )
    }
}

private class RangedSelectionImpl(
    private val id: String,
) : RangedSelection {
    private val config: Provider<ConfigurationNode> = GLOBAL_ATTRIBUTE_CONFIG.node(id)
    private val displayName: String by config.entry<String>("display_name")
    private val tooltips: NumericTooltips = NumericTooltips(config)
    private val scaling: NumericScaling = NumericScaling(config)
    private val quality: QualityText = QualityText(config)

    override fun element(): AttributeBinderRE {
        return AttributeBinderREImpl(id)
    }

    /**
     * Components: Operation, Ranged
     */
    override fun bind(
        component1: Attribute,
        component2: Attribute,
    ): AttributeFacade<ConstantAttributeBundleR, VariableAttributeBundleR> {
        return AttributeFacadeImpl(
            config = config,
            id = id,
            bundleTrait = AttributeBundleTraitSet(
                AttributeBundleTrait.Operation::class, AttributeBundleTrait.Ranged::class
            ),
            createAttributeModifiers = { id, core ->
                ImmutableMap.of(
                    component1, AttributeModifier(id, core.lower, core.operation),
                    component2, AttributeModifier(id, core.upper, core.operation),
                )
            },
            convertNodeToVariable = { node ->
                val operation = node.operation
                val lower = node.variableMin
                val upper = node.variableMax
                VariableAttributeBundleR(id, operation, lower, upper)
            },
            convertNodeToConstant = { node ->
                val operation = node.operation
                val lower = node.min
                val upper = node.max
                ConstantAttributeBundleR(id, operation, lower, upper)
            },
            convertNbtToConstant = { tag ->
                ConstantAttributeBundleR(id, tag)
            },
            createTooltipName = {
                MM.deserialize(displayName)
            },
            createTooltipLore = { data ->
                val lines = tooltips.line(data.operation)
                val resolver1 = tooltips.number("min", scaling.scale(data.operation, data.lower))
                val resolver2 = tooltips.number("max", scaling.scale(data.operation, data.upper))
                val resolver3 = tooltips.component("quality", quality.translate(data.quality))
                listOf(MM.deserialize(lines, resolver1, resolver2, resolver3))
            },
        )
    }
}

private class AttributeBinderSEImpl(
    private val id: String,
) : AttributeBinderSE {
    private val config: Provider<ConfigurationNode> = GLOBAL_ATTRIBUTE_CONFIG.node(id)
    private val displayName: String by config.entry<String>("display_name")
    private val tooltips: NumericTooltips = NumericTooltips(config)
    private val scaling: NumericScaling = NumericScaling(config)
    private val quality: QualityText = QualityText(config)

    /**
     * Components: Operation, Single, Element
     */
    override fun bind(component: AttributeGetter): AttributeFacade<ConstantAttributeBundleSE, VariableAttributeBundleSE> {
        return AttributeFacadeImpl(
            config = config,
            id = id,
            bundleTrait = AttributeBundleTraitSet(
                AttributeBundleTrait.Operation::class, AttributeBundleTrait.Scalar::class, AttributeBundleTrait.Element::class
            ),
            createAttributeModifiers = { id, data ->
                val k1 = component.of(data.element)
                val v1 = AttributeModifier(id, data.value, data.operation)
                ImmutableMap.of(k1, v1)
            },
            convertNodeToVariable = { node ->
                val operation = node.operation
                val value = node.variableScalar
                val element = node.element
                VariableAttributeBundleSE(id, operation, value, element)
            },
            convertNodeToConstant = { node ->
                val operation = node.operation
                val value = node.scalar
                val element = node.element
                ConstantAttributeBundleSE(id, operation, value, element)
            },
            convertNbtToConstant = { tag ->
                ConstantAttributeBundleSE(id, tag)
            },
            createTooltipName = {
                val resolver = Placeholder.component("element", it.element.value.displayName)
                MM.deserialize(displayName, resolver)
            },
            createTooltipLore = { data ->
                val input = tooltips.line(data.operation)
                val resolver1 = tooltips.number("value", scaling.scale(data.operation, data.value))
                val resolver2 = tooltips.component("element", data.element.value.displayName)
                val resolver3 = tooltips.component("quality", quality.translate(data.quality))
                listOf(MM.deserialize(input, resolver1, resolver2, resolver3))
            },
        )
    }
}

private class AttributeBinderREImpl(
    private val id: String,
) : AttributeBinderRE {
    private val config: Provider<ConfigurationNode> = GLOBAL_ATTRIBUTE_CONFIG.node(id)
    private val displayName: String by config.entry<String>("display_name")
    private val tooltips: NumericTooltips = NumericTooltips(config)
    private val scaling: NumericScaling = NumericScaling(config)
    private val quality: QualityText = QualityText(config)

    /**
     * Components: Operation, Ranged, Element
     */
    override fun bind(
        component1: AttributeGetter,
        component2: AttributeGetter,
    ): AttributeFacade<ConstantAttributeBundleRE, VariableAttributeBundleRE> {
        return AttributeFacadeImpl(
            config = config,
            id = id,
            bundleTrait = AttributeBundleTraitSet(
                AttributeBundleTrait.Operation::class, AttributeBundleTrait.Ranged::class, AttributeBundleTrait.Element::class
            ),
            createAttributeModifiers = { id, data ->
                ImmutableMap.of(
                    component1.of(data.element), AttributeModifier(id, data.lower, data.operation),
                    component2.of(data.element), AttributeModifier(id, data.upper, data.operation),
                )
            },
            convertNodeToVariable = { node ->
                val operation = node.operation
                val lower = node.variableMin
                val upper = node.variableMax
                val element = node.element
                VariableAttributeBundleRE(id, operation, lower, upper, element)
            },
            convertNodeToConstant = { node ->
                val operation = node.operation
                val lower = node.min
                val upper = node.max
                val element = node.element
                ConstantAttributeBundleRE(id, operation, lower, upper, element)
            },
            convertNbtToConstant = { tag ->
                ConstantAttributeBundleRE(id, tag)
            },
            createTooltipName = {
                val resolver = Placeholder.component("element", it.element.value.displayName)
                MM.deserialize(displayName, resolver)
            },
            createTooltipLore = { data ->
                val input = tooltips.line(data.operation)
                val resolver1 = tooltips.number("min", scaling.scale(data.operation, data.lower))
                val resolver2 = tooltips.number("max", scaling.scale(data.operation, data.upper))
                val resolver3 = tooltips.component("element", data.element.value.displayName)
                val resolver4 = tooltips.component("quality", quality.translate(data.quality))
                listOf(MM.deserialize(input, resolver1, resolver2, resolver3, resolver4))
            },
        )
    }
}


/* Specialized Configuration Operations */


private val ConfigurationNode.operation: Operation
    get() = node("operation").string?.let(Operation.Companion::byName) ?: Operation.ADD

private val ConfigurationNode.element: RegistryEntry<ElementType>
    get() = node("element").require<RegistryEntry<ElementType>>()

private val ConfigurationNode.scalar: Double
    get() = node("value").require<Double>()

private val ConfigurationNode.min: Double
    get() = node("lower").require<Double>()

private val ConfigurationNode.max: Double
    get() = node("upper").require<Double>()

private val ConfigurationNode.variableScalar: RandomizedValue
    get() = node("value").require<RandomizedValue>()

private val ConfigurationNode.variableMin: RandomizedValue
    get() = node("lower").require<RandomizedValue>()

private val ConfigurationNode.variableMax: RandomizedValue
    get() = node("upper").require<RandomizedValue>()
//</editor-fold>
