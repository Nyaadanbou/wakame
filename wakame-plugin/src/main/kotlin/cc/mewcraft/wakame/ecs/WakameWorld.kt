package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.ecs.system.CooldownSystem
import cc.mewcraft.wakame.ecs.system.RemoveSystem
import cc.mewcraft.wakame.ecs.system.TimeSystem
import cc.mewcraft.wakame.skill2.system.SkillBukkitEntityMetadataSystem
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
            add(TimeSystem())
            add(SkillBukkitEntityMetadataSystem())
            add(CooldownSystem())
        }
    }


    fun tick() {
        instance.update(1f)
    }
}