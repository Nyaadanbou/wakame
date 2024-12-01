package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.component.ComponentMapComponent
import cc.mewcraft.wakame.ecs.component.ResultComponent
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family

class ResultSystem : IteratingSystem(
    family = family { all(ComponentMapComponent, ResultComponent, TickCountComponent, Tags.CAN_TICK) }
) {
    override fun onTickEntity(entity: Entity) {
        val tick = entity[TickCountComponent].tick
        val componentMap = entity[ComponentMapComponent].componentMap
        val result = entity[ResultComponent].result

        result.tick(tick, componentMap)
    }
}