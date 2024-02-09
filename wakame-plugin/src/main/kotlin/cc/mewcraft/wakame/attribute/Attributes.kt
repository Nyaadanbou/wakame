@file:Suppress("PropertyName")

package cc.mewcraft.wakame.attribute

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
object Attributes {
    /*
       Vanilla-backed Attributes

       These are the attributes that need vanilla ones
       as backend to make them effective in game.
     */

    val MAX_HEALTH = RangedAttribute("max_health", 20.0, 1.0, 16384.0)
    val MAX_ABSORPTION = RangedAttribute("max_absorption", .0, .0, 2048.0)
    val MOVEMENT_SPEED_RATE = RangedAttribute("movement_speed_rate", .0, -1.0, +4.0)
    val BLOCK_INTERACTION_RANGE = RangedAttribute("block_interaction_range", 4.5, 1.0, 64.0)
    val ENTITY_INTERACTION_RANGE = RangedAttribute("entity_interaction_range", 3.0, 1.0, 64.0)

    /*
       Independent Attributes

       These are the attributes that need to be implemented
       on our own to make them effective in game. By "on our own",
       we mean that we will implement them by listeners or schedulers.
     */

    val DEFENSE = RangedAttribute("defense", .0, -16384.0, +16384.0)
    val DEFENSE_PENETRATION = RangedAttribute("defense_penetration", .0, -16384.0, +16384.0)
    val DEFENSE_PENETRATION_RATE = RangedAttribute("defense_penetration_rate", .0, -1.0, +1.0)
    val DAMAGE_TAKEN_RATE = RangedAttribute("damage_taken_rate", 1.0, .0, 5.0)

    // Possible values for ATTACK_SPEED_LEVEL are finite: i = 0 ~ 8 (9 states), where i is an int
    val ATTACK_SPEED_LEVEL = RangedAttribute("attack_speed_level", .0, .0, 8.0)

    val ATTACK_EFFECT_CHANCE = RangedAttribute("attack_effect_chance", 0.01, .0, 1.0)
    val CRITICAL_STRIKE_CHANCE = RangedAttribute("critical_strike_chance", 0.01, .0, 1.0)
    val CRITICAL_STRIKE_POWER = RangedAttribute("critical_strike_power", 1.0, .0, 5.0)
    val LIFESTEAL = RangedAttribute("lifesteal", .0, .0, 1.0)
    val LIFESTEAL_RATE = RangedAttribute("lifesteal_rate", .0, .0, 16384.0)
    val MANASTEAL = RangedAttribute("manasteal", .0, .0, 1.0)
    val MANASTEAL_RATE = RangedAttribute("manasteal_rate", .0, .0, 16384.0)
    val HEALTH_REGENERATION = RangedAttribute("health_regeneration", 1.0, 0.0, 16384.0)
    val MANA_CONSUMPTION_RATE = RangedAttribute("mana_consumption_rate", 1.0, .0, 5.0)
    val MANA_REGENERATION = RangedAttribute("mana_regeneration", 1.0, .0, 16384.0)
    val MAX_MANA = RangedAttribute("max_mana", 100.0, 0.0, 16384.0)

    /*
       Element-backed Attributes

       These are the attributes related to the **Element Mechanic**.
       Each of these attribute is associated with a certain element.
     */

    // Use CHM to allow concurrent invocations
    private val elementAttributeMap: ConcurrentHashMap<Element, ElementAttributes> = ConcurrentHashMap()

    /**
     * Gets an [element attribute][ElementAttribute] by the specific [element].
     *
     * Note that this function does not directly give you a
     * [element attribute]. Instead, you first get a "holder" that holds all
     * the [element attribute][ElementAttribute] instances for the specific
     * [element]. Then, you get the [element attribute][ElementAttribute]
     * by accessing the properties of the [ElementAttribute].
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
     * Lazily gets an [element attribute][ElementAttribute]. You can use
     * this function at the time when you are required to specify an
     * [ElementAttribute] but the specific element is unknown.
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
 * A holder that holds all default [ElementAttribute] instances for an
 * [Element].
 */
class ElementAttributes(
    element: Element,
) {
    // Notes: by design, ELEMENT DEFENSE is only used by monsters.
    val DEFENSE = ElementAttribute(element, "element_defense", DEF_DEFENSE, MIN_DEFENSE, MAX_DEFENSE)

    val MIN_ATTACK_DAMAGE = ElementAttribute(element, "element_min_attack_damage", DEF_MIN_ATTACK_DAMAGE, MIN_MIN_ATTACK_DAMAGE, MAX_MIN_ATTACK_DAMAGE)
    val MAX_ATTACK_DAMAGE = ElementAttribute(element, "element_max_attack_damage", DEF_MAX_ATTACK_DAMAGE, MIN_MAX_ATTACK_DAMAGE, MAX_MAX_ATTACK_DAMAGE)

    @Deprecated("Remove it as all attributes now support independent AttributeModifier.Operation")
    val ATTACK_DAMAGE_RATE = ElementAttribute(element, "element_attack_damage_rate", DEF_ATTACK_DAMAGE_RATE, MIN_ATTACK_DAMAGE_RATE, MAX_ATTACK_DAMAGE_RATE)

    companion object Defaults {
        const val DEF_DEFENSE = .0
        const val MIN_DEFENSE = -16384.0
        const val MAX_DEFENSE = +16384.0

        const val DEF_MIN_ATTACK_DAMAGE = .0
        const val MIN_MIN_ATTACK_DAMAGE = .0
        const val MAX_MIN_ATTACK_DAMAGE = 16384.0

        const val DEF_MAX_ATTACK_DAMAGE = .0
        const val MIN_MAX_ATTACK_DAMAGE = .0
        const val MAX_MAX_ATTACK_DAMAGE = 16384.0

        const val DEF_ATTACK_DAMAGE_RATE = +1.0
        const val MIN_ATTACK_DAMAGE_RATE = -1.0
        const val MAX_ATTACK_DAMAGE_RATE = +5.0
    }
}
