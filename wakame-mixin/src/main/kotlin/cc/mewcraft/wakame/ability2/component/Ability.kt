package cc.mewcraft.wakame.ability2.component

import cc.mewcraft.wakame.ability2.StatePhase
import cc.mewcraft.wakame.ability2.meta.AbilityMetaType
import cc.mewcraft.wakame.ability2.trigger.AbilityTrigger
import cc.mewcraft.wakame.ability2.trigger.AbilityTriggerVariant
import cc.mewcraft.wakame.ecs.bridge.EComponentType
import com.github.quillraven.fleks.Component
import team.unnamed.mocha.MochaEngine

data class Ability(
    val metaType: AbilityMetaType<*>,
    var phase: StatePhase,
    var trigger: AbilityTrigger?,
    var variant: AbilityTriggerVariant,
    var mochaEngine: MochaEngine<*>,
) : Component<Ability> {
    companion object : EComponentType<Ability>()

    override fun type(): EComponentType<Ability> = Ability

    var isReadyToRemove: Boolean = false
    var isMarkNextState: Boolean = false
}