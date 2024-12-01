package cc.mewcraft.wakame.skill2

import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.BukkitEntityComponent
import cc.mewcraft.wakame.ecs.component.EntityType
import cc.mewcraft.wakame.ecs.component.ResultComponent
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.skill2.character.Caster
import cc.mewcraft.wakame.skill2.character.CasterUtils
import cc.mewcraft.wakame.skill2.context.SkillContext

/**
 * 用于内部记录一个技能状态.
 */
internal class MechanicRecorder(
    private val world: WakameWorld,
) {
    /**
     * 添加一个 [Skill] 状态.
     */
    fun addMechanic(context: SkillContext) {
        world.createEntity(context.skill.key.asString()) {
            it += EntityType.MECHANIC
            CasterUtils.getCaster<Caster.Single.Entity>(context)?.bukkitEntity?.let { bukkitEntity -> it += BukkitEntityComponent(bukkitEntity) }
            it += ResultComponent(context.skill.result(context))
            it += TickCountComponent(.0)
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
}