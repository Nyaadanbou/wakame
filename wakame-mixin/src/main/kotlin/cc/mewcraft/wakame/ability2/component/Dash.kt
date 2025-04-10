package cc.mewcraft.wakame.ability2.component

import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import cc.mewcraft.wakame.ecs.bridge.EComponentType
import com.github.quillraven.fleks.Component

data class Dash(
    var stepDistance: Double,
    var duration: Long,
    var canContinueAfterHit: Boolean,
    var hitEffects: List<AbilityMeta>,
) : Component<Dash> {
    companion object : EComponentType<Dash>()

    override fun type(): EComponentType<Dash> = Dash
}
