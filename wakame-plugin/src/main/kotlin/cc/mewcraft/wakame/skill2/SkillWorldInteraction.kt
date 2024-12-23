package cc.mewcraft.wakame.skill2

import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.*
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill2.trigger.Trigger
import cc.mewcraft.wakame.util.Key
import org.bukkit.entity.Entity

/**
 * 技能与 ECS 系统交互的工具类.
 */
internal class SkillWorldInteraction(
    private val world: WakameWorld,
) {
    fun getMechanicsBy(bukkitEntity: Entity, trigger: Trigger): List<Skill> {
        val skills = mutableListOf<Skill>()
        with(world.instance) {
            forEach { entity ->
                val family = family { all(EntityType.SKILL, CastBy, TriggerComponent, IdentifierComponent) }
                if (!family.contains(entity))
                    return@forEach
                if (entity[CastBy].entity != bukkitEntity)
                    return@forEach
                if (entity[TriggerComponent].trigger == trigger) {
                    val id = entity[IdentifierComponent].id
                    skills.add(SkillRegistry.INSTANCES[Key(id)])
                }
            }
        }

        return skills
    }

    fun getAllActiveSkillTriggers(bukkitEntity: Entity): Set<Trigger> {
        val triggers = mutableSetOf<Trigger>()
        with(world.instance) {
            forEach { entity ->
                val family = family { all(EntityType.SKILL, CastBy, TriggerComponent) }
                if (!family.contains(entity))
                    return@forEach
                if (entity[CastBy].entity != bukkitEntity)
                    return@forEach
                triggers.add(entity[TriggerComponent].trigger)
            }
        }

        return triggers
    }

    fun setNextState(bukkitEntity: Entity, skill: Skill) {
        world.editEntities(
            family = { family { all(EntityType.SKILL, IdentifierComponent, CastBy, StatePhaseComponent, TickResultComponent) } }
        ) { entity ->
            if (entity[CastBy].entity != bukkitEntity)
                return@editEntities
            if (entity[StatePhaseComponent].phase != StatePhase.IDLE)
                // 只有在 IDLE 状态下才能进行下一个状态的标记.
                return@editEntities
            if (entity[IdentifierComponent].id != skill.key.asString())
                return@editEntities
            entity.configure { it += Tags.NEXT_STATE }
        }
    }

    fun setCostPenalty(bukkitEntity: Entity, skillId: String, penalty: ManaCostPenalty) {
        world.editEntities(
            family = { family { all(EntityType.SKILL, IdentifierComponent, CastBy, MochaEngineComponent) } }
        ) { entity ->
            if (entity[CastBy].entity != bukkitEntity)
                return@editEntities
            if (entity[IdentifierComponent].id != skillId)
                return@editEntities
            entity[ManaCostComponent].penalty = penalty
        }
    }
}