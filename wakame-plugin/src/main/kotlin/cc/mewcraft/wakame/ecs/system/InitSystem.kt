package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.component.Tags
import com.github.quillraven.fleks.IntervalSystem

class InitSystem : IntervalSystem() {
    override fun onTick() {
        world.forEach { entity ->
            entity.configure {
                it -= Tags.READY_TO_REMOVE
            }
        }
    }
}