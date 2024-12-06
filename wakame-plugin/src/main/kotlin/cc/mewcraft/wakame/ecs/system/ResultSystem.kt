package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.*
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject

class ResultSystem(
    private val wakameWorld: WakameWorld = inject()
) : IteratingSystem(
    family = family { all(ResultComponent, TickCountComponent, Tags.CAN_TICK) }
) {
    override fun onTickEntity(entity: Entity) {
        val tick = entity[TickCountComponent].tick
        val result = entity[ResultComponent].result
        val componentMap = wakameWorld.componentMap(entity)

        val tickResult = result.tick(deltaTime.toDouble(), tick, componentMap)

        entity.configure {
            it += TickResultComponent(tickResult)
        }
    }
}