package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class StackCountComponent(
    var count: Int = 1,
    var maxCount: Int = 10,
    var disappearTick: Int = 500,
) : Component<StackCountComponent> {
    companion object : ComponentType<StackCountComponent>()

    override fun type(): ComponentType<StackCountComponent> = StackCountComponent
}