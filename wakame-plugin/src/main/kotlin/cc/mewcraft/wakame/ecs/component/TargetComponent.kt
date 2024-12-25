package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ability.character.Target
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class TargetComponent(
    var target: Target
) : Component<TargetComponent> {
    override fun type(): ComponentType<TargetComponent> = TargetComponent

    companion object : ComponentType<TargetComponent>()
}