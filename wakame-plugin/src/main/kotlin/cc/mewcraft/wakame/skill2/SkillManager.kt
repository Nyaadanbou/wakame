package cc.mewcraft.wakame.skill2

import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.skill2.component.CooldownComponent
import cc.mewcraft.wakame.skill2.component.IdentifierComponent
import cc.mewcraft.wakame.skill2.component.Remove
import cc.mewcraft.wakame.skill2.system.CooldownSystem
import cc.mewcraft.wakame.skill2.system.RemoveSystem
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.configureWorld

class SkillManager(
    private val plugin: WakamePlugin,
) {

    val world: World = configureWorld {

        injectables {
            add(plugin)
        }

        families {
            val family = family {  }
        }

        systems {
            add(RemoveSystem())
            add(CooldownSystem())
        }
    }

    fun addSkill(skill: String) {
        world.entity {
            it += IdentifierComponent(skill)
            it += CooldownComponent()
            world.systems.forEach { t -> t }
        }
    }

    fun removeSkill(skill: String) {
        with(world) {
            val entityToRemove = family { all(IdentifierComponent) }
                .firstOrNull { it[IdentifierComponent].id == skill } ?: return
            entityToRemove.configure {
                it += Remove
            }
        }
    }

    fun tick() {
        world.update(1f)
    }
}