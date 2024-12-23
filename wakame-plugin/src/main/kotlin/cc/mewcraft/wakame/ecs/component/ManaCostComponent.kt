package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.skill2.ManaCostPenalty
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ManaCostComponent(
    var expression: Evaluable<*>,
    var penalty: ManaCostPenalty = ManaCostPenalty()
) : Component<ManaCostComponent> {
    override fun type(): ComponentType<ManaCostComponent> = ManaCostComponent

    companion object : ComponentType<ManaCostComponent>()
}