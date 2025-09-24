package cc.mewcraft.wakame.ability.component

import cc.mewcraft.wakame.ability.StatePhase
import cc.mewcraft.wakame.ability.meta.AbilityMeta
import cc.mewcraft.wakame.ability.trigger.AbilityTrigger
import cc.mewcraft.wakame.ability.trigger.AbilityTriggerVariant
import cc.mewcraft.wakame.ecs.bridge.EComponentType
import com.github.quillraven.fleks.Component
import team.unnamed.mocha.MochaEngine

data class Ability(
    val meta: AbilityMeta,
    var phase: StatePhase,
    var trigger: AbilityTrigger?,
    var variant: AbilityTriggerVariant,
    var mochaEngine: MochaEngine<*>,
) : Component<Ability> {
    companion object : EComponentType<Ability>()

    override fun type(): EComponentType<Ability> = Ability

    var isReadyToRemove: Boolean = false
}