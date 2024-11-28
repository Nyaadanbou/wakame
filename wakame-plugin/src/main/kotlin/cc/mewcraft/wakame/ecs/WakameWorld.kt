package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.skill2.system.MechanicCooldownSystem
import cc.mewcraft.wakame.ecs.system.RemoveSystem
import cc.mewcraft.wakame.ecs.system.TickCountSystem
import cc.mewcraft.wakame.skill2.system.MechanicBukkitEntityMetadataSystem
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
        }
    }


    fun tick() {
        instance.update(1f)
    }
}