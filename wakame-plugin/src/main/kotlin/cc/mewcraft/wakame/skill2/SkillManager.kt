package cc.mewcraft.wakame.skill2

import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.CooldownComponent
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.Remove
import cc.mewcraft.wakame.skill.Skill

class SkillManager(
    private val world: WakameWorld,
) {
    fun addSkill(skill: Skill) {
        world.instance.entity {
            it += IdentifierComponent(skill.key.asString())
            it += CooldownComponent()
        }
    }

    fun removeSkill(skill: Skill) {
        with(world.instance) {
            val entityToRemove = family { all(IdentifierComponent) }
                .firstOrNull { it[IdentifierComponent].id == skill.key.asString() } ?: return
            entityToRemove.configure {
                it += Remove
            }
        }
    }
}