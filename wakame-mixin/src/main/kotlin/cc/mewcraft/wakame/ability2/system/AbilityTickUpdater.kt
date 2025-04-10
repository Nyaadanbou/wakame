package cc.mewcraft.wakame.ability2.system

import cc.mewcraft.wakame.ability2.TickResult
import cc.mewcraft.wakame.ability2.component.Ability
import cc.mewcraft.wakame.ability2.component.AbilityTickResult
import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.component.TickCount
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

object AbilityTickUpdater : IteratingSystem(
    family = EWorld.family { all(Ability, AbilityTickResult) }
) {
    override fun onTickEntity(entity: Entity) {
        val tickResult = entity[AbilityTickResult].result

        when (tickResult) {
            TickResult.INTERRUPT -> {
                entity.remove()
                return
            }

            TickResult.CONTINUE_TICK -> return

            TickResult.ADVANCE_TO_NEXT_STATE, TickResult.ADVANCE_TO_NEXT_STATE_NO_CONSUME, TickResult.RESET_STATE -> {
                entity[TickCount].tick = 0
                return
            }
        }
    }
}