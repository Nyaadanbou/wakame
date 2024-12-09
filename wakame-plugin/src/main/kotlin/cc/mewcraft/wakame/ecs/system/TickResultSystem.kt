package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.component.TickResultComponent
import cc.mewcraft.wakame.ecs.data.TickResult
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import org.slf4j.Logger

class TickResultSystem(
    private val wakameWorld: WakameWorld = inject(),
    private val logger: Logger = inject(),
) : IteratingSystem(
    family = family { all(TickResultComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val tickResult = entity[TickResultComponent].result

        when (tickResult) {
            TickResult.INTERRUPT -> {
                wakameWorld.removeEntity(entity).also { logger.warn("ECS entity ${world.snapshotOf(entity)} Interrupt.") }
                return
            }

            TickResult.CONTINUE_TICK -> return

            TickResult.ALL_DONE -> {
                entity[TickCountComponent].tick = .0
                entity.configure {
                    it += Tags.CAN_NEXT_STATE
                }
            }
        }
    }
}