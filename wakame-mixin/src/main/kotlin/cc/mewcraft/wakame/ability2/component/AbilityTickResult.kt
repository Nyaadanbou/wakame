package cc.mewcraft.wakame.ability2.component

import cc.mewcraft.wakame.ability2.TickResult
import cc.mewcraft.wakame.ecs.bridge.EComponentType
import com.github.quillraven.fleks.Component

data class AbilityTickResult(
    var result: TickResult,
) : Component<AbilityTickResult> {
    companion object : EComponentType<AbilityTickResult>()

    override fun type(): EComponentType<AbilityTickResult> = AbilityTickResult
}