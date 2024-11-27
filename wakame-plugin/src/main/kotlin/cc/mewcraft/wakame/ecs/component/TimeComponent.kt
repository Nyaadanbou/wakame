package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class TimeComponent(
    var time: Double
) : Component<TimeComponent> {
    override fun type(): ComponentType<TimeComponent> = TimeComponent

    companion object : ComponentType<TimeComponent>()
}