package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.ability.context.AbilityInput
import cc.mewcraft.wakame.ability.display.AbilityDisplay
import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.ecs.Mechanic
import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.util.typeTokenOf
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.spongepowered.configurate.serialize.ScalarSerializer
import java.lang.reflect.Type
import java.util.function.Predicate

/**
 * Represents a ability "attached" to a player.
 *
 * If a player has ability X, we say that the ability X is attached to that
 * player; By contrast, if the player has no ability, we say that the player
 * has no ability attached.
 */
interface Ability : Keyed, Examinable {

    /**
     * The key of this ability.
     *
     * **Note that the [key] here is specified by the location of the ability
     * configuration file, not the [cc.mewcraft.wakame.ability.archetype.AbilityArchetypes]'s key**,
     * which means that a [cc.mewcraft.wakame.ability.archetype.AbilityArchetype] can have multiple [Ability].
     *
     * [Ability] will be stored in the AbilityRegistry, and the corresponding
     * [Ability] will be found by the [key].
     */
    override val key: Key

    /**
     * The display infos of this ability.
     */
    val displays: AbilityDisplay

    /**
     * 使用 [input] 记录技能的信息到 ECS 中.
     *
     * 具体是先调用 [mechanic], 再将返回结果添加到 ECS 中.
     */
    fun recordBy(input: AbilityInput)

    /**
     * 返回一个技能执行的结果, 只有执行 [Mechanic] 才会真正执行技能逻辑.
     *
     * @see Mechanic
     * @see ActiveAbilityMechanic
     * @see PassiveAbilityMechanic
     */
    fun mechanic(input: AbilityInput): Mechanic

    companion object {
        /**
         * An empty ability.
         */
        fun empty(): Ability = EmptyAbility
    }
}

private data object EmptyAbility : Ability {
    override val key: Key = Key.key(Namespaces.ABILITY, "empty")
    override val displays: AbilityDisplay = AbilityDisplay.empty()
    override fun recordBy(input: AbilityInput) = Unit
    override fun mechanic(input: AbilityInput): Mechanic = EmptyAbilityMechanic
}

internal object AbilitySerializer : ScalarSerializer<AbilityProvider>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): AbilityProvider {
        return AbilityProvider(Key.key(obj.toString()))
    }

    override fun serialize(item: AbilityProvider, typeSupported: Predicate<Class<*>>?): Any {
        throw UnsupportedOperationException()
    }
}