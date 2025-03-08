package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class TickCountComponent(
    var tick: Double = .0,
) : Component<TickCountComponent> {
    companion object : ComponentType<TickCountComponent>()

    override fun type(): ComponentType<TickCountComponent> = TickCountComponent
}