package cc.mewcraft.wakame.ability2.system

import cc.mewcraft.wakame.ability2.TickResult
import cc.mewcraft.wakame.ability2.component.AbilityComponent
import cc.mewcraft.wakame.ability2.component.AbilityTickResultComponent
import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

object AbilityTickResultSystem : IteratingSystem(
    family = EWorld.family { all(AbilityComponent, AbilityTickResultComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val tickResult = entity[AbilityTickResultComponent].result

        when (tickResult) {
            TickResult.INTERRUPT -> {
                entity.remove()
                return
            }

            TickResult.CONTINUE_TICK -> return

            TickResult.ADVANCE_TO_NEXT_STATE, TickResult.ADVANCE_TO_NEXT_STATE_NO_CONSUME, TickResult.RESET_STATE -> {
                entity[TickCountComponent].tick = 0
                return
            }
        }
    }
}