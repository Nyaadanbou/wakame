package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.data.TickResult
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class TickResultComponent(
    var result: TickResult,
    var isDisposable: Boolean = false
) : Component<TickResultComponent> {
    companion object : ComponentType<TickResultComponent>()

    override fun type(): ComponentType<TickResultComponent> = TickResultComponent
}