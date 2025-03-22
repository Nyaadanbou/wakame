package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ability.component.AbilityComponent
import cc.mewcraft.wakame.ability.component.AbilityTickResultComponent
import cc.mewcraft.wakame.ability.data.TickResult
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family

class AbilityTickResultSystem : IteratingSystem(
    family = family { all(AbilityComponent, AbilityTickResultComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val tickResult = entity[AbilityTickResultComponent].result

        when (tickResult) {
            TickResult.INTERRUPT -> {
                entity.remove()
                return
            }

            TickResult.CONTINUE_TICK -> return

            TickResult.ADVANCE_NEXT_STATE, TickResult.ADVANCE_NEXT_STATE_WITHOUT_CONSUME, TickResult.RESET_STATE -> {
                entity[TickCountComponent].tick = 0
                return
            }
        }
    }
}