package cc.mewcraft.wakame.ability2.component

import cc.mewcraft.wakame.ability2.TickResult
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class AbilityTickResultComponent(
    var result: TickResult,
) : Component<AbilityTickResultComponent> {
    companion object : ComponentType<AbilityTickResultComponent>()

    override fun type(): ComponentType<AbilityTickResultComponent> = AbilityTickResultComponent
}