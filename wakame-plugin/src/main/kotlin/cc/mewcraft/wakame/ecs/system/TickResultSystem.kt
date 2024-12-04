package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.TickResultComponent
import cc.mewcraft.wakame.ecs.data.TickResult
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject

class TickResultSystem(
    private val wakameWorld: WakameWorld = inject()
) : IteratingSystem(
    family = family { all(TickResultComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val tickResult = entity[TickResultComponent].result

        when (tickResult) {
            TickResult.ALL_DONE -> wakameWorld.removeEntity(entity)
            TickResult.INTERRUPT -> wakameWorld.removeEntity(entity)
            TickResult.CONTINUE_TICK -> return
        }
    }
}