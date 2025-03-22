package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ability.component.AbilityComponent
import cc.mewcraft.wakame.ability.component.AbilityTickResultComponent
import cc.mewcraft.wakame.ability.data.StatePhase
import cc.mewcraft.wakame.ability.data.TickResult
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family

class AbilityStatePhaseSystem : IteratingSystem(
    family = family { all(AbilityComponent, AbilityTickResultComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val id = entity[AbilityComponent].abilityId
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