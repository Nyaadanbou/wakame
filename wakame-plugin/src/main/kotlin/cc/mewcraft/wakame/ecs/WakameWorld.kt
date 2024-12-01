package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.Remove
import cc.mewcraft.wakame.ecs.system.ComponentMapSystem
import cc.mewcraft.wakame.skill2.system.MechanicCooldownSystem
import cc.mewcraft.wakame.ecs.system.RemoveSystem
import cc.mewcraft.wakame.ecs.system.ResultSystem
import cc.mewcraft.wakame.ecs.system.TickCountSystem
import cc.mewcraft.wakame.skill2.system.MechanicBukkitEntityMetadataSystem
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld

class WakameWorld(
    private val plugin: WakamePlugin
) {
    val instance: World = configureWorld {

        injectables {
            add(plugin)
        }

        systems {
            add(RemoveSystem())
            add(TickCountSystem())
            add(MechanicBukkitEntityMetadataSystem())
            add(MechanicCooldownSystem())
            add(ResultSystem())
            add(ComponentMapSystem())
        }
    }


    fun tick() {
        instance.update(1f)
    }

    fun createEntity(identifier: String, configuration: EntityCreateContext.(Entity) -> Unit = {}) {
        instance.entity {
            it += IdentifierComponent(identifier)
            configuration.invoke(this, it)
        }
    }

    fun removeEntity(identifier: String) {
        with(instance) {
            val entityToRemove = family { all(IdentifierComponent) }
                .firstOrNull { it[IdentifierComponent].id == identifier } ?: return
            entityToRemove.configure {
                it += Remove
            }
        }
    }
}