package cc.mewcraft.wakame.ability2.system

import cc.mewcraft.wakame.ability2.StatePhase
import cc.mewcraft.wakame.ability2.TickResult
import cc.mewcraft.wakame.ability2.component.Ability
import cc.mewcraft.wakame.ability2.component.AbilityTickResult
import cc.mewcraft.wakame.ecs.bridge.EWorld
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

object AbilityStateManager : IteratingSystem(
    family = EWorld.family { all(Ability, AbilityTickResult) }
) {
    override fun onTickEntity(entity: Entity) {
        val tickResult = entity[AbilityTickResult].result
        val oldPhase = entity[Ability].phase
        val newPhase = when {
            tickResult.canAdvanceToNextState() || entity[Ability].isMarkNextState -> {
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

        entity[Ability].phase = newPhase
        entity[Ability].isMarkNextState = false
    }
}