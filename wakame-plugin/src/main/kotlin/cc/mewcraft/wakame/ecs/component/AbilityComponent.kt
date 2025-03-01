package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ability.ManaCostPenalty
import cc.mewcraft.wakame.ability.trigger.Trigger
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.molang.Evaluable
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import team.unnamed.mocha.MochaEngine

data class AbilityComponent(
    var manaCost: Evaluable<*>,
    var penalty: ManaCostPenalty = ManaCostPenalty(),
    var phase: StatePhase,
    var trigger: Trigger?,
    var mochaEngine: MochaEngine<*>,
) : Component<AbilityComponent> {
    companion object : ComponentType<AbilityComponent>()

    override fun type(): ComponentType<AbilityComponent> = AbilityComponent
}