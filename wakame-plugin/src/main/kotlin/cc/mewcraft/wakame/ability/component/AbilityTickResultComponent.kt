package cc.mewcraft.wakame.ability.component

import cc.mewcraft.wakame.ability.data.TickResult
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class AbilityTickResultComponent(
    var result: TickResult,
) : Component<AbilityTickResultComponent> {
    companion object : ComponentType<AbilityTickResultComponent>()

    override fun type(): ComponentType<AbilityTickResultComponent> = AbilityTickResultComponent
}