package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.*
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.registry.AbilityRegistry
import cc.mewcraft.wakame.ability.trigger.Trigger
import cc.mewcraft.wakame.util.Key
import org.bukkit.entity.Entity

/**
 * 技能与 ECS 系统交互的工具类.
 */
internal class AbilityWorldInteraction(
    private val world: WakameWorld,
) {
    fun getMechanicsBy(bukkitEntity: Entity, trigger: Trigger): List<Ability> {
        val abilities = mutableListOf<Ability>()
        with(world.instance) {
            forEach { entity ->
                val family = family { all(EntityType.ABILITY, CastBy, TriggerComponent, IdentifierComponent) }
                if (!family.contains(entity))
                    return@forEach
                if (entity[CastBy].entity != bukkitEntity)
                    return@forEach
                if (entity[TriggerComponent].trigger == trigger) {
                    val id = entity[IdentifierComponent].id
                    abilities.add(AbilityRegistry.INSTANCES[Key(id)])
                }
            }
        }

        return abilities
    }

    fun getAllActiveAbilityTriggers(bukkitEntity: Entity): Set<Trigger> {
        val triggers = mutableSetOf<Trigger>()
        with(world.instance) {
            forEach { entity ->
                val family = family { all(EntityType.ABILITY, CastBy, TriggerComponent) }
                if (!family.contains(entity))
                    return@forEach
                if (entity[CastBy].entity != bukkitEntity)
                    return@forEach
                triggers.add(entity[TriggerComponent].trigger)
            }
        }

        return triggers
    }

    fun setNextState(bukkitEntity: Entity, ability: Ability) {
        world.editEntities(
            family = { family { all(EntityType.ABILITY, IdentifierComponent, CastBy, StatePhaseComponent, TickResultComponent) } }
        ) { entity ->
            if (entity[CastBy].entity != bukkitEntity)
                return@editEntities
            if (entity[StatePhaseComponent].phase != StatePhase.IDLE)
                // 只有在 IDLE 状态下才能进行下一个状态的标记.
                return@editEntities
            if (entity[IdentifierComponent].id != ability.key.asString())
                return@editEntities
            entity.configure { it += Tags.NEXT_STATE }
        }
    }

    fun setCostPenalty(bukkitEntity: Entity, abilityId: String, penalty: ManaCostPenalty) {
        world.editEntities(
            family = { family { all(EntityType.ABILITY, IdentifierComponent, CastBy, MochaEngineComponent) } }
        ) { entity ->
            if (entity[CastBy].entity != bukkitEntity)
                return@editEntities
            if (entity[IdentifierComponent].id != abilityId)
                return@editEntities
            entity[ManaCostComponent].penalty = penalty
        }
    }
}