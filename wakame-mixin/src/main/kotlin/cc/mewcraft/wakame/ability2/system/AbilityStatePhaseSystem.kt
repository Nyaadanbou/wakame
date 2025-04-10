package cc.mewcraft.wakame.ability2.system

import cc.mewcraft.wakame.ability2.StatePhase
import cc.mewcraft.wakame.ability2.TickResult
import cc.mewcraft.wakame.ability2.component.AbilityComponent
import cc.mewcraft.wakame.ability2.component.AbilityTickResultComponent
import cc.mewcraft.wakame.ecs.bridge.EWorld
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

object AbilityStatePhaseSystem : IteratingSystem(
    family = EWorld.family { all(AbilityComponent, AbilityTickResultComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val tickResult = entity[AbilityTickResultComponent].result
        val oldPhase = entity[AbilityComponent].phase
        val newPhase = when {
            tickResult.canAdvanceToNextState() || entity[AbilityComponent].isMarkNextState -> {
                oldPhase.next()
            }

            tickResult == TickResult.RESET_STATE -> {
                StatePhase.RESET
            }

            else -> {
                oldPhase
            }
        }
        if (oldPhase == newPhase)
            return

        entity[AbilityComponent].phase = newPhase
        entity[AbilityComponent].isMarkNextState = false
    }
}