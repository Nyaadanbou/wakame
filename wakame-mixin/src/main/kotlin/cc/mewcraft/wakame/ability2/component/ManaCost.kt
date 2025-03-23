package cc.mewcraft.wakame.ability2.component

import cc.mewcraft.wakame.ability2.ManaCostPenalty
import cc.mewcraft.wakame.molang.Expression
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ManaCost(
    var manaCost: Expression,
    var penalty: ManaCostPenalty = ManaCostPenalty(),
) : Component<ManaCost> {
    companion object : ComponentType<ManaCost>()

    override fun type(): ComponentType<ManaCost> = ManaCost
}