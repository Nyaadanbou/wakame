package cc.mewcraft.wakame.ability.component

import cc.mewcraft.wakame.ability.ManaCostPenalty
import cc.mewcraft.wakame.ability.data.StatePhase
import cc.mewcraft.wakame.ability.trigger.Trigger
import cc.mewcraft.wakame.ability.trigger.TriggerVariant
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.util.Identifier
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import team.unnamed.mocha.MochaEngine

data class AbilityComponent(
    val abilityId: Identifier,
    var manaCost: Evaluable<*>,
    var penalty: ManaCostPenalty = ManaCostPenalty(),
    var phase: StatePhase,
    var trigger: Trigger?,
    var variant: TriggerVariant,
    var mochaEngine: MochaEngine<*>,
) : Component<AbilityComponent> {
    companion object : ComponentType<AbilityComponent>()

    override fun type(): ComponentType<AbilityComponent> = AbilityComponent

    var isReadyToRemove: Boolean = false
    var isMarkNextState: Boolean = false
}