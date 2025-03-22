package cc.mewcraft.wakame.ability.component

import cc.mewcraft.wakame.ability.data.StatePhase
import cc.mewcraft.wakame.ability.trigger.AbilityTrigger
import cc.mewcraft.wakame.ability.trigger.AbilityTriggerVariant
import cc.mewcraft.wakame.util.Identifier
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import team.unnamed.mocha.MochaEngine

data class AbilityComponent(
    val abilityId: Identifier,
    var phase: StatePhase,
    var trigger: AbilityTrigger?,
    var variant: AbilityTriggerVariant,
    var mochaEngine: MochaEngine<*>,
) : Component<AbilityComponent> {
    companion object : ComponentType<AbilityComponent>()

    override fun type(): ComponentType<AbilityComponent> = AbilityComponent

    var isReadyToRemove: Boolean = false
    var isMarkNextState: Boolean = false
}