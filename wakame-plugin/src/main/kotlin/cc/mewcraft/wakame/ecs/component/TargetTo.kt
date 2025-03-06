package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.external.KoishEntity
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class TargetTo(
    var target: KoishEntity
) : Component<TargetTo> {
    override fun type(): ComponentType<TargetTo> = TargetTo

    companion object : ComponentType<TargetTo>()
}