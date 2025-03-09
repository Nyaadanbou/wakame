package cc.mewcraft.wakame.ability.component

import cc.mewcraft.wakame.ability.Ability
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Dash(
    var stepDistance: Double,
    var duration: Long,
    var canContinueAfterHit: Boolean,
    var hitEffects: List<Ability>,
) : Component<Dash> {
    companion object : ComponentType<Dash>()

    override fun type(): ComponentType<Dash> = Dash
}
