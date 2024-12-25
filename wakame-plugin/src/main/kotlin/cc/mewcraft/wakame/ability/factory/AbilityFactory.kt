package cc.mewcraft.wakame.ability.factory

import cc.mewcraft.wakame.ability.Ability
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

/**
 * Represents a factory of a certain ability.
 *
 * How to create a new ability type:
 * 1. Create a new interface that extends [Ability] interface.
 * 2. Create a companion object that implements [AbilityFactory] interface with the type of the ability.
 * 3. Implement the [create] method to create a new instance of the ability.
 * 4. Register the ability type in the [AbilityFactories]
 *
 * Example:
 *
 * ```kotlin
 * interface MyAbility : Ability {
 *    val myProperty: String
 *
 *    companion object Factory : AbilityFactory<MyAbility> {
 *        override fun create(config: ConfigProvider, key: Key): MyAbility {
 *            return DefaultMyAbility(config.entry("myProperty"))
 *        }
 *    }
 *
 *    private class DefaultMyAbility(override val myProperty: String) : MyAbility
 * }
 * ```
 *
 * @param T The type of the ability that this ability type creates.
 */
interface AbilityFactory<T : Ability> {
    /**
     * Create a new instance of the ability base on a certain ability type
     *
     * @param key The key of the ability. See [Ability.key]
     * @param config The configuration node of the ability.
     */
    fun create(key: Key, config: ConfigurationNode): T
}