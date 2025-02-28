package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.MechanicComponent
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.component.TickResultComponent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family

class MechanicSystem : IteratingSystem(
    family = family { all(MechanicComponent, TickCountComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val tick = entity[TickCountComponent].tick
        val result = entity[MechanicComponent].mechanic
        val componentMap = WakameWorld.componentMap(entity)

        val tickResult = result.tick(deltaTime.toDouble(), tick, componentMap)

        entity.configure {
            it += TickResultComponent(tickResult)
        }
    }
}