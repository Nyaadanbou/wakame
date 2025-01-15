package cc.mewcraft.wakame.ability.factory

import cc.mewcraft.wakame.ability.Ability
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

/**
 * Represents a prototype of a certain ability.
 *
 * Example:
 *
 * ```kotlin
 * object MyAbility : AbilityFactory {
 *    override fun create(config: ConfigProvider, key: Key): Ability {
 *       return DefaultMyAbility(config.entry("myProperty"))
 *    }
 * }
 *
 * private class DefaultMyAbility(override val myProperty: String) : Ability
 * ```
 */
interface AbilityFactory {
    /**
     * Create a new instance of the ability base on a certain ability type
     *
     * @param key The key of the ability. See [Ability.key]
     * @param config The configuration node of the ability.
     */
    fun create(key: Key, config: ConfigurationNode): Ability
}