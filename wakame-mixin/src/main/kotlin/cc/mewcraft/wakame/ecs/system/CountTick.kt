package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.component.TickCount
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

object CountTick : IteratingSystem(
    family = EWorld.family { all(TickCount) }
) {
    override fun onTickEntity(entity: Entity) {
        entity[TickCount].tick++
    }
}