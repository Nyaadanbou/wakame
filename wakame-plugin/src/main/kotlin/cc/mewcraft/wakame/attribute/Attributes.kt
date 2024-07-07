@file:Suppress("PropertyName", "PrivatePropertyName")

package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.element.Element
import com.google.common.collect.Multimap
import com.google.common.collect.MultimapBuilder
import java.util.concurrent.ConcurrentHashMap

/**
 * Holds instances of [Attribute].
 */
interface AttributeContainer<T : Attribute> {
    /**
     * Gets zero or more [Attribute]s by [facadeId].
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
     * @param facadeId the facade ID
     * @return zero or more attributes
     */
    fun byFacade(facadeId: String): Collection<T>
}

/**
 * The container that holds all **non-elemental** [Attribute] instances.
 *
 * Use [Attributes.byElement] to get the container for [ElementAttribute].
 *
 * The attribute instances in this singleton object are primarily served as
 * "lookup index" for other code in this project. The numeric values passing to the
 * attribute constructors are just fallback values when the config provides nothing.
 */
object Attributes : AttributeContainer<Attribute> {
    private val ELEMENT_ATTRIBUTE_CONTAINERS: ConcurrentHashMap<Element, ElementAttributeContainer> = ConcurrentHashMap()
    private val BY_FACADE_ID: Multimap<String, Attribute> = MultimapBuilder.hashKeys().linkedHashSetValues().build()
    private fun register(attribute: Attribute): Attribute {
        BY_FACADE_ID.put(attribute.facadeId, attribute)

        if (attribute.vanilla) {
            AttributeContainerSupport.VANILLA_ATTRIBUTE_NAMES += attribute.facadeId
        }

        return attribute
    }

    //<editor-fold desc="Types">
    /**
     * An empty attribute that does nothing on its own.
     *
     * It only serves as a special value for other systems to use.
     */
    val EMPTY = Attribute("empty", .0).apply(::register)

    /*
       Vanilla Attributes

       These are the attributes that need vanilla ones
       as backend to make them effective in game.
     */

    val BLOCK_INTERACTION_RANGE = RangedAttribute("block_interaction_range", 4.5, 1.0, 64.0, true).apply(::register)
    val ENTITY_INTERACTION_RANGE = RangedAttribute("entity_interaction_range", 3.0, 1.0, 64.0, true).apply(::register)
    val MAX_ABSORPTION = RangedAttribute("max_absorption", .0, .0, 2048.0, true).apply(::register)
    val MAX_HEALTH = RangedAttribute("max_health", 20.0, 1.0, 16384.0, true).apply(::register)
    val MOVEMENT_SPEED = RangedAttribute("movement_speed", .0, -1.0, 4.0, true).apply(::register)

    /*
       Wakame Attributes

       These are the attributes that need to be implemented
       on our own to make them effective in game. By "on our own",
       we mean that we will implement them by, for example,
       custom listeners or schedulers.
     */

    val ATTACK_EFFECT_CHANCE = RangedAttribute("attack_effect_chance", 0.01, .0, 1.0).apply(::register)
    val ATTACK_SPEED_LEVEL = RangedAttribute("attack_speed_level", .0, .0, 8.0).apply(::register) // !!! can only take integer
    val CRITICAL_STRIKE_CHANCE = RangedAttribute("critical_strike_chance", 0.01, .0, 1.0).apply(::register)
    val CRITICAL_STRIKE_POWER = RangedAttribute("critical_strike_power", 1.0, .0, 5.0).apply(::register)
    val HEALTH_REGENERATION = RangedAttribute("health_regeneration", 1.0, .0, 16384.0).apply(::register)
    val LIFESTEAL = RangedAttribute("lifesteal", .0, .0, 16384.0).apply(::register)
    val MANASTEAL = RangedAttribute("manasteal", .0, .0, 16384.0).apply(::register)
    val MANA_CONSUMPTION_RATE = RangedAttribute("mana_consumption_rate", 1.0, .0, 5.0).apply(::register)
    val MANA_REGENERATION = RangedAttribute("mana_regeneration", 1.0, .0, 16384.0).apply(::register)
    val MAX_MANA = RangedAttribute("max_mana", 100.0, .0, 16384.0).apply(::register)
    val UNIVERSAL_DEFENSE = RangedAttribute("universal_defense", .0, -16384.0, 16384.0).apply(::register)
    val UNIVERSAL_DEFENSE_PENETRATION = RangedAttribute("universal_defense_penetration", .0, -16384.0, 16384.0).apply(::register)
    val UNIVERSAL_DEFENSE_PENETRATION_RATE = RangedAttribute("universal_defense_penetration_rate", .0, 0.0, 1.0).apply(::register)
    val UNIVERSAL_MAX_ATTACK_DAMAGE = RangedAttribute("universal_attack_damage", "universal_max_attack_damage", .0, .0, 16384.0).apply(::register)
    val UNIVERSAL_MIN_ATTACK_DAMAGE = RangedAttribute("universal_attack_damage", "universal_min_attack_damage", .0, .0, 16384.0).apply(::register)
    val UNIVERSAL_ATTACK_DAMAGE_RATE = RangedAttribute("universal_attack_damage_rate", .0, -1.0, 16384.0).apply(::register)
    val UNIVERSAL_INCOMING_DAMAGE_RATE = RangedAttribute("universal_incoming_damage_rate", .0, -1.0, 16384.0).apply(::register)
    //</editor-fold>

    // The returned collect does NOT include any elemental attributes!
    // Use the function Attributes.byElement to get a container first.
    override fun byFacade(facadeId: String): Collection<Attribute> {
        return BY_FACADE_ID.get(facadeId)
    }

    /**
     * Gets specific [ElementAttributeContainer] by the [element].
     *
     * This is the only way you get an instance of [ElementAttribute].
     *
     * You first get a container which owns all the [ElementAttribute] instances
     * for the specific [element], from which you can get the [ElementAttribute]
     * by accessing the properties of the container.
     *
     * Example usage:
     * ```kotlin
     * val neutralElement = ...
     * val minAttackDamage = Attributes.byElement(neutralElement).MIN_ATTACK_DAMAGE
     * ```
     *
     * @param element the element of the attribute you want
     * @return the container of the specific element attribute
     *
     * @see ElementAttributeContainer
     */
    fun byElement(element: Element): ElementAttributeContainer {
        return ELEMENT_ATTRIBUTE_CONTAINERS.computeIfAbsent(element) { ElementAttributeContainer(element) }
    }

    /**
     * Creates a function which lazily gets an instance of [ElementAttribute].
     *
     * You can use this function at the time when you are required to specify an
     * [ElementAttribute] but the specific element is unknown.
     *
     * Example usage:
     * ```
     * val lazyMinAtkDmg = Attributes.lazyElement { MIN_ATTACK_DAMAGE }
     * // Some other code that knows the element
     * val someElement = ...
     * val minAtkDmg = lazyMinAttackDamage(someElement)
     * ```
     *
     * @param attribute the function used to specify the element attribute
     * @return a function that returns the specific element attribute
     * @receiver the container of the specific element attribute
     */
    fun lazyElement(attribute: ElementAttributeContainer.() -> ElementAttribute): (Element) -> ElementAttribute {
        return { element -> byElement(element).attribute() }
    }

    /**
     * Gets all [Attribute.facadeId] of the attributes that are backed by vanilla game.
     */
    val VANILLA_ATTRIBUTE_NAMES: Collection<String> = AttributeContainerSupport.VANILLA_ATTRIBUTE_NAMES

    /**
     * Gets all [Attribute.facadeId] of the element attributes.
     */
    val ELEMENT_ATTRIBUTE_NAMES: Collection<String> = AttributeContainerSupport.ELEMENT_ATTRIBUTE_NAMES
}

/**
 * A container which owns all [ElementAttribute] instances for an [Element].
 */
class ElementAttributeContainer
internal constructor(
    /**
     * The element type of this container.
     */
    element: Element,
) : AttributeContainer<ElementAttribute> {
    private val BY_FACADE_ID: Multimap<String, ElementAttribute> = MultimapBuilder.hashKeys().linkedHashSetValues().build()
    private fun register(attribute: ElementAttribute): ElementAttribute {
        BY_FACADE_ID.put(attribute.facadeId, attribute)

        if (attribute.vanilla) {
            AttributeContainerSupport.VANILLA_ATTRIBUTE_NAMES += attribute.facadeId
        }

        AttributeContainerSupport.ELEMENT_ATTRIBUTE_NAMES += attribute.facadeId

        return attribute
    }

    override fun byFacade(facadeId: String): Collection<ElementAttribute> {
        return BY_FACADE_ID.get(facadeId)
    }

    //<editor-fold desc="Types">
    val DEFENSE = ElementAttribute("defense", .0, -16384.0, 16384.0, element).apply(::register)
    val DEFENSE_PENETRATION = ElementAttribute("defense_penetration", .0, -16384.0, 16384.0, element).apply(::register)
    val DEFENSE_PENETRATION_RATE = ElementAttribute("defense_penetration_rate", .0, .0, 1.0, element).apply(::register)
    val MAX_ATTACK_DAMAGE = ElementAttribute("attack_damage", "max_attack_damage", .0, .0, 16384.0, element).apply(::register)
    val MIN_ATTACK_DAMAGE = ElementAttribute("attack_damage", "min_attack_damage", .0, .0, 16384.0, element).apply(::register)
    val ATTACK_DAMAGE_RATE = ElementAttribute("attack_damage_rate", .0, -1.0, 16384.0, element).apply(::register)
    val INCOMING_DAMAGE_RATE = ElementAttribute("incoming_damage_rate", .0, -1.0, 16384.0,element).apply(::register)
    //</editor-fold>
}

/**
 * The support of [AttributeContainer].
 */
private object AttributeContainerSupport {
    val VANILLA_ATTRIBUTE_NAMES: MutableSet<String> = ConcurrentHashMap.newKeySet()
    val ELEMENT_ATTRIBUTE_NAMES: MutableSet<String> = ConcurrentHashMap.newKeySet()
}