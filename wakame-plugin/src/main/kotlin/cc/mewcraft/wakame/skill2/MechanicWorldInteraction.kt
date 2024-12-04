package cc.mewcraft.wakame.skill2

import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.BukkitEntityComponent
import cc.mewcraft.wakame.ecs.component.EntityType
import cc.mewcraft.wakame.ecs.component.ResultComponent
import cc.mewcraft.wakame.ecs.component.StatePhaseComponent
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.skill2.character.Caster
import cc.mewcraft.wakame.skill2.character.CasterUtils
import cc.mewcraft.wakame.skill2.context.SkillContext
import com.github.quillraven.fleks.World.Companion.family
import org.bukkit.entity.Entity

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
            it += EntityType.MECHANIC
            CasterUtils.getCaster<Caster.Single.Entity>(context)?.bukkitEntity?.let { bukkitEntity -> it += BukkitEntityComponent(bukkitEntity.uniqueId) }
            it += ResultComponent(context.skill.result(context))
            it += StatePhaseComponent(StatePhase.IDLE)
            it += TickCountComponent(.0)
            it += Tags.CAN_TICK
        }
    }

    /**
     * 中断一个 [Skill].
     */
    fun interruptMechanic(skill: Skill) {
        interruptMechanic(skill.key.asString())
    }

    /**
     * 中断一个 [Skill], 使用 [Skill.key].
     */
    fun interruptMechanic(identifier: String) {
        world.removeEntity(identifier)
    }

    fun setState(bukkitEntity: Entity, phase: StatePhase) {
        world.editEntities(
            family = family { all(BukkitEntityComponent, StatePhaseComponent) }
        ) { entity ->
            if (entity[BukkitEntityComponent].entity != bukkitEntity)
                return@editEntities
            entity[StatePhaseComponent].phase = phase
        }
    }
}