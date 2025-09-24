package cc.mewcraft.wakame.ability.component

import cc.mewcraft.wakame.ability.ManaCostPenalty
import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.molang.Expression
import com.github.quillraven.fleks.Component

data class ManaCost(
    var manaCost: Expression,
    var penalty: ManaCostPenalty = ManaCostPenalty(),
) : Component<ManaCost> {
    companion object : EComponentType<ManaCost>()

    override fun type(): EComponentType<ManaCost> = ManaCost
}