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
    fun getMechanicsBy(trigger: Trigger): List<Skill> {
        val skills = mutableListOf<Skill>()
        with(world.instance) {
            forEach { entity ->
                val family = family { all(EntityType.SKILL, TriggerComponent, IdentifierComponent) }
                if (!family.contains(entity))
                    return@forEach
                if (entity[TriggerComponent].trigger == trigger) {
                    val id = entity[IdentifierComponent].id
                    skills.add(SkillRegistry.INSTANCES[Key(id)])
                }
            }
        }

        return skills
    }

    fun getAllActiveMechanic(bukkitEntity: Entity): Set<Skill> {
        val skills = mutableSetOf<Skill>()
        with(world.instance) {
            forEach { entity ->
                val family = family { all(EntityType.SKILL, CasterComponent, IdentifierComponent) }
                if (!family.contains(entity))
                    return@forEach
                if (entity[CasterComponent].caster == bukkitEntity) {
                    skills.add(SkillRegistry.INSTANCES[Key(entity[IdentifierComponent].id)])
                }
            }
        }

        return skills
    }

    fun getAllActiveMechanicTriggers(bukkitEntity: Entity): Set<Trigger> {
        val triggers = mutableSetOf<Trigger>()
        with(world.instance) {
            forEach { entity ->
                val family = family { all(EntityType.SKILL, CasterComponent, TriggerComponent) }
                if (!family.contains(entity))
                    return@forEach
                if (entity[CasterComponent].entity == bukkitEntity) {
                    triggers.add(entity[TriggerComponent].trigger)
                }
            }
        }

        return triggers
    }

    /**
     * 中断一个 [Skill].
     */
    fun interruptMechanicBy(skill: Skill) {
        interruptMechanicBy(skill.key.asString())
    }

    /**
     * 中断一个指定 [trigger] 的 [Skill].
     */
    fun interruptMechanicBy(trigger: Trigger, skill: Skill) {
        with(world.instance) {
            forEach { entity ->
                val family = family { all(EntityType.SKILL, TriggerComponent, IdentifierComponent) }
                if (!family.contains(entity))
                    return@forEach
                if (entity[TriggerComponent].trigger == trigger && entity[IdentifierComponent].id == skill.key.asString()) {
                    world.removeEntity(entity)
                }
            }
        }
    }

    /**
     * 中断一个 [Skill], 使用 [Skill.key].
     */
    fun interruptMechanicBy(identifier: String) {
        world.removeEntity(identifier)
    }

    /**
     * 中断一个 [Skill], 使用 [Entity].
     */
    fun interruptMechanicBy(bukkitEntity: Entity) {
        with(world.instance) {
            family { all(EntityType.SKILL, CasterComponent) }
                .forEach {
                    if (it[CasterComponent].caster.uniqueId == bukkitEntity.uniqueId) {
                        world.removeEntity(it)
                    }
                }
        }
    }

    fun markNextState(bukkitEntity: Entity, phase: StatePhase, skills: Collection<Skill>) {
        world.editEntities(
            family = { family { all(EntityType.SKILL, IdentifierComponent, CasterComponent, StatePhaseComponent) } }
        ) { entity ->
            if (entity[CasterComponent].entity != bukkitEntity)
                return@editEntities
            if (entity[StatePhaseComponent].phase != phase)
                return@editEntities
            if (entity[IdentifierComponent].id !in skills.map { it.key.asString() })
                return@editEntities
            entity += Tags.CAN_NEXT_STATE
        }
    }
}