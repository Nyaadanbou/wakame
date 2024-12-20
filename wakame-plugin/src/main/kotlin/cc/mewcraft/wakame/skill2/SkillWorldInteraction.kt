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

    fun getAllActiveSkill(bukkitEntity: Entity): Set<Skill> {
        val skills = mutableSetOf<Skill>()
        with(world.instance) {
            forEach { entity ->
                val family = family { all(EntityType.SKILL, CastBy, IdentifierComponent) }
                if (!family.contains(entity))
                    return@forEach
                if (entity[CastBy].caster == bukkitEntity) {
                    skills.add(SkillRegistry.INSTANCES[Key(entity[IdentifierComponent].id)])
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

    /**
     * 中断一个指定 [bukkitEntity] 的 [Skill].
     */
    fun interruptSkillBy(bukkitEntity: Entity, skill: Skill) {
        interruptSkillBy(bukkitEntity, skill.key.asString())
    }

    /**
     * 中断一个指定 [bukkitEntity] 的 [Skill]. 并且由 [trigger] 触发.
     */
    fun interruptSkillBy(bukkitEntity: Entity, trigger: Trigger, skill: Skill) {
        with(world.instance) {
            forEach { entity ->
                val family = family { all(EntityType.SKILL, CastBy, TriggerComponent, IdentifierComponent) }
                if (!family.contains(entity))
                    return@forEach
                if (entity[CastBy].entity != bukkitEntity) {
                    return@forEach
                }
                if (entity[TriggerComponent].trigger != trigger) {
                    return@forEach
                }
                if (entity[IdentifierComponent].id != skill.key.asString()) {
                    return@forEach
                }
                world.removeEntity(entity)
            }
        }
    }

    /**
     * 中断指定 [bukkitEntity] [identifier] 的 [Skill].
     */
    fun interruptSkillBy(bukkitEntity: Entity, identifier: String) {
        with(world.instance) {
            family { all(EntityType.SKILL, CastBy, IdentifierComponent) }
                .forEach {
                    if (it[CastBy].entity != bukkitEntity)
                        return@forEach
                    if (it[IdentifierComponent].id != identifier)
                        return@forEach
                    world.removeEntity(it)
                }
        }
    }

    /**
     * 中断所有 [Entity] 下的 [Skill].
     */
    fun interruptSkillBy(bukkitEntity: Entity) {
        with(world.instance) {
            family { all(EntityType.SKILL, CastBy) }
                .forEach {
                    if (it[CastBy].caster.uniqueId != bukkitEntity.uniqueId)
                        return@forEach
                    world.removeEntity(it)
                }
        }
    }

    fun markNextState(bukkitEntity: Entity, skills: Collection<Skill>) {
        world.editEntities(
            family = { family { all(EntityType.SKILL, IdentifierComponent, CastBy, StatePhaseComponent) } }
        ) { entity ->
            if (entity[CastBy].entity != bukkitEntity)
                return@editEntities
            if (entity[StatePhaseComponent].phase != StatePhase.IDLE)
                // 只有在 IDLE 状态下才能进行下一个状态的标记.
                return@editEntities
            if (entity[IdentifierComponent].id !in skills.map { it.key.asString() })
                return@editEntities
            entity += Tags.CAN_NEXT_STATE
        }
    }
}