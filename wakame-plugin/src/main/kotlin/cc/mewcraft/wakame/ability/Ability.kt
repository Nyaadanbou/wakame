package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.ability.context.AbilityInput
import cc.mewcraft.wakame.ability.display.AbilityDisplay
import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.ecs.Mechanic
import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.BukkitBridgeComponent
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.HoldBy
import cc.mewcraft.wakame.ecs.component.MechanicComponent
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.component.TargetTo
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.util.adventure.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.util.stream.Stream

/**
 * Represents a ability "attached" to a player.
 *
 * If a player has ability X, we say that the ability X is attached to that
 * player; By contrast, if the player has no ability, we say that the player
 * has no ability attached.
 */
abstract class Ability(
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
    final override val key: Key,
    config: ConfigurationNode,
) : Keyed, Examinable {
    /**
     * The display infos of this ability.
     */
    val displays: AbilityDisplay = config.node("displays").get<AbilityDisplay>() ?: AbilityDisplay()

    /**
     * 添加一个 [Ability] 状态.
     */
    private fun addMechanic(input: AbilityInput) {
        WakameWorld.createEntity(key.asString()) {
            it += AbilityComponent(
                manaCost = input.manaCost,
                penalty = ManaCostPenalty(),
                phase = StatePhase.IDLE,
                trigger = input.trigger,
                mochaEngine = input.mochaEngine
            )
            it += Tags.DISPOSABLE
            it += MechanicComponent(mechanic(input))
            input.castBy?.let { castBy ->
                it += CastBy(castBy)
                it += BukkitBridgeComponent(castBy.entity)
            }
            it += TargetTo(input.targetTo)
            HoldBy(input.holdBy)?.let { holdBy -> it += holdBy }
            input.holdBy?.let { castItem -> it += HoldBy(slot = castItem.first, nekoStack = castItem.second.clone()) }
            it += TickCountComponent(.0)
        }
    }

    /**
     * 使用 [input] 记录技能的信息到 ECS 中.
     *
     * 具体是先调用 [mechanic], 再将返回结果添加到 ECS 中.
     */
    fun recordBy(input: AbilityInput) {
        addMechanic(input)
    }

    /**
     * 返回一个技能执行的结果, 只有执行 [Mechanic] 才会真正执行技能逻辑.
     *
     * @see Mechanic
     * @see ActiveAbilityMechanic
     * @see PassiveAbilityMechanic
     */
    abstract fun mechanic(input: AbilityInput): Mechanic

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("key", key),
            ExaminableProperty.of("displays", displays),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Ability) return false

        return key == other.key
    }
}