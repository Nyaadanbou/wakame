package cc.mewcraft.wakame.ability2.component

import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Dash(
    var stepDistance: Double,
    var duration: Long,
    var canContinueAfterHit: Boolean,
    var hitEffects: List<AbilityMeta>,
) : Component<Dash> {
    companion object : ComponentType<Dash>()

    override fun type(): ComponentType<Dash> = Dash
}
