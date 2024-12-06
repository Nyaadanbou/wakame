package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.component.Tags
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family

class InitSystem : IteratingSystem(
    family = family { any(Tags.CAN_TICK, Tags.CAN_NEXT_STATE) }
) {
    override fun onTickEntity(entity: Entity) {
        entity.configure {
            it -= Tags.CAN_TICK
            it -= Tags.CAN_NEXT_STATE
        }
    }
}