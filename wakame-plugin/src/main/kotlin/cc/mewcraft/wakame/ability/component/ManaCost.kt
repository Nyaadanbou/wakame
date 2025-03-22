package cc.mewcraft.wakame.ability.component

import cc.mewcraft.wakame.ability.ManaCostPenalty
import cc.mewcraft.wakame.molang.Evaluable
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ManaCost(
    var manaCost: Evaluable<*>,
    var penalty: ManaCostPenalty = ManaCostPenalty(),
) : Component<ManaCost> {
    companion object : ComponentType<ManaCost>()

    override fun type(): ComponentType<ManaCost> = ManaCost
}