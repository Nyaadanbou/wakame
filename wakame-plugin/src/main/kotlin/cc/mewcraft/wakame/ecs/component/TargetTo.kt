package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ability.character.Target
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class TargetTo(
    var target: Target
) : Component<TargetTo> {
    override fun type(): ComponentType<TargetTo> = TargetTo

    companion object : ComponentType<TargetTo>()
}