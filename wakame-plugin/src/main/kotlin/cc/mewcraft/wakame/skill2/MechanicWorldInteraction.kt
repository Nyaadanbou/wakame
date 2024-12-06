package cc.mewcraft.wakame.skill2

import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.*
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill2.character.Caster
import cc.mewcraft.wakame.skill2.character.CasterUtils
import cc.mewcraft.wakame.skill2.context.SkillContext
import cc.mewcraft.wakame.skill2.state.StateInfo
import cc.mewcraft.wakame.skill2.trigger.Trigger
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

/**
 * 用于内部记录一个技能状态.
 */
internal class MechanicWorldInteraction(
    private val world: WakameWorld,
) {
    /**
     * 添加一个 [Skill] 状态.
     */
    fun addMechanic(context: SkillContext) {
        world.createEntity(context.skill.key.asString()) {
            it += CooldownComponent(context.cooldown)
            it += EntityType.MECHANIC
            CasterUtils.getCaster<Caster.Single.Entity>(context)?.bukkitEntity?.let { bukkitEntity -> it += BukkitEntityComponent(bukkitEntity.uniqueId) }
            it += ResultComponent(context.skill.result(context))
            it += StatePhaseComponent(StatePhase.IDLE)
            it += TickCountComponent(.0)
            it += TriggerComponent(context.trigger)
            it += Tags.CAN_TICK
        }
    }

    fun getMechanicsBy(trigger: Trigger): List<Skill> {
        val skills = mutableListOf<Skill>()
        with(world.instance) {
            forEach { entity ->
                val family = family { all(TriggerComponent, IdentifierComponent) }
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

    fun getAllActiveMechanicTriggers(bukkitEntity: Entity): Set<Trigger> {
        val triggers = mutableSetOf<Trigger>()
        with(world.instance) {
            forEach { entity ->
                val family = family { all(BukkitEntityComponent, TriggerComponent) }
                if (!family.contains(entity))
                    return@forEach
                if (entity[BukkitEntityComponent].entity == bukkitEntity) {
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
                val family = family { all(TriggerComponent, IdentifierComponent) }
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
            family { all(BukkitEntityComponent) }
                .forEach {
                    if (it[BukkitEntityComponent].entityUniqueId == bukkitEntity.uniqueId) {
                        world.removeEntity(it)
                    }
                }
        }
    }

    fun getStates(bukkitEntity: Entity): Map<Key, StatePhase> {
        val states = mutableMapOf<Key, StatePhase>()
        with(world.instance) {
            family { all(IdentifierComponent, StatePhaseComponent, BukkitEntityComponent) }
                .forEach { entity ->
                    if (entity[BukkitEntityComponent].entity != bukkitEntity)
                        return@forEach
                    states[Key(entity[IdentifierComponent].id)] = entity[StatePhaseComponent].phase
                }
        }

        return states
    }

    fun getStateInfos(player: Player): Map<Key, StateInfo> {
        val stateInfos = mutableMapOf<Key, StateInfo>()
        with(world.instance) {
            family { all(IdentifierComponent, StatePhaseComponent, BukkitEntityComponent, StateInfoComponent) }
                .forEach { entity ->
                    if (entity[BukkitEntityComponent].entity != player)
                        return@forEach
                    stateInfos[Key(entity[IdentifierComponent].id)] = entity[StateInfoComponent].stateInfo
                }
        }

        return stateInfos
    }

    fun markNextState(bukkitEntity: Entity) {
        world.editEntities(
            family = { family { all(BukkitEntityComponent, StatePhaseComponent) } }
        ) { entity ->
            if (entity[BukkitEntityComponent].entity != bukkitEntity)
                return@editEntities
            entity += Tags.CAN_NEXT_STATE
        }
    }
}