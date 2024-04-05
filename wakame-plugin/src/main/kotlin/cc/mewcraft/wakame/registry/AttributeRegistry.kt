package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.attribute.ElementAttribute
import cc.mewcraft.wakame.attribute.facade.AttributeComponent
import cc.mewcraft.wakame.attribute.facade.BinaryAttributeCoreNbtEncoder
import cc.mewcraft.wakame.attribute.facade.BinaryAttributeCoreNodeEncoder
import cc.mewcraft.wakame.attribute.facade.SchemaAttributeCoreNodeEncoder
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.item.binary.cell.core.BinaryAttributeCore
import cc.mewcraft.wakame.item.schema.cell.core.SchemaAttributeCore
import cc.mewcraft.wakame.registry.AttributeRegistry.FACADES
import cc.mewcraft.wakame.util.*
import com.google.common.collect.ImmutableMap
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.util.UUID
import kotlin.reflect.KClass

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
    val FACADES: Registry<Key, AttributeFacade<BinaryAttributeCore>> = SimpleRegistry()

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

    private fun registerFacades() {
        // Registry more attribute facade here ...

        +buildFacade("attack_damage", ShadowTagType.SHORT).ranged().element().bind(Attributes.byElement { MIN_ATTACK_DAMAGE }, Attributes.byElement { MAX_ATTACK_DAMAGE })
        +buildFacade("attack_effect_chance", ShadowTagType.DOUBLE).single().bind(Attributes.ATTACK_EFFECT_CHANCE)
        +buildFacade("attack_speed_level", ShadowTagType.BYTE).single().bind(Attributes.ATTACK_SPEED_LEVEL)
        +buildFacade("block_interaction_range", ShadowTagType.DOUBLE).single().bind(Attributes.BLOCK_INTERACTION_RANGE)
        +buildFacade("critical_strike_chance", ShadowTagType.DOUBLE).single().bind(Attributes.CRITICAL_STRIKE_CHANCE)
        +buildFacade("critical_strike_power", ShadowTagType.DOUBLE).single().bind(Attributes.CRITICAL_STRIKE_POWER)
        +buildFacade("damage_reduction_rate", ShadowTagType.DOUBLE).single().bind(Attributes.DAMAGE_REDUCTION_RATE)
        +buildFacade("defense", ShadowTagType.SHORT).single().element().bind(Attributes.byElement { DEFENSE })
        +buildFacade("defense_penetration", ShadowTagType.SHORT).single().element().bind(Attributes.byElement { DEFENSE_PENETRATION })
        +buildFacade("defense_penetration_rate", ShadowTagType.DOUBLE).single().element().bind(Attributes.byElement { DEFENSE_PENETRATION_RATE })
        +buildFacade("entity_interaction_range", ShadowTagType.DOUBLE).single().bind(Attributes.ENTITY_INTERACTION_RANGE)
        +buildFacade("health_regeneration", ShadowTagType.SHORT).single().bind(Attributes.HEALTH_REGENERATION)
        +buildFacade("lifesteal", ShadowTagType.SHORT).single().bind(Attributes.LIFESTEAL)
        +buildFacade("lifesteal_rate", ShadowTagType.DOUBLE).single().bind(Attributes.LIFESTEAL_RATE)
        +buildFacade("mana_consumption_rate", ShadowTagType.DOUBLE).single().bind(Attributes.MANA_CONSUMPTION_RATE)
        +buildFacade("mana_regeneration", ShadowTagType.SHORT).single().bind(Attributes.MANA_REGENERATION)
        +buildFacade("manasteal", ShadowTagType.SHORT).single().bind(Attributes.MANASTEAL)
        +buildFacade("manasteal_rate", ShadowTagType.DOUBLE).single().bind(Attributes.MANASTEAL_RATE)
        +buildFacade("max_absorption", ShadowTagType.SHORT).single().bind(Attributes.MAX_ABSORPTION)
        +buildFacade("max_health", ShadowTagType.SHORT).single().bind(Attributes.MAX_HEALTH)
        +buildFacade("max_mana", ShadowTagType.SHORT).single().bind(Attributes.MAX_MANA)
        +buildFacade("movement_speed_rate", ShadowTagType.DOUBLE).single().bind(Attributes.MOVEMENT_SPEED_RATE)
    }

    override fun onPreWorld() {
        registerFacades()
    }

    override fun onReload() {
        // TODO("Not yet implemented") // what to reload?
    }
}

/**
 * 集合了一个属性的各种操作和实现。
 *
 * 将各种实现放在一起以方便外部使用。
 *
 * @param A the type parameter of [AttributeModifierCreator]
 */
@Suppress("PropertyName")
interface AttributeFacade<out A : BinaryAttributeCore> {

    /**
     * 注册此对象。
     */
    operator fun unaryPlus() {
        FACADES.register(KEY, this)
    }

    /**
     * 属性 facade 的唯一标识。
     *
     * 属性 facade 的唯一标识与单个属性的唯一标识不一定相同。
     * 当一个属性的 facade 是由多个属性构成时（例如攻击力），
     * 它们的唯一标识就略微不同。
     */
    val KEY: Key

    /**
     * TBD.
     */
    val MODIFIER_FACTORY: AttributeModifierCreator<@UnsafeVariance A>

    /**
     * TBD.
     */
    val STRUCTURE_METADATA: AttributeStructureMetadata

    /**
     * TBD.
     */
    val SCHEMA_CORE_NODE_ENCODER: SchemaAttributeCoreNodeEncoder

    /**
     * TBD.
     */
    val BINARY_CORE_NODE_ENCODER: BinaryAttributeCoreNodeEncoder

    /**
     * TBD.
     */
    val BINARY_CORE_NBT_ENCODER: BinaryAttributeCoreNbtEncoder
}

/**
 * 一个属性修饰符的创建器。
 *
 * @param T the type of [BinaryAttributeCore] which this creator
 *          creates attribute modifier out of
 */
fun interface AttributeModifierCreator<in T : BinaryAttributeCore> {
    fun makeAttributeModifiers(uuid: UUID, core: T): Map<Attribute, AttributeModifier>
}

/**
 * 一个属性的组件相关信息。
 */
class AttributeStructureMetadata private constructor(
    private val components: Set<KClass<out AttributeComponent>>,
) {
    constructor(vararg components: KClass<out AttributeComponent>) : this(components.toSet())

    /**
     * 查询该属性是否有指定的组件。
     *
     * @param T 组件的类型
     * @param componentClass 组件的接口类
     * @return 如果该属性拥有该组件，则返回 `true`
     */
    fun <T : AttributeComponent> hasComponent(componentClass: KClass<T>): Boolean {
        return components.any { it == componentClass }
    }

    /**
     * @see hasComponent
     */
    inline fun <reified T : AttributeComponent> hasComponent(): Boolean {
        return hasComponent(T::class)
    }
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
    fun bind(component: Attribute): AttributeFacade<BinaryAttributeCore.S>
}

/**
 * 已选择 ranged，然后最终绑定到属性上。
 */
interface RangedAttributeBinder {
    fun bind(component1: Attribute, component2: Attribute): AttributeFacade<BinaryAttributeCore.R>
}

/**
 * 已选择 single + element，然后最终绑定到属性上。
 */
interface SingleElementAttributeBinder {
    fun bind(component: (Element) -> ElementAttribute): AttributeFacade<BinaryAttributeCore.SE>
}

/**
 * 已选择 ranged + element，然后最终绑定到属性上。
 */
interface RangedElementAttributeBinder {
    fun bind(component1: (Element) -> ElementAttribute, component2: (Element) -> ElementAttribute): AttributeFacade<BinaryAttributeCore.RE>
}

//<editor-fold desc="Implementation">
private class AttributeFacadeImpl<T : BinaryAttributeCore>(
    override val KEY: Key,
    override val MODIFIER_FACTORY: AttributeModifierCreator<T>,
    override val STRUCTURE_METADATA: AttributeStructureMetadata,
    override val SCHEMA_CORE_NODE_ENCODER: SchemaAttributeCoreNodeEncoder,
    override val BINARY_CORE_NODE_ENCODER: BinaryAttributeCoreNodeEncoder,
    override val BINARY_CORE_NBT_ENCODER: BinaryAttributeCoreNbtEncoder,
) : AttributeFacade<T>

private class FormatSelectionImpl(
    private val facadeKey: Key,
    private val tagType: ShadowTagType,
) : FormatSelection {
    override fun single(): SingleSelectionImpl {
        return SingleSelectionImpl(facadeKey, tagType)
    }

    override fun ranged(): RangedSelectionImpl {
        return RangedSelectionImpl(facadeKey, tagType)
    }
}

private class SingleSelectionImpl(
    private val facadeKey: Key,
    private val tagType: ShadowTagType,
) : SingleSelection {
    override fun element(): SingleElementAttributeBinderImpl {
        return SingleElementAttributeBinderImpl(facadeKey, tagType)
    }

    /**
     * Components: Operation, Single
     */
    override fun bind(
        component: Attribute,
    ): AttributeFacade<BinaryAttributeCore.S> = AttributeFacadeImpl(
        KEY = facadeKey,

        MODIFIER_FACTORY = { uuid: UUID, core: BinaryAttributeCore.S ->
            ImmutableMap.of(
                component, AttributeModifier(uuid, core.value.toStableDouble(), core.operation),
            )
        },

        STRUCTURE_METADATA = AttributeStructureMetadata(
            AttributeComponent.Op::class, AttributeComponent.Single::class
        ),

        SCHEMA_CORE_NODE_ENCODER = { node: ConfigurationNode ->
            val operation = node.getOperation()
            val value = node.getSchemaSingle()
            SchemaAttributeCore.S(facadeKey, tagType, operation, value)
        },

        BINARY_CORE_NODE_ENCODER = { node: ConfigurationNode ->
            val operation = node.getOperation()
            val value = node.getBinarySingle()
            BinaryAttributeCore.S(facadeKey, tagType, operation, value)
        },

        BINARY_CORE_NBT_ENCODER = { compound: CompoundShadowTag ->
            val operation = compound.getOperation()
            val value = compound.getNumber(NekoTags.Attribute.VAL)
            BinaryAttributeCore.S(facadeKey, tagType, operation, value)
        },
    )
}

private class RangedSelectionImpl(
    private val facadeKey: Key,
    private val tagType: ShadowTagType,
) : RangedSelection {
    override fun element(): RangedElementAttributeBinderImpl {
        return RangedElementAttributeBinderImpl(facadeKey, tagType)
    }

    /**
     * Components: Operation, Ranged
     */
    override fun bind(
        component1: Attribute,
        component2: Attribute,
    ): AttributeFacade<BinaryAttributeCore.R> = AttributeFacadeImpl(
        KEY = facadeKey,

        MODIFIER_FACTORY = { uuid: UUID, core: BinaryAttributeCore.R ->
            ImmutableMap.of(
                component1, AttributeModifier(uuid, core.lower.toStableDouble(), core.operation),
                component2, AttributeModifier(uuid, core.upper.toStableDouble(), core.operation),
            )
        },

        STRUCTURE_METADATA = AttributeStructureMetadata(
            AttributeComponent.Op::class, AttributeComponent.Ranged::class
        ),

        SCHEMA_CORE_NODE_ENCODER = { node: ConfigurationNode ->
            val operation = node.getOperation()
            val lower = node.getSchemaLower()
            val upper = node.getSchemaUpper()
            SchemaAttributeCore.R(facadeKey, tagType, operation, lower, upper)
        },

        BINARY_CORE_NODE_ENCODER = { node: ConfigurationNode ->
            val operation = node.getOperation()
            val lower = node.getBinaryLower()
            val upper = node.getBinaryUpper()
            BinaryAttributeCore.R(facadeKey, tagType, operation, lower, upper)
        },

        BINARY_CORE_NBT_ENCODER = { compound: CompoundShadowTag ->
            val lower = compound.getNumber(NekoTags.Attribute.MIN)
            val upper = compound.getNumber(NekoTags.Attribute.MAX)
            val operation = compound.getOperation()
            BinaryAttributeCore.R(facadeKey, tagType, operation, lower, upper)
        },
    )
}

private class SingleElementAttributeBinderImpl(
    private val facadeKey: Key,
    private val tagType: ShadowTagType,
) : SingleElementAttributeBinder {

    /**
     * Components: Operation, Single, Element
     */
    override fun bind(
        component: (Element) -> ElementAttribute,
    ): AttributeFacade<BinaryAttributeCore.SE> = AttributeFacadeImpl(
        KEY = facadeKey,

        MODIFIER_FACTORY = { uuid: UUID, core: BinaryAttributeCore.SE ->
            ImmutableMap.of(
                component(core.element), AttributeModifier(uuid, core.value.toStableDouble(), core.operation)
            )
        },

        STRUCTURE_METADATA = AttributeStructureMetadata(
            AttributeComponent.Op::class, AttributeComponent.Single::class, AttributeComponent.Element::class
        ),

        SCHEMA_CORE_NODE_ENCODER = { node: ConfigurationNode ->
            val operation = node.getOperation()
            val value = node.getSchemaSingle()
            val element = node.getElement()
            SchemaAttributeCore.SE(facadeKey, tagType, operation, value, element)
        },

        BINARY_CORE_NODE_ENCODER = { node: ConfigurationNode ->
            val operation = node.getOperation()
            val value = node.getBinarySingle()
            val element = node.getElement()
            BinaryAttributeCore.SE(facadeKey, tagType, operation, value, element)
        },

        BINARY_CORE_NBT_ENCODER = { compound: CompoundShadowTag ->
            val value = compound.getNumber(NekoTags.Attribute.VAL)
            val element = compound.getElement()
            val operation = compound.getOperation()
            BinaryAttributeCore.SE(facadeKey, tagType, operation, value, element)
        },
    )
}

private class RangedElementAttributeBinderImpl(
    private val facadeKey: Key,
    private val tagType: ShadowTagType,
) : RangedElementAttributeBinder {

    /**
     * Components: Operation, Ranged, Element
     */
    override fun bind(
        component1: (Element) -> ElementAttribute,
        component2: (Element) -> ElementAttribute,
    ): AttributeFacade<BinaryAttributeCore.RE> = AttributeFacadeImpl(
        KEY = facadeKey,

        MODIFIER_FACTORY = { uuid: UUID, core: BinaryAttributeCore.RE ->
            ImmutableMap.of(
                component1(core.element), AttributeModifier(uuid, core.lower.toStableDouble(), core.operation),
                component2(core.element), AttributeModifier(uuid, core.upper.toStableDouble(), core.operation),
            )
        },

        STRUCTURE_METADATA = AttributeStructureMetadata(
            AttributeComponent.Op::class, AttributeComponent.Ranged::class, AttributeComponent.Element::class
        ),

        SCHEMA_CORE_NODE_ENCODER = { node: ConfigurationNode ->
            val operation = node.getOperation()
            val lower = node.getSchemaLower()
            val upper = node.getSchemaUpper()
            val element = node.getElement()
            SchemaAttributeCore.RE(facadeKey, tagType, operation, lower, upper, element)
        },

        BINARY_CORE_NODE_ENCODER = { node: ConfigurationNode ->
            val operation = node.getOperation()
            val lower = node.getBinaryLower()
            val upper = node.getBinaryUpper()
            val element = node.getElement()
            BinaryAttributeCore.RE(facadeKey, tagType, operation, lower, upper, element)
        },

        BINARY_CORE_NBT_ENCODER = { compound: CompoundShadowTag ->
            val lower = compound.getNumber(NekoTags.Attribute.MIN)
            val upper = compound.getNumber(NekoTags.Attribute.MAX)
            val element = compound.getElement()
            val operation = compound.getOperation()
            BinaryAttributeCore.RE(facadeKey, tagType, operation, lower, upper, element)
        },
    )
}

/* Specialized Compound Operations */

private fun CompoundShadowTag.getElement(): Element {
    return this.getByteOrNull(NekoTags.Attribute.ELEMENT)?.let { ElementRegistry.getBy(it) } ?: ElementRegistry.DEFAULT
}

private fun CompoundShadowTag.getOperation(): AttributeModifier.Operation {
    return AttributeModifier.Operation.byId(this.getInt(NekoTags.Attribute.OPERATION))
}

private fun CompoundShadowTag.getNumber(key: String): Double {
    return this.getDouble(key)
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

private fun ConfigurationNode.getOperation(): AttributeModifier.Operation {
    return node("operation").string?.let { AttributeModifier.Operation.byKey(it) } ?: AttributeModifier.Operation.ADD
}
//</editor-fold>
