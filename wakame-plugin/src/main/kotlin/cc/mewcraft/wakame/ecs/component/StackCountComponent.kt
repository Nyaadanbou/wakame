package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class StackCountComponent(
    var count: Int = 1,
    var maxCount: Int = 10,
) : Component<StackCountComponent> {

    override fun type(): ComponentType<StackCountComponent> = StackCountComponent

    companion object : ComponentType<StackCountComponent>()
}