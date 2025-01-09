package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.ability.trigger.Trigger
import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.*
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.registry.AbilityRegistry
import cc.mewcraft.wakame.util.Key
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal inline fun <T> abilityWorldInteraction(block: AbilityWorldInteraction.() -> T): T {
    return AbilityWorldInteraction.block()
}

@DslMarker
internal annotation class AbilityWorldInteractionDsl

/**
 * 技能与 ECS 系统交互的工具类.
 */
@AbilityWorldInteractionDsl
internal object AbilityWorldInteraction : KoinComponent {
    private val wakameWorld: WakameWorld by inject()

    fun Player.getMechanicsBy(trigger: Trigger): List<Ability> {
        val abilities = mutableListOf<Ability>()
        with(WakameWorld.world()) {
            forEach { entity ->
                val family = family { all(EntityType.ABILITY, CastBy, TriggerComponent, IdentifierComponent) }
                if (!family.contains(entity))
                    return@forEach
                if (entity[CastBy].entity != this@getMechanicsBy)
                    return@forEach
                if (entity[TriggerComponent].trigger == trigger) {
                    val id = entity[IdentifierComponent].id
                    abilities.add(AbilityRegistry.INSTANCES[Key.key(id)])
                }
            }
        }

        return abilities
    }

    fun Player.getAllActiveAbilityTriggers(): Set<Trigger> {
        val triggers = mutableSetOf<Trigger>()
        with(WakameWorld.world()) {
            forEach { entity ->
                val family = family { all(EntityType.ABILITY, CastBy, TriggerComponent) }
                if (!family.contains(entity))
                    return@forEach
                if (entity[CastBy].entity != this@getAllActiveAbilityTriggers)
                    return@forEach
                triggers.add(entity[TriggerComponent].trigger)
            }
        }

        return triggers
    }

    fun Player.setNextState(ability: Ability) {
        WakameWorld.editEntities(
            family = { family { all(EntityType.ABILITY, IdentifierComponent, CastBy, StatePhaseComponent, TickResultComponent) } }
        ) { entity ->
            if (entity[CastBy].entity != this@setNextState)
                return@editEntities
            if (entity[StatePhaseComponent].phase != StatePhase.IDLE)
            // 只有在 IDLE 状态下才能进行下一个状态的标记.
                return@editEntities
            if (entity[IdentifierComponent].id != ability.key.asString())
                return@editEntities
            entity.configure { it += Tags.NEXT_STATE }
        }
    }

    fun Player.setCostPenalty(abilityId: String, penalty: ManaCostPenalty) {
        WakameWorld.editEntities(
            family = { family { all(EntityType.ABILITY, IdentifierComponent, CastBy, MochaEngineComponent) } }
        ) { entity ->
            if (entity[CastBy].entity != this@setCostPenalty)
                return@editEntities
            if (entity[IdentifierComponent].id != abilityId)
                return@editEntities
            entity[ManaCostComponent].penalty = penalty
        }
    }

    fun Player.cleanupAbility() {
        wakameWorld.editEntities(
            family = { family { all(EntityType.ABILITY, CastBy) } }
        ) { entity ->
            if (entity[CastBy].entity != this@cleanupAbility)
                return@editEntities
            wakameWorld.removeEntity(entity)
        }
    }
}