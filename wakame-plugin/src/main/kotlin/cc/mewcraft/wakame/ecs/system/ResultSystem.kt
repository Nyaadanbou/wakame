package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.component.*
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family

class ResultSystem : IteratingSystem(
    family = family { all(ComponentMapComponent, ResultComponent, TickCountComponent, Tags.CAN_TICK) }
) {
    override fun onTickEntity(entity: Entity) {
        val tick = entity[TickCountComponent].tick
        val result = entity[ResultComponent].result
        val componentMap = entity[ComponentMapComponent].componentMap

        val tickResult = result.tick(tick, componentMap)

        entity.configure {
            it += TickResultComponent(tickResult)
        }
    }
}