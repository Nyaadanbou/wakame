package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.element.Element
import com.google.common.collect.Multimap
import com.google.common.collect.MultimapBuilder
import java.util.concurrent.ConcurrentHashMap

/**
 * The container that holds all **non-elemental** [Attribute] instances.
 *
 * Use [Attributes.element] to get the container for [ElementAttribute].
 *
 * The attribute instances in this singleton object are primarily served as
 * "lookup index" for other code in this project. The numeric values passing to the
 * attribute constructors are just fallback values when the config provides nothing.
 */
object Attributes : AttributeCollectionProvider<Attribute> {
    // 一个空的属性, 本身没有任何作用.
    // 这只是一个特殊值, 供其他系统使用.
    val EMPTY = Attribute("empty", .0).register()

    //<editor-fold desc="原版属性">
    // 请在这里添加/获取*原版属性*的实例.
    // ------
    // 这些属性需要原版属性作为后端才能在游戏中生效.
    val BLOCK_INTERACTION_RANGE = RangedAttribute("block_interaction_range", 4.5, 1.0, 64.0, true).register()
    val ENTITY_INTERACTION_RANGE = RangedAttribute("entity_interaction_range", 3.0, 1.0, 64.0, true).register()
    val MAX_ABSORPTION = RangedAttribute("max_absorption", .0, .0, 2048.0, true).register()
    val MAX_HEALTH = RangedAttribute("max_health", 20.0, 1.0, 16384.0, true).register()
    val MOVEMENT_SPEED = RangedAttribute("movement_speed", .0, -1.0, 4.0, true).register()
    //</editor-fold>

    //<editor-fold desc="萌芽属性">
    // 请在这里添加/获取*萌芽属性*的实例.
    // ------
    // 这些属性需要我们自己实现才能在游戏中生效. 所谓“自己实现”,
    // 就是说, 我们需要通过自定义监听器或调度器等方式来实现它们.
    val ATTACK_EFFECT_CHANCE = RangedAttribute("attack_effect_chance", 0.01, .0, 1.0).register()
    val CRITICAL_STRIKE_CHANCE = RangedAttribute("critical_strike_chance", 0.0, -1.0, 1.0).register()
    val CRITICAL_STRIKE_POWER = RangedAttribute("critical_strike_power", 1.0, 1.0, 16384.0).register()
    val HEALTH_REGENERATION = RangedAttribute("health_regeneration", 1.0, .0, 16384.0).register()
    val LIFESTEAL = RangedAttribute("lifesteal", .0, .0, 16384.0).register()
    val MANASTEAL = RangedAttribute("manasteal", .0, .0, 16384.0).register()
    val MANA_CONSUMPTION_RATE = RangedAttribute("mana_consumption_rate", 1.0, .0, 5.0).register()
    val MANA_REGENERATION = RangedAttribute("mana_regeneration", 1.0, .0, 16384.0).register()
    val MAX_MANA = RangedAttribute("max_mana", 100.0, .0, 16384.0).register()
    val NEGATIVE_CRITICAL_STRIKE_POWER = RangedAttribute("negative_critical_strike_power", 1.0, .0, 1.0).register()
    val UNIVERSAL_DEFENSE = RangedAttribute("universal_defense", .0, -16384.0, 16384.0).register()
    val UNIVERSAL_DEFENSE_PENETRATION = RangedAttribute("universal_defense_penetration", .0, -16384.0, 16384.0).register()
    val UNIVERSAL_DEFENSE_PENETRATION_RATE = RangedAttribute("universal_defense_penetration_rate", .0, 0.0, 1.0).register()
    val UNIVERSAL_MAX_ATTACK_DAMAGE = RangedAttribute("universal_attack_damage", "universal_max_attack_damage", .0, .0, 16384.0).register()
    val UNIVERSAL_MIN_ATTACK_DAMAGE = RangedAttribute("universal_attack_damage", "universal_min_attack_damage", .0, .0, 16384.0).register()
    //</editor-fold>

    /**
     * Gets specific [ElementAttributes] by the [element].
     *
     * This is the only way you get [ElementAttribute] instances.
     *
     * You first get an [ElementAttributes] by the [element], from which
     * you can get the [ElementAttribute] by accessing its properties.
     *
     * @param element the element of the attribute
     * @return the [ElementAttributes] instance
     *
     * @see ElementAttributes
     */
    fun element(element: Element): ElementAttributes {
        return ElementAttributeInternals.getElementAttributes(element)
    }

    /**
     * Gets all [Attribute.facadeId] of the attributes that are backed by vanilla attributes.
     */
    fun getVanillaAttributeNames(): Collection<String> {
        return AttributeNameInternals.getNamesOfVanilla()
    }

    /**
     * Gets all [Attribute.facadeId] of the element attributes.
     */
    fun getElementAttributeNames(): Collection<String> {
        return AttributeNameInternals.getNamesOfElement()
    }

    /**
     * Gets specific [Attribute] instances by the [facadeId].
     *
     * The returned collect does *not* include any elemental attributes!
     */
    override fun getAttributesByFacade(facadeId: String): Collection<Attribute> {
        return AttributeInternals.byFacade(facadeId)
    }

    private fun Attribute.register(): Attribute {
        return AttributeInternals.register(this)
    }
}

/**
 * A container which owns all [ElementAttribute] instances for a type of [Element].
 */
@Suppress("PropertyName", "MemberVisibilityCanBePrivate")
class ElementAttributes internal constructor(
    /**
     * The element type of this container.
     */
    val ELEMENT: Element,
) : AttributeCollectionProvider<ElementAttribute> {
    val DEFENSE = ElementAttribute("defense", .0, -16384.0, 16384.0, ELEMENT).register()
    val DEFENSE_PENETRATION = ElementAttribute("defense_penetration", .0, -16384.0, 16384.0, ELEMENT).register()
    val DEFENSE_PENETRATION_RATE = ElementAttribute("defense_penetration_rate", .0, .0, 1.0, ELEMENT).register()
    val MAX_ATTACK_DAMAGE = ElementAttribute("attack_damage", "max_attack_damage", .0, .0, 16384.0, ELEMENT).register()
    val MIN_ATTACK_DAMAGE = ElementAttribute("attack_damage", "min_attack_damage", .0, .0, 16384.0, ELEMENT).register()
    val ATTACK_DAMAGE_RATE = ElementAttribute("attack_damage_rate", 1.0, -1.0, 16384.0, ELEMENT).register()
    val INCOMING_DAMAGE_RATE = ElementAttribute("incoming_damage_rate", 1.0, -1.0, 16384.0, ELEMENT).register()

    /**
     * Gets all the [ElementAttribute] instances by the [facadeId].
     */
    override fun getAttributesByFacade(facadeId: String): Collection<ElementAttribute> {
        return ElementAttributeInternals.byFacade(ELEMENT, facadeId)
    }

    private fun ElementAttribute.register(): ElementAttribute {
        return ElementAttributeInternals.register(this)
    }
}

/**
 * Holds one or more instances of [Attribute].
 */
interface AttributeCollectionProvider<T : Attribute> {
    /**
     * Gets zero or more [Attribute]s by [facadeId].
     *
     * **Remember that different [Attribute] instances may have the same facade identity!**
     *
     * The returned list may contain zero or more attributes:
     * - `=0`: the facade is not registered
     * - `=1`: the facade is bound to exactly one attribute
     * - `>1`: the facade is bound to more than one attributes
     *
     * ## Side notes
     *
     * This function is primarily used by the config deserializer.
     *
     * @param facadeId the facade identity
     * @return zero or more attributes
     */
    fun getAttributesByFacade(facadeId: String): Collection<T>
}


/* Internals */


// 封装了一些内部状态, 以提供更好的接口体验
private object AttributeInternals {
    // 从 facadeId 映射到若干个 Attribute
    private val BY_FACADE_ID: Multimap<String, Attribute> = MultimapBuilder.hashKeys().linkedHashSetValues().build()

    fun register(attribute: Attribute): Attribute {
        BY_FACADE_ID.put(attribute.facadeId, attribute)
        AttributeNameInternals.register(attribute)
        return attribute
    }

    fun byFacade(facadeId: String): Collection<Attribute> {
        return BY_FACADE_ID.get(facadeId)
    }
}

// 封装了一些内部状态, 以提供更好的接口体验
private object ElementAttributeInternals {
    // 从 Element 映射到若干个 ElementAttribute
    private val BY_FACADE_ID: ConcurrentHashMap<Element, Multimap<String, ElementAttribute>> = ConcurrentHashMap()

    fun register(attribute: ElementAttribute): ElementAttribute {
        BY_FACADE_ID.computeIfAbsent(attribute.element) {
            MultimapBuilder.hashKeys().linkedHashSetValues().build()
        }.put(attribute.facadeId, attribute)
        AttributeNameInternals.register(attribute)
        return attribute
    }

    fun byFacade(element: Element, facadeId: String): Collection<ElementAttribute> {
        return BY_FACADE_ID[element]?.get(facadeId) ?: throw IllegalArgumentException("Unknown facade identity: '$facadeId'")
    }

    // 从 Element 映射到 ElementAttributes
    private val ELEMENT_ATTRIBUTES: ConcurrentHashMap<Element, ElementAttributes> = ConcurrentHashMap()

    fun getElementAttributes(element: Element): ElementAttributes {
        return ELEMENT_ATTRIBUTES.computeIfAbsent(element, ::ElementAttributes)
    }
}

// 封装了一些内部状态, 以提供更好的接口体验
private object AttributeNameInternals {
    private val VANILLA_ATTRIBUTE_NAMES: MutableSet<String> = ConcurrentHashMap.newKeySet()
    private val ELEMENT_ATTRIBUTE_NAMES: MutableSet<String> = ConcurrentHashMap.newKeySet()

    fun register(attribute: Attribute) {
        tryRegisterVanillaAttribute(attribute)
        tryRegisterElementAttribute(attribute)
    }

    fun getNamesOfVanilla(): Collection<String> {
        return VANILLA_ATTRIBUTE_NAMES
    }

    fun getNamesOfElement(): Collection<String> {
        return ELEMENT_ATTRIBUTE_NAMES
    }

    private fun tryRegisterVanillaAttribute(attribute: Attribute) {
        if (attribute.vanilla) {
            VANILLA_ATTRIBUTE_NAMES.add(attribute.facadeId)
        }
    }

    private fun tryRegisterElementAttribute(attribute: Attribute) {
        if (attribute is ElementAttribute) {
            ELEMENT_ATTRIBUTE_NAMES.add(attribute.facadeId)
        }
    }
}