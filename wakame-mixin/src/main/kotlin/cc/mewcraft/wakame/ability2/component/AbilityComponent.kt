package cc.mewcraft.wakame.ability2.component

import cc.mewcraft.wakame.ability2.StatePhase
import cc.mewcraft.wakame.ability2.meta.AbilityMetaType
import cc.mewcraft.wakame.ability2.trigger.AbilityTrigger
import cc.mewcraft.wakame.ability2.trigger.AbilityTriggerVariant
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import team.unnamed.mocha.MochaEngine

data class AbilityComponent(
    val metaType: AbilityMetaType<*>,
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