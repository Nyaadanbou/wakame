package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class TargetTo(
    var target: FleksEntity
) : Component<TargetTo> {
    override fun type(): ComponentType<TargetTo> = TargetTo

    companion object : ComponentType<TargetTo>()
}