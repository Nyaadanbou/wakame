@file:Suppress("PropertyName")

package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.element.Element
import java.util.concurrent.ConcurrentHashMap

/**
 * A holder that holds all default [Attribute] instances.
 *
 * The attribute instances in this singleton object are primarily served as
 * "lookup index" for other code in this project. The given numeric values
 * of a certain attribute are decided by the most common default values
 * among all the attributes of the same type.
 */
@OptIn(InternalApi::class)
object Attributes {
    /**
     * An empty attribute that does nothing on its own.
     *
     * It only serves as a special value for other systems to use.
     */
    val EMPTY = Attribute("empty", defaultValue = 0.0)

    /*
       Vanilla-backed Attributes

       These are the attributes that need vanilla ones
       as backend to make them effective in game.
     */

    val MAX_HEALTH by createRangedAttribute("max_health", true, 20.0, 1.0, 16384.0)
    val MAX_ABSORPTION by createRangedAttribute("max_absorption", true, .0, .0, 2048.0)
    val MOVEMENT_SPEED by createRangedAttribute("movement_speed", true, .0, -1.0, +4.0)
    val BLOCK_INTERACTION_RANGE by createRangedAttribute("block_interaction_range", true, 4.5, 1.0, 64.0)
    val ENTITY_INTERACTION_RANGE by createRangedAttribute("entity_interaction_range", true, 3.0, 1.0, 64.0)

    /*
       Independent Attributes

       These are the attributes that need to be implemented
       on our own to make them effective in game. By "on our own",
       we mean that we will implement them by listeners or schedulers.
     */

    val DAMAGE_REDUCTION_RATE by createRangedAttribute("damage_reduction_rate", .0, .0, 1.0)
    val ATTACK_SPEED_LEVEL by createRangedAttribute("attack_speed_level", .0, .0, 8.0) // !!! can only take integers
    val ATTACK_EFFECT_CHANCE by createRangedAttribute("attack_effect_chance", 0.01, .0, 1.0)
    val CRITICAL_STRIKE_CHANCE by createRangedAttribute("critical_strike_chance", 0.01, .0, 1.0)
    val CRITICAL_STRIKE_POWER by createRangedAttribute("critical_strike_power", 1.0, .0, 5.0)
    val LIFESTEAL by createRangedAttribute("lifesteal", .0, .0, 1.0)
    val LIFESTEAL_RATE by createRangedAttribute("lifesteal_rate", .0, .0, 16384.0)
    val MANASTEAL by createRangedAttribute("manasteal", .0, .0, 1.0)
    val MANASTEAL_RATE by createRangedAttribute("manasteal_rate", .0, .0, 16384.0)
    val HEALTH_REGENERATION by createRangedAttribute("health_regeneration", 1.0, 0.0, 16384.0)
    val MANA_CONSUMPTION_RATE by createRangedAttribute("mana_consumption_rate", 1.0, .0, 5.0)
    val MANA_REGENERATION by createRangedAttribute("mana_regeneration", 1.0, .0, 16384.0)
    val MAX_MANA by createRangedAttribute("max_mana", 100.0, 0.0, 16384.0)

    /*
       Element-backed Attributes

       These are the attributes related to the **Element Mechanic**.
       Each of these attribute is associated with a certain element.
     */

    private val elementAttributeMap: ConcurrentHashMap<Element, ElementAttributes> = ConcurrentHashMap()

    /**
     * Gets an [element attribute][createElementAttribute] by the specific [element].
     *
     * Note that this function does not directly give you a
     * [element attribute]. Instead, you first get a "holder" that holds all
     * the [element attribute][createElementAttribute] instances for the specific
     * [element]. Then, you get the [element attribute][createElementAttribute]
     * by accessing the properties of the [createElementAttribute].
     *
     * Example usage:
     * ```kotlin
     * val neutralElement = ...
     * val minAttackDamage = Attributes.byElement(neutralElement).MIN_ATTACK_DAMAGE
     * ```
     *
     * @param element the element of the attribute you want
     * @return the holder of the specific element attribute you want
     */
    fun byElement(element: Element): ElementAttributes {
        return elementAttributeMap.computeIfAbsent(element) {
            ElementAttributes(element)
        }
    }

    /**
     * Lazily gets an [element attribute][createElementAttribute]. You can use
     * this function at the time when you are required to specify an
     * [createElementAttribute] but the specific element is unknown.
     *
     * Example usage:
     * ```
     * val lazyMinAttackDamage = Attributes.byElement { MIN_ATTACK_DAMAGE }
     * // Some other code that knows the element
     * val neutralElement = ...
     * val minAttackDamage = lazyMinAttackDamage(neutralElement)
     * ```
     *
     * @param attribute the function used to specify the element attribute
     * @return a function that returns the specific element attribute
     * @receiver the holder of the specific element attribute
     */
    fun byElement(attribute: ElementAttributes.() -> ElementAttribute): (Element) -> ElementAttribute {
        return { element -> byElement(element).attribute() }
    }
}

/**
 * A holder that holds all default [createElementAttribute] instances for an
 * [Element].
 */
@OptIn(InternalApi::class)
class ElementAttributes
@InternalApi internal constructor(
    element: Element,
) {
    val DEFENSE by createElementAttribute("defense", .0, -16384.0, +16384.0, element)
    val DEFENSE_PENETRATION by createElementAttribute("defense_penetration", .0, -16384.0, +16384.0, element)
    val DEFENSE_PENETRATION_RATE by createElementAttribute("defense_penetration_rate", .0, -1.0, +1.0, element)
    val MIN_ATTACK_DAMAGE by createElementAttribute("min_attack_damage", .0, .0, 16384.0, element)
    val MAX_ATTACK_DAMAGE by createElementAttribute("max_attack_damage", .0, .0, 16384.0, element)
}
